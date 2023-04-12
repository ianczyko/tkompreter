package com.anczykowski.errormodule;

import java.io.PrintStream;
import java.util.LinkedList;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorModule {

    @Getter
    private final LinkedList<ErrorElement> errors = new LinkedList<>();

    public void addError(ErrorElement error) {
        errors.add(error);
    }

    public void printErrors(PrintStream out) {
        if (errors.isEmpty()) return;
        out.println("\n===== Error Module =====");
        out.printf("%d Errors, most recent errors first:%n", errors.size());
        var it = errors.descendingIterator();
        while(it.hasNext()) {
            out.println(it.next());
        }
    }
}
