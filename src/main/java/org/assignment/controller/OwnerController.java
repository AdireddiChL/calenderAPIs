package org.assignment.controller;

import org.assignment.constants.ApiPaths;
import org.assignment.dtos.AvailabilityRuleRequest;
import org.assignment.dtos.AvailabilityRuleResponse;
import org.assignment.dtos.AppointmentResponse;
import org.assignment.services.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.OWNER)
public class OwnerController {

    private final CalendarService calendarService;

    public OwnerController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @PostMapping(ApiPaths.SET_AVAILABILITY)
    public ResponseEntity<AvailabilityRuleResponse> setAvailability(@RequestBody AvailabilityRuleRequest request) {
        return ResponseEntity.ok(calendarService.setAvailability(request));
    }

    @GetMapping(ApiPaths.LIST_APPOINTMENTS)
    public ResponseEntity<List<AppointmentResponse>> listAppointments(@RequestParam("ownerId") String ownerId) {
        return ResponseEntity.ok(calendarService.listUpcomingAppointments(ownerId));
    }
}
