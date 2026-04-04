package com.demo.springbatch.runner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvGenerator {

    public static void main(String[] args) throws IOException {

        int totalRecords = 1_000_000;
//        int totalRecords = 100; // for testing, use smaller number
        int numberOfFiles = 4; // number of partitions
        int recordsPerFile = totalRecords / numberOfFiles;

        // Ensure input directory exists
        File dir = new File("input");
        if (!dir.exists()) {
            dir.mkdir();
        }

        for (int fileIndex = 1; fileIndex <= numberOfFiles; fileIndex++) {

            String fileName = "input/users_" + fileIndex + ".csv";

            BufferedWriter writer = null;

            try {
                writer = new BufferedWriter(new FileWriter(fileName));

                writer.write("name,email\n"); // header

                int start = (fileIndex - 1) * recordsPerFile + 1;
                int end = fileIndex * recordsPerFile;

                for (int i = start; i <= end; i++) {
                    writer.write("User" + i + ",user" + i + "@test.com\n");
                }

                System.out.println("CSV Generated: " + fileName);

            } finally {
                if (writer != null) {
                    writer.close(); // JDK 8 safe close
                }
            }
        }

        System.out.println("CSV Files Generated Successfully!");
    }
}