package com.demo.springbatch.runner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CsvGenerator {

    public static void main(String[] args) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.csv"))) {

            writer.write("name,email\n"); // header

            for (int i = 1; i <= 1_000_000; i++) {
                writer.write("User" + i + ",user" + i + "@test.com\n");
            }
        }

        System.out.println("CSV Generated!");
    }
}