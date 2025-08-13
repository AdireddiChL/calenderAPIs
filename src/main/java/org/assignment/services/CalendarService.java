package org.assignment.services;

import org.assignment.dtos.*;

import java.util.List;

public interface CalendarService {
    AvailabilityRuleResponse setAvailability(AvailabilityRuleRequest request);

    List<DaySlots> searchAvailableSlots(String ownerId);

    AppointmentResponse bookAppointment(BookAppointmentRequest request);

    List<AppointmentResponse> listUpcomingAppointments(String ownerId);
}


