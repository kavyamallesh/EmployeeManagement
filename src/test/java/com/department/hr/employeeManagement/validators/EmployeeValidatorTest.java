package com.department.hr.employeeManagement.validators;

import com.department.hr.employeeManagement.exceptions.FileFormatException;
import com.department.hr.employeeManagement.exceptions.InvalidFieldException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    public void shouldThrowInvalidFieldExceptionWhenStartDateIsInInvalidFormat(){
        final InvalidFieldException invalidFieldException = assertThrows(InvalidFieldException.class, () -> validator.validateAndGetStartDate("02-02-1989","startDate"));
        assertThat(invalidFieldException.getMessage()).isEqualTo("Invalid date format, startDate can only be in the format yyyy-MM-dd or dd-MMM-yy");
    }

    @Test
    public void shouldNotThrowInvalidFieldExceptionWhenDateFormatIsYYYY_MM_DD() throws InvalidFieldException {
        validator.validateAndGetStartDate("1989-02-02", "startDate");
    }

   @Test
    public void shouldNotThrowInvalidFieldExceptionWhenDateFormatIsDD_MMM_YY() throws InvalidFieldException {
        validator.validateAndGetStartDate("02-Feb-89", "startDate");
    }

    @Test
    public void shouldThrowInvalidFieldExceptionWhenStartDateIsNullOrEmpty(){
        final InvalidFieldException invalidFieldExceptionWhenNull = assertThrows(InvalidFieldException.class, () -> validator.validateAndGetStartDate(null,"startDate"));
        assertThat(invalidFieldExceptionWhenNull.getMessage()).isEqualTo("startDate cannot be null");
        final InvalidFieldException invalidFieldExceptionWhenEmpty = assertThrows(InvalidFieldException.class, () -> validator.validateAndGetStartDate("","startDate"));
        assertThat(invalidFieldExceptionWhenEmpty.getMessage()).isEqualTo("Invalid date format, startDate can only be in the format yyyy-MM-dd or dd-MMM-yy");
    }

    @Test
    public void shouldThrowInvalidFieldExceptionWhenSalaryIsNotANumber(){
        final InvalidFieldException exception = assertThrows(InvalidFieldException.class, () -> validator.validateAndGetSalary("13555h", "salary"));
        assertThat(exception.getMessage()).isEqualTo("salary should be a number, but is 13555h");
    }

    @Test
    public void shouldThrowInvalidFieldExceptionWhenSalaryIsLesserThanZero(){
        final InvalidFieldException exception = assertThrows(InvalidFieldException.class, () -> validator.validateAndGetSalary("-12343.0", "salary"));
        assertThat(exception.getMessage()).isEqualTo("Invalid salary -12343.0, salary should be greater than 0");
    }



}