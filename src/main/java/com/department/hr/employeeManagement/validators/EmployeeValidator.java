package com.department.hr.employeeManagement.validators;

import com.department.hr.employeeManagement.entity.Employee;
import com.department.hr.employeeManagement.exceptions.BadInputException;
import com.department.hr.employeeManagement.exceptions.FileFormatException;
import com.department.hr.employeeManagement.exceptions.InvalidFieldException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeeValidator {

    public static final String CONTENT_TYPE_TEXT_CSV = "text/csv";
    public static final DateTimeFormatter validPattern = DateTimeFormatter.ofPattern("[yyyy-MM-dd][dd-MMM-yy]");
    public static final DateTimeFormatter DD_MMMM_YY = DateTimeFormatter.ofPattern("dd-MMM-yy");
    public static final List<String> FIELDS = Arrays.stream(Employee.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());


    public void validateInputFile(MultipartFile file) throws FileFormatException {
        if (!CONTENT_TYPE_TEXT_CSV.equalsIgnoreCase(file.getContentType())) {
            throw new FileFormatException("The input file provided is not of a valid format. Please upload a csv file only");
        }
    }

    public String validateId(String id, String fieldName) throws InvalidFieldException {
        if (isNull(id, fieldName) || !id.matches("[A-Za-z0-9]+")) {
            throw new InvalidFieldException(String.format("Invalid id %s, only alphanumeric values are allowed for id", id));
        }
        return id;
    }

    public String validateLogin(String login, String fieldName) throws InvalidFieldException {
        if (isNull(login, fieldName) || !login.matches("[A-Za-z0-9]+")) {
            throw new InvalidFieldException(String.format("Invalid login %s, only alphanumeric values are allowed for login", login));
        }
        return login;
    }

    public String validateName(String name, String fieldName) throws InvalidFieldException {
        isNull(name, fieldName);
        return name;
    }

    public Double validateAndGetSalary(String salary, String fieldName) throws InvalidFieldException {
        Double val = -1d;
        if (!isNull(salary, fieldName)) {
            try {
                val = Double.parseDouble(salary);
            } catch (NumberFormatException e) {
                throw new InvalidFieldException("salary should be a number, but is " + salary);
            }
            validateSalary(val);
        }
        return val;
    }

    public LocalDate validateAndGetStartDate(String startDateString, String fieldName) throws InvalidFieldException {
        if (!isNull(startDateString, fieldName)) {
            try {
                return LocalDate.parse(startDateString, validPattern);
            } catch (DateTimeParseException e) {
                throw new InvalidFieldException("Invalid date format, startDate can only be in the format yyyy-MM-dd or dd-MMM-yy");

            }
        }
        return null;
    }


    private boolean isNull(String value, String fieldName) throws InvalidFieldException {
        if (value == null) {
            throw new InvalidFieldException(String.format("%s cannot be null", fieldName));
        }
        return false;
    }

    public void validateSortField(String field) throws BadInputException {
        if (field == null || field.trim().equalsIgnoreCase("")) {
            throw new BadInputException("Sort field cannot be empty with just direction specified");
        }
        if (!FIELDS.contains(field)) {
            throw new BadInputException("Can sort based on one of the following columns " + FIELDS);
        }
    }

    public void validateSalary(Double salary) throws InvalidFieldException {
        if (salary < 0.0) {
            throw new InvalidFieldException(String.format("Invalid salary %s, salary should be greater than 0", salary));
        }
    }
}
