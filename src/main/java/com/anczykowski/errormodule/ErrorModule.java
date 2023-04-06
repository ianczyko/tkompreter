package com.anczykowski.errormodule;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorModule {

    private final List<ErrorElement> errors = new ArrayList<>();

    public void addError(ErrorElement error) {
        errors.add(error);
    }

    public void printErrors(PrintStream out) {
        if (errors.isEmpty()) return;
        out.println("\n===== Error Module =====");
        out.printf("%d Errors, most recent errors first:%n", errors.size());
        errors.forEach(out::print);
        out.println();
    }
}
