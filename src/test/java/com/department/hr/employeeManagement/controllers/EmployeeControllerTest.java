package com.department.hr.employeeManagement.controllers;

import com.department.hr.employeeManagement.entity.Employee;
import com.department.hr.employeeManagement.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository repository;

    private Employee employee = new Employee("emp0001", "harry1", "Harry Potter", 1200.50, LocalDate.of(2012, 12, 26));

    private ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setup() {
        repository.deleteAll();
        repository.save(employee);
    }

    @Test
    void shouldUploadDataInFileToRepositoryWhenInputIsValid() throws Exception {
        int totalEntities = repository.findAll().size();
        String content = "id,login, name,salary,startDate\n" +
                "e1,j1, Jooni,134,2001-11-19,\n" +
                "#e2,h1, Hoon,404.5,2005-08-11,\n" +
                "e3,hy, Hyuk,8844.999,2020-03-12";
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/csv",
                content.getBytes()
        );

        this.mockMvc
                .perform(multipart("/users/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Successful"));

        assertThat(repository.findAll().size()).isEqualTo(totalEntities+2);
    }

    @Test
    void getEmployeesShouldReturnBadInputErrorIfInputFileIsNotPassed() throws Exception {
        this.mockMvc
                .perform(multipart("/users/upload"))
                .andExpect(status().is(400));
    }

    @Test
    void createEmployeeShouldCreateEntityWhenCorrectInputIsGiven() throws Exception {
        int totalEntities = repository.findAll().size();
        String input = "{\n" +
                "\"id\": \"emp00012\",\n" +
                "\"name\": \"Harry Potter\",\n" +
                "\"login\": \"hpotter2\",\n" +
                "\"salary\": 1234.00,\n" +
                "\"startDate\": \"2001-11-16\"\n" +
                "}";
        this.mockMvc
                .perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(input)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully created"));

        assertThat(repository.findAll().size()).isEqualTo(totalEntities+1);

    }

    @Test
    void shouldUpdateEmployeeWhenCorrectInputIsGiven() throws Exception {
        String empId = "emp0001";
        String input = "{\n" +
                "\"id\": \"emp0001\",\n" +
                "\"name\": \"Harry Potter\",\n" +
                "\"login\": \"hpotter2\",\n" +
                "\"salary\": 80000, \n" +
                "\"startDate\": \"2001-11-16\"\n" +
                "}";
        Optional<Employee> employee = repository.findById(empId);
        assertTrue(employee.isPresent());
        assertThat(employee.get().getSalary()).isEqualTo(1200.5);
        this.mockMvc
                .perform(put("/users/" + empId).contentType(MediaType.APPLICATION_JSON)
                        .content(input)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated"));

        Optional<Employee> employeeUpdated = repository.findById(empId);
        assertTrue(employeeUpdated.isPresent());
        assertThat(employeeUpdated.get().getSalary()).isEqualTo(80000);
    }

    @Test
    void shouldDeleteEmployeeWhenCorrectIdIsGiven() throws Exception {
        String empId = "emp0001";
        Optional<Employee> employee = repository.findById(empId);
        assertTrue(employee.isPresent());

        this.mockMvc
                .perform(delete("/users/" + empId))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully deleted"));

        Optional<Employee> employeeUpdated = repository.findById(empId);
        assertFalse(employeeUpdated.isPresent());
    }

    @Test
    void shouldGetEmployeeWhenCorrectIdIsGiven() throws Exception {
        String empId = "emp0001";
        this.mockMvc
                .perform(get("/users/" + empId))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(employee)));
    }

    @Test
    void shouldGetAllEmployees() throws Exception {
        Map result = new HashMap<>();
        result.put("response", Collections.singletonList(employee));
        this.mockMvc
                .perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(result)));
    }

}