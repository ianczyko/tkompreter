package com.anczykowski;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.exceptions.InterpreterException;
import com.anczykowski.errormodule.exceptions.ParserException;
import com.anczykowski.lexer.LexerFiltered;
import com.anczykowski.lexer.LexerImpl;
import com.anczykowski.lexer.Source;
import com.anczykowski.lexer.TokenFilters;
import com.anczykowski.parser.Parser;
import com.anczykowski.visitors.InterpreterVisitor;
import com.anczykowski.visitors.PrinterVisitor;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {
    private static final boolean isDebug = true;

    public static void main(String[] args) throws Exception {
        var outPrintStream = getPrintStream();

        var errorModule = new ErrorModule();

        try (var src = getSource(args, errorModule)) {
            var lexer = new LexerImpl(src, errorModule);
            var lexerFiltered = new LexerFiltered(lexer, TokenFilters.getCommentFilter());

            var parser = new Parser(lexerFiltered, errorModule);

            try {
                outPrintStream.println("#### Printer ####");
                var program = parser.parse();
                var printer = new PrinterVisitor(outPrintStream);
                program.accept(printer);

                outPrintStream.println("#### Interpreter ####");
                var interpreter = new InterpreterVisitor(errorModule, outPrintStream);
                program.accept(interpreter);
            } catch (ParserException | InterpreterException e) {
                if (isDebug) {
                    outPrintStream.println("#### Debug StackTrace ####");
                    e.printStackTrace(outPrintStream);
                }
            }
            finally {
                errorModule.printErrors(outPrintStream);
            }

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
            var fileReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
            return new Source(errorModule, fileReader);
        }
    }
}