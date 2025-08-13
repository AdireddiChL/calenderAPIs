package org.assignment.controller;

import org.assignment.dtos.AppointmentResponse;
import org.assignment.dtos.BookAppointmentRequest;
import org.assignment.dtos.DaySlots;
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
class InviteeControllerTest {

    @Mock
    private CalendarService calendarService;

    @InjectMocks
    private InviteeController inviteeController;

    @Test
    void searchSlots_success() {
        LocalDate date = LocalDate.now().plusDays(1);
        List<DaySlots> serviceResp = List.of(new DaySlots(date, List.of(LocalTime.of(10, 0), LocalTime.of(11, 0))));
        given(calendarService.searchAvailableSlots("owner1")).willReturn(serviceResp);

        ResponseEntity<List<DaySlots>> response = inviteeController.searchSlots("owner1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(serviceResp, response.getBody());
    }

    @Test
    void bookAppointment_success() {
        LocalDate date = LocalDate.now().plusDays(1);
        BookAppointmentRequest req = new BookAppointmentRequest("owner1", date, LocalTime.of(10, 0), "User", "u@example.com");
        AppointmentResponse resp = new AppointmentResponse(UUID.randomUUID(), "owner1", date, LocalTime.of(10, 0), LocalTime.of(11, 0), "User", "u@example.com");
        given(calendarService.bookAppointment(any(BookAppointmentRequest.class))).willReturn(resp);

        ResponseEntity<AppointmentResponse> response = inviteeController.bookAppointment(req);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(resp, response.getBody());
    }

    @Test
    void searchSlots_whenValidationError_propagatesException() {
        given(calendarService.searchAvailableSlots(""))
                .willThrow(new BadRequestException("ownerId is required"));

        assertThrows(BadRequestException.class, () -> inviteeController.searchSlots(""));
    }

    @Test
    void bookAppointment_whenSlotConflict_propagatesException() {
        LocalDate date = LocalDate.now().plusDays(1);
        BookAppointmentRequest req = new BookAppointmentRequest("owner1", date, LocalTime.of(10, 0), "User", "u@example.com");
        given(calendarService.bookAppointment(any(BookAppointmentRequest.class)))
                .willThrow(new org.assignment.exceptions.ConflictException("Slot already booked"));

        assertThrows(org.assignment.exceptions.ConflictException.class, () -> inviteeController.bookAppointment(req));
    }
}


