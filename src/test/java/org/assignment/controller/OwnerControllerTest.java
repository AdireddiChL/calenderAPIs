package org.assignment.controller;

import org.assignment.dtos.AppointmentResponse;
import org.assignment.dtos.AvailabilityRuleRequest;
import org.assignment.dtos.AvailabilityRuleResponse;
import org.assignment.services.CalendarService;
import org.assignment.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    @Mock
    private CalendarService calendarService;

    @InjectMocks
    private OwnerController ownerController;

    @Test
    void setAvailability_success() {
        AvailabilityRuleRequest req = new AvailabilityRuleRequest("owner1", LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(12, 0));
        AvailabilityRuleResponse resp = new AvailabilityRuleResponse(200, "Availability set successfully");
        given(calendarService.setAvailability(any(AvailabilityRuleRequest.class))).willReturn(resp);

        ResponseEntity<AvailabilityRuleResponse> response = ownerController.setAvailability(req);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(resp, response.getBody());
    }

    @Test
    void listAppointments_success() {
        LocalDate date = LocalDate.now().plusDays(1);
        List<AppointmentResponse> resp = List.of(
                new AppointmentResponse(UUID.randomUUID(), "owner1", date, LocalTime.of(10, 0), LocalTime.of(11, 0), "U", "u@e.com")
        );
        given(calendarService.listUpcomingAppointments("owner1")).willReturn(resp);

        ResponseEntity<List<AppointmentResponse>> response = ownerController.listAppointments("owner1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(resp, response.getBody());
    }

    @Test
    void listAppointments_whenValidationError_propagatesException() {
        given(calendarService.listUpcomingAppointments("")).willThrow(new BadRequestException("ownerId is required"));
        assertThrows(BadRequestException.class, () -> ownerController.listAppointments(""));
    }
}


