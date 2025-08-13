package org.assignment.controller;

import org.assignment.constants.ApiPaths;
import org.assignment.dtos.AppointmentResponse;
import org.assignment.dtos.BookAppointmentRequest;
import org.assignment.dtos.DaySlots;
import org.assignment.services.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.INVITEE)
public class InviteeController {

    private final CalendarService calendarService;

    public InviteeController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping(ApiPaths.SEARCH_SLOTS)
    public ResponseEntity<List<DaySlots>> searchSlots(@RequestParam("ownerId") String ownerId) {
        List<DaySlots> result = calendarService.searchAvailableSlots(ownerId);
        return ResponseEntity.ok(result);
    }

    @PostMapping(ApiPaths.BOOK_APPOINTMENT)
    public ResponseEntity<AppointmentResponse> bookAppointment(@RequestBody BookAppointmentRequest request) {
        return ResponseEntity.ok(calendarService.bookAppointment(request));
    }
}
