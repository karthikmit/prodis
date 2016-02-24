package com.buyhatke.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.stream.Stream;

/**
 * FileUtils
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static String readFileToString(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            final Stream<String> lines = reader.lines();
            StringBuilder builder = new StringBuilder();
            lines.forEach(builder::append);
            reader.close();
            return builder.toString();
        } catch (FileNotFoundException e) {
            logger.error("FileUtils: File not found for reading :: " + file.getAbsolutePath());
            throw e;
        }
    }

    public static void writeStringToFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content, 0, content.length());
            writer.flush();
            writer.close();
        }
    }
}
