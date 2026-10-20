package com.back;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AppTestRunner {

    public static String run(String input) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayInputStream in = new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8)
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            System.setIn(in);
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

            new App().run();

            return out.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }
}