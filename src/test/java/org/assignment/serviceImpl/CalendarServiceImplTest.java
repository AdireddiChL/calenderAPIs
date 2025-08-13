package org.assignment.serviceImpl;

import org.assignment.dtos.*;
import org.assignment.exceptions.BadRequestException;
import org.assignment.exceptions.AvailabilityException;
import org.assignment.utils.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceImplTest {

    @Spy
    private ValidationUtil validationUtil = new ValidationUtil();

    @InjectMocks
    private CalendarServiceImpl calendarService;

    private String ownerId;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        ownerId = "owner-1";
        date = LocalDate.now().plusDays(1);
    }

    @Test
    void setAvailability_and_search_and_book_flow() {
        AvailabilityRuleRequest availabilityRuleRequest = new AvailabilityRuleRequest(
                ownerId,
                date,
                LocalTime.of(9, 15),
                LocalTime.of(12, 45)
        );

        AvailabilityRuleResponse response = calendarService.setAvailability(availabilityRuleRequest);
        assertEquals(200, response.getCode());

        List<DaySlots> slotsBefore = calendarService.searchAvailableSlots(ownerId);
        assertEquals(1, slotsBefore.size());
        DaySlots daySlots = slotsBefore.get(0);
        assertEquals(date, daySlots.getDate());
        // 09:15-12:45 aligns to 10:00-12:00, so 10:00 and 11:00 are valid starts
        assertEquals(List.of(LocalTime.of(10, 0), LocalTime.of(11, 0)), daySlots.getAvailableStartTimes());

        BookAppointmentRequest book = new BookAppointmentRequest(
                ownerId,
                date,
                LocalTime.of(10, 0),
                "Test User",
                "test@example.com"
        );

        AppointmentResponse appt = calendarService.bookAppointment(book);
        assertEquals(ownerId, appt.getOwnerId());
        assertEquals(date, appt.getDate());
        assertEquals(LocalTime.of(10, 0), appt.getStartTime());
        assertEquals(LocalTime.of(11, 0), appt.getEndTime());

        List<DaySlots> slotsAfter = calendarService.searchAvailableSlots(ownerId);
        assertEquals(1, slotsAfter.size());
        assertEquals(List.of(LocalTime.of(11, 0)), slotsAfter.get(0).getAvailableStartTimes());
    }

    @Test
    void setAvailability_whenInvalid_returnsBadRequestResponse() {
        // Missing ownerId triggers validation failure
        AvailabilityRuleRequest bad = new AvailabilityRuleRequest(
                null,
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );
        AvailabilityRuleResponse resp = calendarService.setAvailability(bad);
        assertEquals(400, resp.getCode());
    }

    @Test
    void setAvailability_whenPastDate_returnsBadRequestResponse() {
        AvailabilityRuleRequest bad = new AvailabilityRuleRequest(
                ownerId,
                LocalDate.now().minusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );
        AvailabilityRuleResponse resp = calendarService.setAvailability(bad);
        assertEquals(400, resp.getCode());
    }

    @Test
    void bookAppointment_whenNoAvailability_throwsAvailabilityException() {
        BookAppointmentRequest req = new BookAppointmentRequest(
                ownerId,
                date,
                LocalTime.of(10, 0),
                "User",
                "u@example.com"
        );

        assertThrows(AvailabilityException.class, () -> calendarService.bookAppointment(req));
    }

    @Test
    void listUpcomingAppointments_returnsSortedAppointments() {
        // Prepare two slots across two days and book them in reverse order, expect sorted by date then time
        LocalDate d1 = LocalDate.now().plusDays(1);
        LocalDate d2 = LocalDate.now().plusDays(2);

        calendarService.setAvailability(new AvailabilityRuleRequest(ownerId, d1, LocalTime.of(10, 0), LocalTime.of(12, 0)));
        calendarService.setAvailability(new AvailabilityRuleRequest(ownerId, d2, LocalTime.of(9, 0), LocalTime.of(11, 0)));

        calendarService.bookAppointment(new BookAppointmentRequest(ownerId, d2, LocalTime.of(10, 0), "A", "a@a.com"));
        calendarService.bookAppointment(new BookAppointmentRequest(ownerId, d1, LocalTime.of(11, 0), "B", "b@b.com"));

        List<AppointmentResponse> upcoming = calendarService.listUpcomingAppointments(ownerId);
        assertEquals(2, upcoming.size());
        assertEquals(d1, upcoming.get(0).getDate());
        assertEquals(LocalTime.of(11, 0), upcoming.get(0).getStartTime());
        assertEquals(d2, upcoming.get(1).getDate());
        assertEquals(LocalTime.of(10, 0), upcoming.get(1).getStartTime());
    }

    @Test
    void searchAvailableSlots_withBlankOwner_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> calendarService.searchAvailableSlots(" "));
    }

    @Test
    void searchAvailableSlots_whenNoAvailability_throwsAvailabilityException() {
        // No availability set for owner
        assertThrows(AvailabilityException.class, () -> calendarService.searchAvailableSlots(ownerId));
    }

    @Test
    void bookAppointment_whenSlotAlreadyBooked_throwsAvailabilityException() {
        calendarService.setAvailability(new AvailabilityRuleRequest(ownerId, date, LocalTime.of(10, 0), LocalTime.of(12, 0)));
        calendarService.bookAppointment(new BookAppointmentRequest(ownerId, date, LocalTime.of(10, 0), "A", "a@a.com"));
        assertThrows(AvailabilityException.class, () ->
                calendarService.bookAppointment(new BookAppointmentRequest(ownerId, date, LocalTime.of(10, 0), "B", "b@b.com"))
        );
    }

    @Test
    void setAvailability_whenAppointmentsExistForDate_returnsConflictResponse() {
        // initial availability and a booking
        calendarService.setAvailability(new AvailabilityRuleRequest(ownerId, date, LocalTime.of(10, 0), LocalTime.of(12, 0)));
        calendarService.bookAppointment(new BookAppointmentRequest(ownerId, date, LocalTime.of(10, 0), "User", "u@example.com"));

        // attempt to change availability for the same date
        AvailabilityRuleRequest updateReq = new AvailabilityRuleRequest(ownerId, date, LocalTime.of(13, 0), LocalTime.of(15, 0));
        AvailabilityRuleResponse resp = calendarService.setAvailability(updateReq);
        assertEquals(409, resp.getCode());
        assertTrue(resp.getMessage().contains("An appointment has already been booked for that date"));
    }
}


