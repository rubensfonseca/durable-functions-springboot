package com.rfonseca.example;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class EcoBusDailyBatchApp {
   strictfp
    public static void main(String[] args) {
        
		ApplicationContext ctx = SpringApplication.run(EcoBusDailyBatchApp.class, args);

		System.out.println("Starting daily batch at ");

	}
}
