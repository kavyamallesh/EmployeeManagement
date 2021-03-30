package com.department.hr.employeeManagement.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployee {

    private String login;

    @NotNull(message = "Invalid name")
    private String name;

    @DecimalMin(value = "0.0", message = "Invalid salary")
    @NotNull(message = "Invalid salary")
    private Double salary;

    @NotNull(message = "Invalid date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "[yyyy-MM-dd][dd-MMM-yy]")
    private LocalDate startDate;
}
