package org.assignment.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentRequest {
    private String ownerId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date; // YYYY-MM-DD
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime; // HH:mm
    private String inviteeName;
    private String inviteeEmail;
}


