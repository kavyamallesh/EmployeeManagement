package com.department.hr.employeeManagement.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getEmployeesShouldReturnDefaultSuccessMsg() throws Exception {

        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello World!".getBytes()
        );

        this.mockMvc
                .perform(multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Successful"));
    }

    @Test
    void getEmployeesShouldReturnBadInputErrorIfInputFileIsNotPassed() throws Exception {
        this.mockMvc
                .perform(multipart("/upload"))
                .andExpect(status().is(400));
    }

}