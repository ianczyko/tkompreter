package com.anczykowski;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import com.anczykowski.lexer.Lexer;
import com.anczykowski.lexer.Source;
import com.anczykowski.lexer.TokenFilters;

public class Main {

    public static void main(String[] args) throws Exception {
        var outPrintStream = getPrintStream();

        try (var src = getSource(args)) {
            var lexer = new Lexer(src);
            lexer.stream()
                .filter(TokenFilters.getCommentFilter())
                .forEach(outPrintStream::println);
        }
    }

    private static PrintStream getPrintStream() {
        return new PrintStream(System.out, false, StandardCharsets.UTF_8);
    }

    private static Source getSource(String[] args) throws IOException {
        if (args.length == 1) {
            var path = args[0];
            var file = new File(path);
            var fileReader = new FileReader(file, StandardCharsets.UTF_8);
            return new Source(fileReader, file.getCanonicalPath());
        } else {
            var fileReader =  new InputStreamReader(System.in, StandardCharsets.UTF_8);
            return new Source(fileReader);
        }
    }
}