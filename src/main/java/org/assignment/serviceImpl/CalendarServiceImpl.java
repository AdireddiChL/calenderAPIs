package org.assignment.serviceImpl;

import org.assignment.dtos.*;
import org.assignment.exceptions.*;
import org.assignment.services.CalendarService;
import org.assignment.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


//Stores owner availabilities and booked appointments in process-local maps.
@Service
public class CalendarServiceImpl implements CalendarService {
    @Autowired
    private ValidationUtil validationUtil;

    // Duration for each appointment in minutes.
    private static final int APPOINTMENT_MINUTES = 60;

    //Map of owner id to a per-date map of available slot start times.
    //Each start time represents a window [start, start + 60m).
    private final Map<String, Map<LocalDate, NavigableSet<LocalTime>>> availabilityByOwner = new HashMap<>();


    //Map of owner id to a per-date map of booked appointments keyed by start time.
    private final Map<String, Map<LocalDate, Map<LocalTime, AppointmentResponse>>> appointmentsByOwner = new HashMap<>();

    /**
     * Sets the availability for a specific owner and date.
     * The provided time window will be aligned to hour boundaries:  according to the requirement
     * start time rounded up to the next hour and end time rounded down to the previous hour.
     * If the owner already has at least one appointment booked for the given date, a conflict is raised
     * and availability cannot be modified for that date.
     * @throws org.assignment.exceptions.BadRequestException when validation fails or no full 60-minute slots available
     * @throws org.assignment.exceptions.ConflictException when appointments already exist for the date
     */
    @Override
    public AvailabilityRuleResponse setAvailability(AvailabilityRuleRequest request) {
        try {
            validationUtil.validateAvailabilityReq(request);

            LocalTime normalizedStart = validationUtil.ceilToHour(request.getStartTime());
            LocalTime normalizedEnd = validationUtil.floorToHour(request.getEndTime());

            if (!normalizedStart.isBefore(normalizedEnd)) {
                throw new BadRequestException("No full 60-minute slots within provided window");
            }

            // Do not allow modifying availability if any appointment already exists on that date
            Map<LocalDate, Map<LocalTime, AppointmentResponse>> ownerAppts = appointmentsByOwner.get(request.getOwnerId());
            if (ownerAppts != null) {
                Map<LocalTime, AppointmentResponse> apptsForDate = ownerAppts.get(request.getDate());
                if (apptsForDate != null && !apptsForDate.isEmpty()) {
                    throw new ConflictException("An appointment has already been booked for that date, you cannot modify your availability, please select another date");
                }
            }

            // Generate proper hourly start times within the normalized window
            List<LocalTime> slots = generateSlotsForDay(normalizedStart, normalizedEnd);
            NavigableSet<LocalTime> slotSet = new TreeSet<>(slots);
            availabilityByOwner
                    .computeIfAbsent(request.getOwnerId(), x -> new HashMap<>())
                    .put(request.getDate(), slotSet);
            return new AvailabilityRuleResponse(200, "Availability set successfully");
        } catch (BadRequestException ex) {
            return new AvailabilityRuleResponse(400, ex.getMessage());
        } catch (ConflictException ex) {
            return new AvailabilityRuleResponse(409, ex.getMessage());
        } catch (Exception ex) {
            return new AvailabilityRuleResponse(500, "Something went wrong, Availability set failed");
        }
    }

    /**
     * Returns the list of available hourly start times(sorted) for all future dates for the given owner.
     * @throws org.assignment.exceptions.BadRequestException when owner id is invalid or no available slots
     */
    @Override
    public List<DaySlots> searchAvailableSlots(String ownerId) {
        try {
            validationUtil.validateAvailabilitySlotsReq(ownerId);
            Map<LocalDate, NavigableSet<LocalTime>> byDate = availabilityByOwner.getOrDefault(ownerId, Collections.emptyMap());
            if (byDate.isEmpty()) {
                throw new AvailabilityException("No available Slots found, please check the availability for the given owner");
            }
            List<DaySlots> result = new ArrayList<>();
            for (Map.Entry<LocalDate, NavigableSet<LocalTime>> entry : byDate.entrySet()) {
                LocalDate date = entry.getKey();
                NavigableSet<LocalTime> times = entry.getValue();
                result.add(new DaySlots(date, new ArrayList<>(times)));
            }
            result.sort(Comparator.comparing(DaySlots::getDate));
            return result;
        } catch (AvailabilityException | BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalServerException("Failed to fetch available slots", ex);
        }
    }

    /**
     * Books a single 60-minute appointment for the given owner/date/start time.
     * The start time must be present in the owner's current availability and not already booked.
     * On success, the chosen slot is removed from availability to prevent double booking.
     * @throws org.assignment.exceptions.BadRequestException when inputs are invalid or slot not available
     * @throws org.assignment.exceptions.ConflictException when attempting to book an already-booked slot
     */
    @Override
    public AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        try {
            validationUtil.validateBookAppointReq(request);
            String ownerId = request.getOwnerId();
            LocalDate date = request.getDate();
            NavigableSet<LocalTime> availableSet = availabilityByOwner
                    .getOrDefault(ownerId, Collections.emptyMap())
                    .get(date);
            if (availableSet == null) {
                throw new AvailabilityException("No availabile slots for owner on this date");
            }
            LocalTime start = request.getStartTime();
            if (!availableSet.contains(start)) {
                throw new AvailabilityException("Selected time slot is not available, please select another time slot");
            }
            LocalTime end = start.plusMinutes(APPOINTMENT_MINUTES);

            Map<LocalDate, Map<LocalTime, AppointmentResponse>> byDate = appointmentsByOwner.computeIfAbsent(ownerId, k -> new HashMap<>());
            Map<LocalTime, AppointmentResponse> bookedForDate = byDate.computeIfAbsent(date, d -> new HashMap<>());

            AppointmentResponse appointment = new AppointmentResponse(
                    UUID.randomUUID(), ownerId, date, start, end, request.getInviteeName(), request.getInviteeEmail());
            //Add this appointment details to the appointmentsByOwner
            bookedForDate.put(start, appointment);
            // Remove this slot from available slots so it is not shown to other invitees
            availableSet.remove(start);
            return appointment;
        }  catch (AvailabilityException | BadRequestException | ConflictException ex) {
            throw ex;
        }  catch (Exception ex) {
            throw new InternalServerException("Failed to book appointment", ex);
        }
    }

    /**
     * Lists all upcoming appointments for the given owner, sorted by date then start time.
     * @throws org.assignment.exceptions.BadRequestException when owner id is null/blank
     */
    @Override
    public List<AppointmentResponse> listUpcomingAppointments(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new BadRequestException("ownerId is required");
        }
        LocalDate today = LocalDate.now();
        return appointmentsByOwner.getOrDefault(ownerId, Collections.emptyMap()).entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(today))
                .sorted(Map.Entry.comparingByKey())
                .flatMap(entry -> entry.getValue().values().stream())
                .sorted(Comparator.comparing(AppointmentResponse::getDate).thenComparing(AppointmentResponse::getStartTime))
                .collect(Collectors.toList());
    }

    /**
     * Generates hourly slot start times within the given window, inclusive of start time and exclusive of end time.
     */
    private List<LocalTime> generateSlotsForDay(LocalTime startInclusive, LocalTime endExclusive) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime time = startInclusive;
        while (!time.plusMinutes(APPOINTMENT_MINUTES).isAfter(endExclusive)) {
            slots.add(time);
            time = time.plusMinutes(APPOINTMENT_MINUTES);
        }
        return slots;
    }
}


