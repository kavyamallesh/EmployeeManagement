package com.department.hr.employeeManagement.input;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InputEmployee {

    private String id;

    private String login;

    private String name;

    private String salary;

    private String startDate;
}
