package com.dak.spravel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
// "myAuditor" ini harus sama dengan nama di @Component milik AuditorAwareImpl.java
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl") 
public class JpaConfig {
}