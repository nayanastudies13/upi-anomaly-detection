package com.upianomaly.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Command-line entry point. No web server, no external services - running
 * this class is the entire application.
 *
 * Run with:  java -cp out com.upianomaly.engine.Main
 * (see README.md for the full build/run commands)
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
