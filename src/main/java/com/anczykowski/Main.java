package com.anczykowski;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

public class Main {

    public static void main(String[] args) throws Exception {
        Reader reader;
        if (args.length == 1) {
            var path = args[0];
            var file = new File(path);
            reader = new FileReader(file);
        } else {
            reader = new InputStreamReader(System.in);
        }

        try (var src = new Source(reader)) {
            var lexer = new Lexer(src);
            lexer.stream()
                .filter(TokenFilters.getWhitespaceFilter())
                .forEach(System.out::println);
        }
    }
}