package com.axgrid.lock;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = "com.axgrid.lock")
@EnableScheduling
public class AxLockConfiguration {
}
