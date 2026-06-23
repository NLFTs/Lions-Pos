package com.dak.spravel;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import java.util.TimeZone;

/**
 * Spring Boot main entry point with JPA auditing and timezone configuration.
 */
@SpringBootApplication
@Slf4j
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class MainApplication implements ApplicationRunner {

	@Value("${app.version}")
	private String appVersion;

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

    @Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		log.info("[x] Application started [App Version {}]", appVersion);
	}

    @PostConstruct
	public void init() {
		log.info("[x] Set default time zone to Asia/Jakarta");

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Jakarta"));
	}

    @Bean
	public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
		log.info("[x] Set Jackson time zone to default");
		return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
	}
}
