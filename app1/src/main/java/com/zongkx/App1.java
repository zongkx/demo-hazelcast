package com.zongkx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SpringBootApplication
@EnableAsync
@RestController
@RequiredArgsConstructor
public class App1 {
    public static void main(String[] args) {
        SpringApplication.run(App1.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        CacheUtil.set(TestCache.TEST, "id", "1");
        return args -> {
        };
    }
}
