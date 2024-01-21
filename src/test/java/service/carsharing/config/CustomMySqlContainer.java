package service.carsharing.config;

import org.testcontainers.containers.MySQLContainer;

public class CustomMySqlContainer extends MySQLContainer<CustomMySqlContainer> {
    private static final String BD_IMAGE = "mysql:8";
    private static CustomMySqlContainer mySqlContainer;

    private CustomMySqlContainer() {
        super(BD_IMAGE);
    }

    public static synchronized CustomMySqlContainer getInstance() {
        if (mySqlContainer == null) {
            mySqlContainer = new CustomMySqlContainer();
        }
        return mySqlContainer;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {

    }
}
