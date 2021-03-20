package com.department.hr.employeeManagement.validators;

import com.department.hr.employeeManagement.exceptions.FileFormatException;
import com.department.hr.employeeManagement.exceptions.InvalidFieldException;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class EmployeeValidator {

    public static final String CONTENT_TYPE_TEXT_CSV = "text/csv";
    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DD_MMMM_YY = DateTimeFormatter.ofPattern("dd-MMM-yy");


    public void validateInputFile(MultipartFile file) throws FileFormatException {
        if (!CONTENT_TYPE_TEXT_CSV.equalsIgnoreCase(file.getContentType())) {
            throw new FileFormatException("The input file provided is not of a valid format. Please upload a csv file only");

        }
    }

    public String validateAndGetId(String id, String fieldName) throws InvalidFieldException {
        if (isNull(id, "id") || id.matches("[A-Za-z0-9]")) {
            throw new InvalidFieldException(String.format("Invalid id %s, only alphanumeric values are allowed for id"));
        }
        return id;
    }

    public String validateAndGetLogin(String login, String fieldName) throws InvalidFieldException {
        if (isNull(login, fieldName) || !login.matches("[A-Za-z0-9]")) {
            throw new InvalidFieldException(String.format("Invalid login %s, only alphanumeric values are allowed for login"));
        }
        return login;
    }

    public String validateAndGetName(String name, String fieldName) throws InvalidFieldException {
        isNull(name, fieldName);
        return name;
    }

    public Double validateAndGetSalary(String salary, String fieldName) throws InvalidFieldException {
        final Double val;
        if (!isNull(salary, fieldName)) {
            try {
                val = Double.parseDouble(salary);
            } catch (NumberFormatException e) {
                throw new InvalidFieldException("salary should be a number, but is "+salary);
            }

            if(val < 0.0){
                throw new InvalidFieldException(String.format("Invalid salary %s, salary should be greater than 0", salary));
            }
        }
        return 1d;
    }

    public LocalDate validateAndGetStartDate(String startDateString, String fieldName) throws InvalidFieldException {
        if (!isNull(startDateString, fieldName)) {
            try {
                return LocalDate.parse(startDateString, YYYY_MM_DD);
            } catch (DateTimeParseException e) {
                try {
                    return LocalDate.parse(startDateString, DD_MMMM_YY);
                } catch (DateTimeParseException ex) {
                    throw new InvalidFieldException("Invalid date format, startDate can only be in the format yyyy-MM-dd or dd-MMM-yy");
                }
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

}
