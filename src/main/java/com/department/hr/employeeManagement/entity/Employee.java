package com.department.hr.employeeManagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Employee {

    @Id
    @Pattern(regexp = "[A-Za-z0-9]+", message = "Invalid id, id can only be alphanumeric")
    private String id;

    @Column(unique = true, nullable = false)
    @Pattern(regexp = "[A-Za-z0-9]+", message = "Invalid login, login can only be alphanumeric")
    private String login;

    @NotNull(message = "Invalid name")
    private String name;

    @DecimalMin(value = "0.0", message = "Invalid salary")
    @NotNull(message = "Invalid salary")
    private Double salary;

    @NotNull(message = "Invalid date")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "[yyyy-MM-dd]")
    public LocalDate getStartDate() {
        return startDate;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "[yyyy-MM-dd][dd-MMM-yy]")
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
