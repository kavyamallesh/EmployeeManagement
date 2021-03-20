package com.department.hr.employeeManagement.exceptions;

public class InvalidFieldException extends Throwable {
    public InvalidFieldException(String message) {
        super(message);
    }

    public InvalidFieldException() {

    }
}
