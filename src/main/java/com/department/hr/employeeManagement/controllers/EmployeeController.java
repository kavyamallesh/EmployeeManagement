package com.department.hr.employeeManagement.controllers;

import com.department.hr.employeeManagement.entity.Employee;
import com.department.hr.employeeManagement.exceptions.BadInputException;
import com.department.hr.employeeManagement.exceptions.DuplicateDataException;
import com.department.hr.employeeManagement.exceptions.FileFormatException;
import com.department.hr.employeeManagement.exceptions.InvalidFieldException;
import com.department.hr.employeeManagement.service.EmployeeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class EmployeeController {

    @Autowired
    private final EmployeeService service;

    @PostMapping("/upload")
    public ResponseEntity uploadCSV(@RequestParam("file") MultipartFile file) throws FileFormatException, IOException, DuplicateDataException {
        log.info("Received request for upload");
        final List employeesSaved = service.uploadData(file);
        if (employeesSaved.size() > 0) {
            return ResponseEntity.ok("Successful");
        }
        return ResponseEntity.status(201).body("Success but no data updated");
    }

    @GetMapping
    public ResponseEntity getEmployees(@RequestParam(required = false, name = "minSalary", defaultValue = "0") Double minSalary,
                                       @RequestParam(required = false, name = "maxSalary", defaultValue = "4000.00") Double maxSalary,
                                       @RequestParam(required = false, name = "offset", defaultValue = "0") Integer offset,
                                       @RequestParam(required = false, name = "limit", defaultValue = "0") Integer limit,
                                       @RequestParam(required = false, name = "orderByfieldAndDirection", defaultValue = "id-asc") String sortFieldsAndDirection) {

        List<Employee> employeeList;
        employeeList = service.fetchEmployees(minSalary, maxSalary, offset, limit, sortFieldsAndDirection);
        return ResponseEntity.ok(createResponse(employeeList));
    }

    @GetMapping("/{id}")
    public ResponseEntity getEmployee(@PathVariable("id") String id) throws BadInputException {
        Employee employee = service.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    public ResponseEntity createEmployee(@RequestBody Employee employee) throws InvalidFieldException, BadInputException, Exception {
        final String employeeId = service.creatEmployee(employee);
        return ResponseEntity.ok("Successfully created");
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEmployee(@RequestBody Employee employee) throws BadInputException, InvalidFieldException {
        final String employeeId = service.updateEmployee(employee);
        return ResponseEntity.ok("Successfully updated");
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteEmployee(@PathVariable("id") String id) throws BadInputException {
        service.deleteEmployee(id);
        return ResponseEntity.ok("Successfully deleted");
    }

    private HashMap<String, List<Employee>> createResponse(List<Employee> employeeList) {
        final HashMap<String, List<Employee>> response = new HashMap<>();
        response.put("response", employeeList);
        return response;
    }

}
