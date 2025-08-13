package org.assignment.utils;

import org.assignment.dtos.AvailabilityRuleRequest;
import org.assignment.dtos.BookAppointmentRequest;
import org.assignment.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Component
public class ValidationUtil {
    /**
     * Validates owner/date/time window for setting availability.
     * checking owner id present
     * checking date present and not in the past
     * checking start and end present and start < end
     */
    public void validateAvailabilityReq(AvailabilityRuleRequest request) {
        if (request.getOwnerId() == null || request.getOwnerId().isBlank()) {
            throw new BadRequestException("ownerId is required");
        }
        if (request.getDate() == null) {
            throw new BadRequestException("date is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BadRequestException("startTime and endTime are required");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("startTime must be before endTime");
        }
        if (request.getDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("date cannot be in the past");
        }
    }

    /** Rounds start time up to the next hour boundary. */
    public LocalTime ceilToHour(LocalTime time) {
        if (time.getMinute() == 0 && time.getSecond() == 0 && time.getNano() == 0) {
            return time.truncatedTo(ChronoUnit.HOURS);
        }
        return time.truncatedTo(ChronoUnit.HOURS).plusHours(1);
    }

    /** Truncates end time down to the hour. */
    public LocalTime floorToHour(LocalTime time) {
        return time.truncatedTo(ChronoUnit.HOURS);
    }

    /** Validates the owner id for slots availability. */
    public void validateAvailabilitySlotsReq(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            throw new BadRequestException("ownerId is required");
        }
    }

    /** Validates inputs for booking an appointment. */
    public void validateBookAppointReq(BookAppointmentRequest request) {
        if (request.getOwnerId() == null || request.getOwnerId().isBlank()) {
            throw new BadRequestException("ownerId is required");
        }
        if (request.getDate() == null || request.getStartTime() == null) {
            throw new BadRequestException("date and startTime are required");
        }
        if (request.getInviteeName() == null || request.getInviteeName().isBlank()) {
            throw new BadRequestException("inviteeName is required");
        }
        if (request.getInviteeEmail() == null || request.getInviteeEmail().isBlank()) {
            throw new BadRequestException("inviteeEmail is required");
        }
    }
}
