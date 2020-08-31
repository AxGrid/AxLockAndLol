package com.axgrid.lock;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AxLockConfiguration.class})
public @interface EnableAxLock {
}
