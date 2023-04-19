package com.anczykowski;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.lexer.LexerFiltered;
import com.anczykowski.lexer.LexerImpl;
import com.anczykowski.lexer.Source;
import com.anczykowski.lexer.TokenFilters;
import com.anczykowski.parser.Parser;
import com.anczykowski.parser.visitors.PrinterVisitor;

public class Main {

    public static void main(String[] args) throws Exception {
        var outPrintStream = getPrintStream();

        var errorModule = new ErrorModule();

        try (var src = getSource(args, errorModule)) {
            var lexer = new LexerImpl(src, errorModule);
            var lexerFiltered = new LexerFiltered(lexer, TokenFilters.getCommentFilter());

            var parser = new Parser(lexerFiltered, errorModule);

            var program = parser.parse();
            var printer = new PrinterVisitor(outPrintStream);

            program.accept(printer);

            errorModule.printErrors(outPrintStream);
        }
    }

    private static PrintStream getPrintStream() {
        return new PrintStream(System.out, false, StandardCharsets.UTF_8);
    }

    private static Source getSource(String[] args, ErrorModule errorModule) throws IOException {
        if (args.length == 1) {
            var path = args[0];
            var file = new File(path);
            var fileReader = new FileReader(file, StandardCharsets.UTF_8);
            return new Source(errorModule, fileReader, file.getCanonicalPath());
        } else {
            var fileReader =  new InputStreamReader(System.in, StandardCharsets.UTF_8);
            return new Source(errorModule, fileReader);
        }
    }
}