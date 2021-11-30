package edu.prometheus.poc;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

@SpringBootApplication
@RestController
public class Main implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    private MeterRegistry meterRegistry;

    @Autowired
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public Random random() {
        log.info("Creating random");
        return new Random();
    }

    @Bean
    public Counter counterOne() {
        log.info("Creating custom counter 1");
        return meterRegistry.counter("my-custom-counter", "type", "one");
    }

    @Bean
    public Counter counterTwo() {
        log.info("Creating custom counter 2");
        return meterRegistry.counter("my-custom-counter", "type", "two");
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        log.info("String initialized");

        log.info("Initializing HTTP client for load generating");
        final var client = HttpClient.newHttpClient();
        final var request = HttpRequest.newBuilder(URI.create("http://localhost:8080/service/ping")).GET().build();

        log.info("Starting to send service calls...");
        for (double i = 0; i < Double.MAX_VALUE; i+=0.01) {
            try {
                client.send(request, HttpResponse.BodyHandlers.discarding());
                Thread.sleep(Math.abs((long) (Math.sin(i) * 100)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @GetMapping("/service/ping")
    public void ping() {
        log.trace("Handling ping request");
        final var randomFloat = random().nextFloat(1.0f);
        if (randomFloat < 0.01) {
            counterOne().increment();
            log.info("1st counter: {}", Double.valueOf(counterOne().count()).intValue());
        }
        if (randomFloat > 0.95) {
            counterTwo().increment();
            log.info("2nd counter: {}", Double.valueOf(counterTwo().count()).intValue());
        }
    }

}
