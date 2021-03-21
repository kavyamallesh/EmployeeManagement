package com.department.hr.employeeManagement.controllers;

import com.department.hr.employeeManagement.entity.Employee;
import com.department.hr.employeeManagement.exceptions.DuplicateDataException;
import com.department.hr.employeeManagement.exceptions.FileFormatException;
import com.department.hr.employeeManagement.exceptions.InvalidFieldException;
import com.department.hr.employeeManagement.service.EmployeeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class EmployeeController {

    @Autowired
    private final EmployeeService service;

    @PostMapping("/upload")
    public ResponseEntity uploadCSV(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Received request for upload");
            final List employeesSaved = service.uploadData(file);
            if (employeesSaved.size() > 0) {
                return ResponseEntity.ok("Successful");
            }
            return ResponseEntity.status(201).body("Success but no data updated");
        } catch (FileFormatException | DuplicateDataException e) {
            return ResponseEntity.badRequest().body("Bad input -" + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(e.getMessage());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof InvalidFieldException) {
                return ResponseEntity.badRequest().body("Bad input -" + e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(e.getMessage());
            }
        }
    }

    @GetMapping
    public ResponseEntity getEmployees(@RequestParam(required = false, name = "minSalary", defaultValue = "0") Double minSalary,
                                       @RequestParam(required = false, name = "maxSalary", defaultValue = "4000.00") Double maxSalary,
                                       @RequestParam(required = false, name = "offset", defaultValue = "0") Integer offset,
                                       @RequestParam(required = false, name = "limit", defaultValue = "0") Integer limit,
                                       @RequestParam(required = false, name = "orderByfieldAndDirection", defaultValue = "id-asc") String sortFieldsAndDirection) {

        //TODO : define your own filters and sorting
        List<Employee> employeeList = service.fetchEmployees(minSalary, maxSalary, offset, limit, sortFieldsAndDirection);
        return ResponseEntity.ok(employeeList);
    }

}
