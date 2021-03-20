package com.department.hr.employeeManagement.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sun.javafx.beans.IDProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Employee {

    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double salary;

    @Column(nullable = false)
    private LocalDate startDate;

    @JsonIgnore
    private LocalDateTime lastModifiedAt;

}
