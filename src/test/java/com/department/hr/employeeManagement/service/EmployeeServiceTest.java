package com.department.hr.employeeManagement.service;

import com.department.hr.employeeManagement.domain.OffsetBasedPageRequest;
import com.department.hr.employeeManagement.domain.SortedUnpaged;
import com.department.hr.employeeManagement.exceptions.BadInputException;
import com.department.hr.employeeManagement.repository.EmployeeRepository;
import com.department.hr.employeeManagement.validators.EmployeeValidator;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeValidator validator;

    @Mock
    EmployeeRepository repository;

    @InjectMocks
    EmployeeService service;

    @Test
    void shouldIgnoreCommentedLines() throws IOException {
        String content = "id,login, name,salary,startDate\n" +
                "e1,j1, Jooni,134,2001-11-19,\n" +
                "#e2,h1, Hoon,404.5,2005-08-11,\n" +
                "e3,hy, Hyuk,8844.999,2020-03-12";
        MockMultipartFile file = new MockMultipartFile("something", "something.csv", "text/csv", content.getBytes());
        final List<CSVRecord> records = service.processCSVFile(file);
        assertThat(records.size()).isEqualTo(2);
        final List<String> ids = records.stream().map(r -> r.get("id")).collect(Collectors.toList());
        assertFalse(ids.contains("e2"));
        assertTrue(ids.containsAll(Arrays.asList("e1", "e3")));
    }

    @Test
    void shouldReturnSortOrderAndDirection() throws BadInputException {
        String inputParam = "startDate-asc,salary-desc";
        final List<Sort.Order> sortOrder = service.getSortOrderList(inputParam);
        assertThat(sortOrder).isEqualTo(Arrays.asList(new Sort.Order(Sort.Direction.ASC, "startDate"), new Sort.Order(Sort.Direction.DESC, "salary")));
    }

    @Test
    void shouldReturnDefaultDirectionAscIfDirectionNotSpecified() throws BadInputException {
        String inputParam = "startDate";
        final List<Sort.Order> sortOrder = service.getSortOrderList(inputParam);
        assertThat(sortOrder).isEqualTo(Arrays.asList(new Sort.Order(Sort.Direction.ASC, "startDate")));
    }

    @Test
    void shouldReturnDefaultDirectionAscIfDirectionIsSpecifiedIncorrectly() throws BadInputException {
        String inputParam = "salary-descending";
        final List<Sort.Order> sortOrder = service.getSortOrderList(inputParam);
        assertThat(sortOrder).isEqualTo(Arrays.asList(new Sort.Order(Sort.Direction.ASC, "salary")));
    }

    @Test
    void shouldThrowBadInputExceptionIfSortDirectionIsSpecifiedButNotField() throws BadInputException {
        doThrow(BadInputException.class).when(validator).validateSortField("");
        String inputParam = "-descending";
        assertThrows(RuntimeException.class, () -> service.getSortOrderList(inputParam));
    }

    @Test
    void shouldPassTheInstanceOfOffsetPageableRequestIfLimitIsGreaterThan0() throws BadInputException {
        service.fetchEmployees(1d, 3000d, 1, 12, "id-asc");

        final ArgumentCaptor<Double> minSalaryArgCaptor = ArgumentCaptor.forClass(Double.class);
        final ArgumentCaptor<Double> maxSalaryArgCaptor = ArgumentCaptor.forClass(Double.class);
        final ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(repository).findBySalaryGreaterThanEqualAndSalaryLessThan(minSalaryArgCaptor.capture(), maxSalaryArgCaptor.capture(), pageableArgumentCaptor.capture());

        assertEquals(1d, minSalaryArgCaptor.getValue());
        assertEquals(3000d, maxSalaryArgCaptor.getValue());
        assertEquals(OffsetBasedPageRequest.class, pageableArgumentCaptor.getValue().getClass());

    }

    @Test
    void shouldPassTheInstanceOfSortedUnpagedIfLimitIs0() throws BadInputException {
        service.fetchEmployees(1d, 3000d, 1, 0, "id-asc");

        final ArgumentCaptor<Double> minSalaryArgCaptor = ArgumentCaptor.forClass(Double.class);
        final ArgumentCaptor<Double> maxSalaryArgCaptor = ArgumentCaptor.forClass(Double.class);
        final ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(repository).findBySalaryGreaterThanEqualAndSalaryLessThan(minSalaryArgCaptor.capture(), maxSalaryArgCaptor.capture(), pageableArgumentCaptor.capture());

        assertEquals(SortedUnpaged.class, pageableArgumentCaptor.getValue().getClass());

    }

}