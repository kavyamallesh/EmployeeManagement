package com.department.hr.employeeManagement.controllers;

import com.department.hr.employeeManagement.exceptions.BadInputException;
import com.department.hr.employeeManagement.exceptions.DuplicateDataException;
import com.department.hr.employeeManagement.exceptions.FileFormatException;
import com.department.hr.employeeManagement.exceptions.InvalidFieldException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
@Slf4j
public class ExceptionHandlingController {

    @ExceptionHandler({BadInputException.class, InvalidFieldException.class, RuntimeException.class, FileFormatException.class, DuplicateDataException.class})
    public ResponseEntity badInputErrorHandler(Exception e) {
        log.error("this is a bad input" + e.getCause().getMessage());
        return ResponseEntity.badRequest().body(e.getCause().getMessage());
    }

    @ExceptionHandler({IOException.class, Exception.class})
    public ResponseEntity internalServerErrorHandler(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(e.getMessage());
    }
}
