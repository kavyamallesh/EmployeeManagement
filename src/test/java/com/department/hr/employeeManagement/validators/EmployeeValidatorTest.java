package com.department.hr.employeeManagement.validators;

import com.department.hr.employeeManagement.exceptions.FileFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeValidatorTest {

    EmployeeValidator validator = new EmployeeValidator();

    @Test
    public void shouldThrowExceptionIfFileIsNotCSV(){
        MockMultipartFile file = new MockMultipartFile("file", "data.pdf", MediaType.APPLICATION_PDF_VALUE, "somethign".getBytes());
        assertThrows(FileFormatException.class, ()->validator.validateInputFile(file));
    }

    @Test
    public void shouldNotThrowExceptionIfFileIsaCSV() throws FileFormatException {
        MockMultipartFile file = new MockMultipartFile("file", "data.csv", "text/csv", "somethign".getBytes());
        validator.validateInputFile(file);
    }


}