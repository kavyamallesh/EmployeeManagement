package com.department.hr.employeeManagement.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
public class EmployeeController {

    @PostMapping("/upload")
    public ResponseEntity uploadCSV(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok("Successful");
    }
}
