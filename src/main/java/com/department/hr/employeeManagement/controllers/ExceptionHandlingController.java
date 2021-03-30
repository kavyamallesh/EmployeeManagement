package com.department.hr.employeeManagement.controllers;

import com.department.hr.employeeManagement.exceptions.BadInputException;
import com.department.hr.employeeManagement.exceptions.DuplicateDataException;
import com.department.hr.employeeManagement.exceptions.FileFormatException;
import com.department.hr.employeeManagement.exceptions.InvalidFieldException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ExceptionHandlingController {

    @ExceptionHandler({BadInputException.class, MissingServletRequestPartException.class, InvalidFieldException.class, RuntimeException.class, FileFormatException.class, DuplicateDataException.class})
    public ResponseEntity badInputErrorHandler(Exception e) {
        String localizedMessage = e.getLocalizedMessage();
        if (localizedMessage.contains("java.time.format.DateTimeParseException")) {
            return ResponseEntity.badRequest().body("Invalid date");
        }

        if(localizedMessage.contains("ConstraintViolationException") && localizedMessage.contains("EMPLOYEE(LOGIN)")){
            return ResponseEntity.badRequest().body("Login id is not unique");
        }
        return ResponseEntity.badRequest().body(e.getCause().getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity badInputErrorHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(er -> er.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler({IOException.class, Exception.class})
    public ResponseEntity internalServerErrorHandler(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(e.getMessage());
    }
}
