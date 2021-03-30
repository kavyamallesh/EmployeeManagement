package com.department.hr.employeeManagement.vo;

import com.department.hr.employeeManagement.entity.Employee;
import lombok.Data;

import java.util.List;

@Data
public class Results {
    private List<Employee> results;
}
