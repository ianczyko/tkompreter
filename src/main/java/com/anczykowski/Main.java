package com.anczykowski;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.anczykowski.lexer.Lexer;
import com.anczykowski.lexer.Source;
import com.anczykowski.lexer.TokenFilters;

public class Main {

    public static void main(String[] args) throws Exception {
        Reader reader;
        if (args.length == 1) {
            var path = args[0];
            var file = new File(path);
            reader = new FileReader(file, StandardCharsets.UTF_8);
        } else {
            reader = new InputStreamReader(System.in);
        }

        PrintStream out = new PrintStream(System.out, false, StandardCharsets.UTF_8);

        try (var src = new Source(reader)) {
            var lexer = new Lexer(src);
            lexer.stream()
                .filter(TokenFilters.getWhitespaceFilter())
                .forEach(out::println);
        }
    }
}