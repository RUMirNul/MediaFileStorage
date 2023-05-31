package edu.rumirnul.mediafilestorage.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("edu.rumirnul.mediafilestorage.entity")
@EnableJpaRepositories(basePackages = {"edu.rumirnul.mediafilestorage.repository"})
@ComponentScan({"edu.rumirnul.mediafilestorage.repository"})
public class SystemTestingJpaConfig {
}
