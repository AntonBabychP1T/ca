package service.carsharing.repository;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import service.carsharing.model.Car;

@DataJpaTest
@Sql(
        scripts = "classpath:database/cars/add-default-car.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "classpath:database/cars/delete-default-car.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CarRepositoryTest {
    private static final Long VALID_ID = 3L;
    private static final String VALID_MODEL = "Valid Model";
    private static final String VALID_BRAND = "Valid Brand";
    private static final Car.Type VALID_TYPE = Car.Type.CUV;
    private static final Integer VALID_INVENTORY = 2;
    private static final BigDecimal VALID_FEE = BigDecimal.TEN;
    private static final boolean NOT_DELETED = false;
    @Autowired
    private CarRepository carRepository;

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

    @Test
    public void findCarById_ValidId_ValidCar() {
        Car expected = createValidCar();
        Car actual = carRepository.findById(VALID_ID).get();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);

    }
}
