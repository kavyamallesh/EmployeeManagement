package com.department.hr.employeeManagement.service;

import com.department.hr.employeeManagement.domain.OffsetBasedPageRequest;
import com.department.hr.employeeManagement.domain.SortedUnpaged;
import com.department.hr.employeeManagement.entity.Employee;
import com.department.hr.employeeManagement.exceptions.BadInputException;
import com.department.hr.employeeManagement.exceptions.DuplicateDataException;
import com.department.hr.employeeManagement.exceptions.FileFormatException;
import com.department.hr.employeeManagement.exceptions.InvalidFieldException;
import com.department.hr.employeeManagement.input.UpdateEmployee;
import com.department.hr.employeeManagement.repository.EmployeeRepository;
import com.department.hr.employeeManagement.validators.EmployeeValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;
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

    public List uploadData(MultipartFile file) throws FileFormatException, IOException, DuplicateDataException, BadInputException {
        validator.validateInputFile(file);
        final List<Employee> entitiesFromFile = getEntitiesFromFile(file);
        return repository.saveAll(entitiesFromFile);
    }

    protected List<Employee> getEntitiesFromFile(MultipartFile file) throws IOException, DuplicateDataException, BadInputException {
        final List<CSVRecord> records = processCSVFile(file);
        List<Employee> employeesToSave = records.stream().map(r -> {
            try {
                return createEmployee(r);
            } catch (InvalidFieldException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        checkForDuplicateRecords(employeesToSave);

        checkForNonUniqueLogin(employeesToSave);
        return employeesToSave;
    }

    private void checkForNonUniqueLogin(List<Employee> employeesToSave) throws DuplicateDataException, BadInputException {
        final List<String> logins = employeesToSave.stream().map(Employee::getLogin).collect(Collectors.toList());

        Set<String> duplicateLogins = logins.stream()
                .filter(i -> Collections.frequency(logins, i) > 1)
                .collect(Collectors.toSet());
        if (duplicateLogins.size() != 0) {
            throw new DuplicateDataException("Duplicate login ids detected - " + duplicateLogins);
        }

//        List<Employee> all = repository.findAll();
//        Set<String> nonAvailableLoginIds = all.stream().map(Employee::getLogin).collect(Collectors.toSet());
//        logins.retainAll(nonAvailableLoginIds);
//        if (logins.size() > 0) {
//            throw new BadInputException("Following login ids are unavailable - " + logins);
//        }

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
        final List<String> ids = employeesToSave.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());

        Set<String> duplicateIds = ids.stream()
                .filter(i -> Collections.frequency(ids, i) > 1)
                .collect(Collectors.toSet());

        if (duplicateIds.size() != 0) {
            throw new DuplicateDataException("Duplicate ids detected - " + duplicateIds);
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
        final List<Sort.Order> sortOrderList = getSortOrderList(sortFieldsAndDirection);

        if (limit > 0) {
            Pageable pageable = new OffsetBasedPageRequest(offset, limit, Sort.by(sortOrderList));
            final Page<Employee> pagedResults = repository.findBySalaryGreaterThanEqualAndSalaryLessThan(minSalary, maxSalary, pageable);
            return pagedResults != null ? pagedResults.getContent() : Collections.emptyList();
        }
        final Page<Employee> sortedResults = repository.findBySalaryGreaterThanEqualAndSalaryLessThan(minSalary, maxSalary, SortedUnpaged.getInstance(Sort.by(sortOrderList)));
        return sortedResults != null ? sortedResults.getContent() : Collections.emptyList();
    }

    protected List<Sort.Order> getSortOrderList(String sortFieldsWithDirection) {
        return Arrays.stream(sortFieldsWithDirection.split(","))
                .map(s -> s.split("-"))
                .map(f -> getSortFieldAndDirection(f))
                .collect(Collectors.toList());
    }

    private Sort.Order getSortFieldAndDirection(String[] fieldWithDirection) {
        final String field = fieldWithDirection[0];
        final String direction = fieldWithDirection.length == 2 ? fieldWithDirection[1] : null;
        try {
            validator.validateSortField(field);
        } catch (BadInputException e) {
            throw new RuntimeException(e);
        }
        return new Sort.Order(getDirection(direction), field);
    }

    private Sort.Direction getDirection(String direction) {
        return direction != null && direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    public Employee getEmployeeById(String id) throws BadInputException {
        final Optional<Employee> employee = repository.findById(id);
        if (!employee.isPresent()) {
            throw new BadInputException("No such employee");
        }
        return employee.get();
    }

    public String createEmployee(Employee inputEmployee) throws InvalidFieldException, BadInputException {
        final String id = inputEmployee.getId();
        validator.validateId(id, "id");
        if (repository.existsById(inputEmployee.getId())) {
            throw new BadInputException("Employee ID already exists");
        }
        final String login = inputEmployee.getLogin();
        validator.validateId(id, "id");
        if (repository.existsByLogin(login)) {
            throw new BadInputException("Employee login not unique");
        }
        final Employee newEmployee = repository.save(inputEmployee);

        return newEmployee.getId();
    }

    public void updateEmployee(String id, UpdateEmployee updateEmployee) throws BadInputException, InvalidFieldException {
        Optional<Employee> optionalEmployee = repository.findById(id);

        if (!optionalEmployee.isPresent()) {
            throw new BadInputException("No such employee");
        }

        Employee employeeToUpdate = optionalEmployee.get();

        if (!employeeToUpdate.getLogin().equals(updateEmployee.getLogin()) && repository.existsByLogin(updateEmployee.getLogin())) {
            throw new BadInputException("Employee login not unique");
        }
        Employee employee = new Employee(id, updateEmployee.getLogin(), updateEmployee.getName(), updateEmployee.getSalary(), updateEmployee.getStartDate());
        repository.save(employee);
    }

    public void deleteEmployee(String id) throws BadInputException {
        if (!repository.existsById(id)) {
            throw new BadInputException("No such employee");
        }
        repository.deleteById(id);
    }
}