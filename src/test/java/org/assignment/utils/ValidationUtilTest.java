package org.assignment.utils;

import org.assignment.dtos.AvailabilityRuleRequest;
import org.assignment.dtos.BookAppointmentRequest;
import org.assignment.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    private final ValidationUtil util = new ValidationUtil();

    @Test
    void ceilToHour_behaviour() {
        assertEquals(LocalTime.of(10, 0), util.ceilToHour(LocalTime.of(10, 0)));
        assertEquals(LocalTime.of(11, 0), util.ceilToHour(LocalTime.of(10, 1)));
        assertEquals(LocalTime.of(11, 0), util.ceilToHour(LocalTime.of(10, 59, 59)));
    }

    @Test
    void floorToHour_behaviour() {
        assertEquals(LocalTime.of(10, 0), util.floorToHour(LocalTime.of(10, 59)));
        assertEquals(LocalTime.of(10, 0), util.floorToHour(LocalTime.of(10, 0)));
    }

    @Test
    void validateAvailabilityReq_invalid_throws() {
        AvailabilityRuleRequest r1 = new AvailabilityRuleRequest(null, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertThrows(BadRequestException.class, () -> util.validateAvailabilityReq(r1));

        AvailabilityRuleRequest r2 = new AvailabilityRuleRequest("owner", null, LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertThrows(BadRequestException.class, () -> util.validateAvailabilityReq(r2));

        AvailabilityRuleRequest r3 = new AvailabilityRuleRequest("owner", LocalDate.now(), null, LocalTime.of(10, 0));
        assertThrows(BadRequestException.class, () -> util.validateAvailabilityReq(r3));

        AvailabilityRuleRequest r4 = new AvailabilityRuleRequest("owner", LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(9, 0));
        assertThrows(BadRequestException.class, () -> util.validateAvailabilityReq(r4));

        AvailabilityRuleRequest r5 = new AvailabilityRuleRequest("owner", LocalDate.now().minusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertThrows(BadRequestException.class, () -> util.validateAvailabilityReq(r5));
    }

    @Test
    void validateBookAppointmentReq_invalid_throws() {
        BookAppointmentRequest b1 = new BookAppointmentRequest(null, LocalDate.now(), LocalTime.of(10, 0), "xyz", "abc@gmail");
        assertThrows(BadRequestException.class, () -> util.validateBookAppointReq(b1));

        BookAppointmentRequest b2 = new BookAppointmentRequest("owner", null, LocalTime.of(10, 0), "xyz", "abc@gmail");
        assertThrows(BadRequestException.class, () -> util.validateBookAppointReq(b2));

        BookAppointmentRequest b3 = new BookAppointmentRequest("owner", LocalDate.now(), null, "xyz", "abc@gmail");
        assertThrows(BadRequestException.class, () -> util.validateBookAppointReq(b3));

        BookAppointmentRequest b4 = new BookAppointmentRequest("owner", LocalDate.now(), LocalTime.of(10, 0), "", "abc@gmail");
        assertThrows(BadRequestException.class, () -> util.validateBookAppointReq(b4));

        BookAppointmentRequest b5 = new BookAppointmentRequest("owner", LocalDate.now(), LocalTime.of(10, 0), "xyz", "");
        assertThrows(BadRequestException.class, () -> util.validateBookAppointReq(b5));
    }

    @Test
    void validateAvailabilitySlotsReq_invalid_throws() {
        assertThrows(BadRequestException.class, () -> util.validateAvailabilitySlotsReq(" "));
        assertThrows(BadRequestException.class, () -> util.validateAvailabilitySlotsReq(null));
    }
}


