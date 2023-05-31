package edu.rumirnul.mediafilestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MediaFileStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediaFileStorageApplication.class, args);
	}

}
