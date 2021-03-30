package com.department.hr.employeeManagement.controllers;

import com.department.hr.employeeManagement.entity.Employee;
import com.department.hr.employeeManagement.repository.EmployeeRepository;
import com.department.hr.employeeManagement.vo.Results;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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


    @Nested
    class testUploadFunctionality {
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

            mockMvc
                    .perform(multipart("/users/upload").file(file))
                    .andExpect(status().is(201))
                    .andExpect(content().string("Data created or uploaded successfully"));

            assertThat(repository.findAll().size()).isEqualTo(totalEntities + 2);
        }

        @Test
        void shouldReturn200WhenNoDataModified() throws Exception {
            int totalEntities = repository.findAll().size();
            String content = "id,login, name,salary,startDate\n" +
                    "#e3,hy, Hyuk,8844.999,2020-03-12";
            MockMultipartFile file
                    = new MockMultipartFile(
                    "file",
                    "hello.txt",
                    "text/csv",
                    content.getBytes()
            );

            mockMvc
                    .perform(multipart("/users/upload").file(file))
                    .andExpect(status().is(200))
                    .andExpect(content().string("Success but no data updated"));

            assertThat(repository.findAll().size()).isEqualTo(totalEntities);
        }

        @Test
        void shouldReturnBadRequestWhenFileContainsDuplicateIds() throws Exception {
            int totalEntities = repository.findAll().size();
            String content = "id,login, name,salary,startDate\n" +
                    "e1,j1, Jooni,134,2001-11-19,\n" +
                    "e1,h1, Hoon,404.5,2005-08-11,\n" +
                    "e3,hy, Hyuk,8844.999,2020-03-12";
            MockMultipartFile file
                    = new MockMultipartFile(
                    "file",
                    "hello.txt",
                    "text/csv",
                    content.getBytes()
            );

            mockMvc
                    .perform(multipart("/users/upload").file(file))
                    .andExpect(status().is(400))
                    .andExpect(content().string("Duplicate ids detected - [e1]"));

            assertThat(repository.findAll().size()).isEqualTo(totalEntities);
        }

        @Test
        void shouldReturnBadRequestWhenFileContainsDuplicateLogins() throws Exception {
            int totalEntities = repository.findAll().size();
            String content = "id,login, name,salary,startDate\n" +
                    "e1,j1, Jooni,134,2001-11-19,\n" +
                    "e2,j1, Hoon,404.5,2005-08-11,\n" +
                    "e3,hy, Hyuk,8844.999,2020-03-12";
            MockMultipartFile file
                    = new MockMultipartFile(
                    "file",
                    "hello.txt",
                    "text/csv",
                    content.getBytes()
            );

            mockMvc
                    .perform(multipart("/users/upload").file(file))
                    .andExpect(status().is(400))
                    .andExpect(content().string("Duplicate login ids detected - [j1]"));

            assertThat(repository.findAll().size()).isEqualTo(totalEntities);
        }

        @Test
        void shouldReturnBadRequestWhenFileContainsNonUniqueLogins() throws Exception {
            int totalEntities = repository.findAll().size();
            String content = "id,login, name,salary,startDate\n" +
                    "e1,j1, Jooni,134,2001-11-19,\n" +
                    "e2,h1, Hoon,404.5,2005-08-11,\n" +
                    "e3,harry1, Hyuk,8844.999,2020-03-12";
            MockMultipartFile file
                    = new MockMultipartFile(
                    "file",
                    "hello.txt",
                    "text/csv",
                    content.getBytes()
            );

            mockMvc
                    .perform(multipart("/users/upload").file(file))
                    .andExpect(status().is(400))
                    .andExpect(content().string("Login id is not unique"));

            assertThat(repository.findAll().size()).isEqualTo(totalEntities);
        }

        @Test
        void getEmployeesShouldReturnBadInputErrorIfInputFileIsNotPassed() throws Exception {
            mockMvc
                    .perform(multipart("/users/upload"))
                    .andExpect(status().is(400));
        }
    }

    @Nested
    class testCRUDOperations {
        @BeforeEach
        void setup() {
            repository.deleteAll();
            repository.save(employee);
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
            mockMvc
                    .perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(input)
                            .characterEncoding("utf-8"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Successfully created"));

            assertThat(repository.findAll().size()).isEqualTo(totalEntities + 1);

        }

        @Test
        void shouldUpdateEmployeeWhenCorrectInputIsGiven() throws Exception {
            String empId = "emp0001";
            String input = "{\n" +
                    "\"name\": \"Entwickeln Sie mit Vergnügen\",\n" +
                    "\"login\": \"hpotter2\",\n" +
                    "\"salary\": 80000, \n" +
                    "\"startDate\": \"2001-11-16\"\n" +
                    "}";
            Optional<Employee> employee = repository.findById(empId);
            assertTrue(employee.isPresent());
            assertThat(employee.get().getSalary()).isEqualTo(1200.5);
            mockMvc
                    .perform(put("/users/" + empId).contentType(MediaType.APPLICATION_JSON)
                            .content(input)
                            .characterEncoding("utf-8"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Successfully updated"));

            Optional<Employee> employeeUpdated = repository.findById(empId);
            assertTrue(employeeUpdated.isPresent());
            assertThat(employeeUpdated.get().getSalary()).isEqualTo(80000);
            assertThat(employeeUpdated.get().getName()).isEqualTo("Entwickeln Sie mit Vergnügen");
        }

        @Test
        void shouldDeleteEmployeeWhenCorrectIdIsGiven() throws Exception {
            String empId = "emp0001";
            Optional<Employee> employee = repository.findById(empId);
            assertTrue(employee.isPresent());

            mockMvc
                    .perform(delete("/users/" + empId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Successfully deleted"));

            Optional<Employee> employeeUpdated = repository.findById(empId);
            assertFalse(employeeUpdated.isPresent());
        }

        @Test
        void shouldGetEmployeeWhenCorrectIdIsGiven() throws Exception {
            String empId = "emp0001";
            mockMvc
                    .perform(get("/users/" + empId))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(employee)));
        }

    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class validateGetEmployeesService {
        @BeforeAll
        void beforeEach() throws Exception {
            String content = "id,login,name,salary,startDate\n" +
                    "e0001,hpotter,Harry Potter,1234.00,16-Nov-01\n" +
                    "e0002,rwesley,Ron Weasley,19234.50,2001-11-16\n" +
                    "e0003,ssnape,Severus Snape,4000.0,2001-11-16\n" +
                    "e0004,rhagrid,Rubeus Hagrid,3999.999,16-Nov-01\n" +
                    "e0005,voldemort,Lord Voldemort,523.4,17-Nov-01\n" +
                    "e0006,gwesley,Ginny Weasley,4000.004,18-Nov-01\n" +
                    "e0007,hgranger,Hermione Granger,0.0,2001-11-18\n" +
                    "e0008,adumbledore,Albus Dumbledore,34.23,2001-11-19";
            MockMultipartFile file
                    = new MockMultipartFile(
                    "file",
                    "hello.txt",
                    "text/csv",
                    content.getBytes()
            );

            mockMvc
                    .perform(multipart("/users/upload").file(file))
                    .andExpect(status().is(201))
                    .andExpect(content().string("Data created or uploaded successfully"));
        }

        @Test
        void shouldReturnAllEmployeesByDefaultAndSortedByIdAscending() throws Exception {
            int size = repository.findAll().size();
            assertEquals(size, 8);
            MvcResult result = mockMvc
                    .perform(get("/users"))
                    .andExpect(status().is(200))
                    .andReturn();

            String contentAsString = result.getResponse().getContentAsString();
            assertNotNull(contentAsString);

            Results results = mapper.readValue(contentAsString, Results.class);
            List<Employee> employees = results.getResults();
            //entities with salary >=4000 is filtered by default
            assertThat(employees.size()).isEqualTo(5);
            assertFalse(employees.stream().map(Employee::getSalary).anyMatch(s -> s >= 40000));

            List<String> ids = employees.stream().map(Employee::getId).collect(Collectors.toList());
            List<String> sortedIds = ids.stream().sorted().collect(Collectors.toList());
            assertThat(ids).isEqualTo(sortedIds);
        }

        @Test
        void shouldReturnOnlySpecifiedPage() throws Exception {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("offset", "0");
            queryParams.add("limit", "2");
            queryParams.add("maxSalary", "200000");

            MvcResult page1 = mockMvc
                    .perform(get("/users")
                            .queryParams(queryParams))
                    .andExpect(status().is(200))
                    .andReturn();



            String page1Response = page1.getResponse().getContentAsString();
            Results page1Results = mapper.readValue(page1Response, Results.class);
            List<Employee> page1Employees = page1Results.getResults();
            assertEquals(page1Employees.size(), 2);
            List<String> ids = page1Employees.stream().map(Employee::getId).collect(Collectors.toList());
            assertThat(ids).isEqualTo(Arrays.asList("e0001", "e0002"));

            MultiValueMap<String, String> queryParams2 = new LinkedMultiValueMap<>();
            queryParams2.add("offset", "2");
            queryParams2.add("limit", "2");

            MvcResult page2 = mockMvc
                    .perform(get("/users")
                            .queryParams(queryParams2))
                    .andExpect(status().is(200))
                    .andReturn();
            String page2Response = page2.getResponse().getContentAsString();
            Results page2Results = mapper.readValue(page2Response, Results.class);
            List<Employee> page2Employees = page2Results.getResults();
            assertEquals(page2Employees.size(), 2);
            List<String> page2Ids = page2Employees.stream().map(Employee::getId).collect(Collectors.toList());
            assertThat(page2Ids).isEqualTo(Arrays.asList("e0005", "e0007"));

        }

        @Test
        void shouldSortAsPerSortOrder() throws Exception {
            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("maxSalary", "200000");
            queryParams.add("orderByfieldAndDirection", "startDate-desc,salary-asc");

            MvcResult page1 = mockMvc
                    .perform(get("/users")
                            .queryParams(queryParams))
                    .andExpect(status().is(200))
                    .andReturn();


            String page1Response = page1.getResponse().getContentAsString();
            Results page1Results = mapper.readValue(page1Response, Results.class);
            List<Employee> page1Employees = page1Results.getResults();
            List<String> ids = page1Employees.stream().map(Employee::getId).collect(Collectors.toList());
            assertThat(ids).isEqualTo(Arrays.asList("e0008", "e0007", "e0006", "e0005", "e0001", "e0004", "e0003", "e0002"));
        }

    }

}