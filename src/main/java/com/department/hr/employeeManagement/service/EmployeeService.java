package com.department.hr.employeeManagement.service;

import com.department.hr.employeeManagement.entity.Employee;
import com.department.hr.employeeManagement.exceptions.DuplicateDataException;
import com.department.hr.employeeManagement.exceptions.FileFormatException;
import com.department.hr.employeeManagement.exceptions.InvalidFieldException;
import com.department.hr.employeeManagement.repository.EmployeeRepository;
import com.department.hr.employeeManagement.validators.EmployeeValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeeService {

    @Autowired
    private final EmployeeValidator validator;

    @Autowired
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeValidator validator, EmployeeRepository repository) {
        this.validator = validator;
        this.repository = repository;
    }

    public List uploadData(MultipartFile file) throws FileFormatException, IOException, DuplicateDataException {
        validator.validateInputFile(file);
        final List<Employee> entitiesFromFile = getEntitiesFromFile(file);
        return repository.saveAll(entitiesFromFile);
    }

    protected List<Employee> getEntitiesFromFile(MultipartFile file) throws IOException, DuplicateDataException {

        final List<CSVRecord> records = processCSVFile(file);
        List<Employee> employeesToSave = records.stream().map(r -> {
            try {
                return createEmployee(r);
            } catch (InvalidFieldException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        checkForDuplicateRecords(employeesToSave);

        return employeesToSave;
    }

    protected List<CSVRecord> processCSVFile(MultipartFile file) throws IOException {
        final List<CSVRecord> records;
        final InputStream inputStream = file.getInputStream();
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim().withCommentMarker('#'))) {
            records = csvParser.getRecords();
        }
        return records;
    }

    private void checkForDuplicateRecords(List<Employee> employeesToSave) throws DuplicateDataException {
        final Set<String> ids = employeesToSave.stream().map(e -> e.getId())
                .filter(i -> Collections.frequency(employeesToSave, i) > 1)
                .collect(Collectors.toSet());

        if (ids.size() != 0) {
            throw new DuplicateDataException("Duplicate rows detected for the following id(s) " + ids);
        }
    }

    private Employee createEmployee(CSVRecord record) throws InvalidFieldException {
        final String id = validator.validateId(record.get("id"), "id");
        final String login = validator.validateLogin(record.get("login"), "login");
        final String name = validator.validateName(record.get("name"), "name");
        final Double salary = validator.validateAndGetSalary(record.get("salary"), "salary");
        final LocalDate startDate = validator.validateAndGetStartDate(record.get("startDate"), "startDate");
        return new Employee(id, login, name, salary, startDate);
    }


    public List<Employee> fetchEmployees(Double minSalary, Double maxSalary, Integer offset, Integer limit, String sortFieldsAndDirection) {
        return null;
    }
}