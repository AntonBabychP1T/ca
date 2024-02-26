package service.carsharing.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import service.carsharing.dto.cars.CarRequestDto;
import service.carsharing.dto.cars.CarResponseDto;
import service.carsharing.model.Car;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {
    private static final Long VALID_ID = 1L;
    private static final String VALID_MODEL = "Valid Model";
    private static final String VALID_BRAND = "Valid Brand";
    private static final Car.Type VALID_TYPE = Car.Type.CUV;
    private static final String VALID_STRING_TYPE = "SUV";
    private static final Integer VALID_INVENTORY = 2;
    private static final BigDecimal VALID_FEE = BigDecimal.TEN;
    private static final boolean NOT_DELETED = false;

    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection, new ClassPathResource("database/cars/add-two-cars.sql")
            );
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) throws SQLException {
        teardown(dataSource);
    }

    static void teardown(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection, new ClassPathResource("database/cars/delete-all-from-cars.sql")
            );
        }
    }

    private Car createValidCar() {
        Car car = new Car();
        car.setId(VALID_ID);
        car.setInventory(VALID_INVENTORY);
        car.setFee(VALID_FEE);
        car.setDeleted(NOT_DELETED);
        car.setModel(VALID_MODEL);
        car.setBrand(VALID_BRAND);
        car.setType(VALID_TYPE);
        return car;
    }

    private CarRequestDto createValidCarRequestDto() {
        return new CarRequestDto(
                VALID_MODEL,
                VALID_BRAND,
                VALID_STRING_TYPE,
                VALID_INVENTORY,
                VALID_FEE
        );
    }

    private CarResponseDto createValidCarResponseDto() {
        return new CarResponseDto(
                VALID_ID,
                VALID_MODEL,
                VALID_BRAND,
                VALID_STRING_TYPE,
                VALID_INVENTORY,
                VALID_FEE
        );
    }

    @WithMockUser(username = "manager", roles = "MANAGER")
    @Test
    @Sql(
            scripts = "classpath:database/cars/delete-default-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Verify createCar() method work")
    public void createCar_ValidRequestDto_ReturnCarResponseDto() throws Exception {
        CarRequestDto requestDto = createValidCarRequestDto();
        CarResponseDto expected = createValidCarResponseDto();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        CarResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), CarResponseDto.class);

        assertNotNull(actual);
        assertNotNull(actual.id());
        reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @Test
    @DisplayName("Verify getAllCars() method work")
    public void getAllCars_ValidCarInDb_ReturnAllCarsInDb() throws Exception {
        CarResponseDto firstCar = new CarResponseDto(
                1L,
                "First model",
                "Audi",
                "SEDAN",
                2,
                BigDecimal.valueOf(149.99)
        );
        CarResponseDto secondCar = new CarResponseDto(
                2L,
                "Second model",
                "Nissan",
                "SUV",
                3,
                BigDecimal.valueOf(99.99)
        );
        List<CarResponseDto> expected = List.of(firstCar, secondCar);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CarResponseDto[] actual = objectMapper
                .readValue(result.getResponse().getContentAsByteArray(), CarResponseDto[].class);
        assertEquals(2, actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @Test
    @DisplayName("Verify getCarById() method work")
    public void getCarById_ValidId_CarResponseDto() throws Exception {
        CarResponseDto expected = new CarResponseDto(
                1L,
                "First model",
                "Audi",
                "SEDAN",
                2,
                BigDecimal.valueOf(149.99)
        );
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/cars/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), CarResponseDto.class);
        assertNotNull(actual);
        reflectionEquals(expected, actual);
    }

    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @Sql(
            scripts = "classpath:database/cars/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/cars/delete-default-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Verify deleteCar() method works")
    public void deleteCar_ValidId_ReturnNoContentStatus() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/cars/", 3L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @Sql(
            scripts = "classpath:database/cars/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/cars/delete-default-car.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Verify updateCar() method works")
    public void updateCar_ValidIdAndRequestDto_UpdatedCarInDb() throws Exception {
        CarRequestDto requestDto = new CarRequestDto(
                "new model",
                "new brand",
                VALID_STRING_TYPE,
                1,
                VALID_FEE
        );
        CarResponseDto expected = new CarResponseDto(
                3L,
                "new model",
                "new brand",
                VALID_STRING_TYPE,
                1,
                VALID_FEE
        );
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("api/cars/{id}", 3L)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), CarResponseDto.class);
        assertNotNull(actual);
        reflectionEquals(expected, actual);

    }
}
