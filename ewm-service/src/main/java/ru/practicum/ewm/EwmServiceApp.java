package ru.practicum.ewm;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.practicum.stats.config.ClientConfig;

@SpringBootApplication(scanBasePackages = {"ru.practicum.ewm", "ru.practicum.stats"})
@Import(ClientConfig.class)
public class EwmServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(EwmServiceApp.class, args);
    }
}
