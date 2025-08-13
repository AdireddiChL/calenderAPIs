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
public class AvailabilityRuleRequest {
    private String ownerId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date; // availability date
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime; // inclusive
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;   // exclusive
}


