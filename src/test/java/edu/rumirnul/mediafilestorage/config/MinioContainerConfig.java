package edu.rumirnul.mediafilestorage.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@TestConfiguration
@Testcontainers
public class MinioContainerConfig extends GenericContainer<MinioContainerConfig> {
    private static final String ADMIN_ACCESS_KEY = "admin";
    private static final String ADMIN_SECRET_KEY = "12345678";
    private static final String BUCKET_NAME = "test-bucket";
    private static final int PORT = 9000;

    @Autowired
    private final MinioConfiguration minioConfiguration;



    public MinioContainerConfig(MinioConfiguration minioConfiguration) {
        this.minioConfiguration = minioConfiguration;
        GenericContainer minioServer = new GenericContainer("minio/minio")
                .withEnv("MINIO_ACCESS_KEY", ADMIN_ACCESS_KEY)
                .withEnv("MINIO_SECRET_KEY", ADMIN_SECRET_KEY)
                .withCommand("server /data")
                .withExposedPorts(PORT)
                .waitingFor(new HttpWaitStrategy()
                        .forPath("/minio/health/ready")
                        .forPort(PORT)
                        .withStartupTimeout(Duration.ofSeconds(10)));

        minioServer.start();

        Integer mappedPort = minioServer.getFirstMappedPort();
        org.testcontainers.Testcontainers.exposeHostPorts(mappedPort);
        String minioServerUrl = String.format("http://%s:%s", minioServer.getContainerIpAddress(), mappedPort);

        this.minioConfiguration.setUrl(minioServerUrl);
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "app.minio.bucket=" + BUCKET_NAME,
                    "app.minio.access-key=" + ADMIN_ACCESS_KEY,
                    "app.minio.secret-key=" + ADMIN_SECRET_KEY
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}