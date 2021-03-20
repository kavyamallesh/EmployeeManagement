package com.department.hr.employeeManagement.validators;

import com.department.hr.employeeManagement.exceptions.FileFormatException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class EmployeeValidator {

    public static final String CONTENT_TYPE_TEXT_CSV = "text/csv";

    public void validateInputFile(MultipartFile file) throws FileFormatException {
        if(!CONTENT_TYPE_TEXT_CSV.equalsIgnoreCase(file.getContentType())){
            throw new FileFormatException("The input file provided is not of a valid format. Please upload a csv file only");

        }
    }

}
