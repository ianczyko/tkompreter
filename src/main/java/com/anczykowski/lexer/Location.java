package com.anczykowski.lexer;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Location implements Cloneable {

    @Getter
    private int lineNumber = 1;

    @Getter
    private int columnNumber = 0;

    @Getter
    private String filename = null;

    public Location(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        if(filename == null) {
            return "STDIN:%d:%d".formatted(lineNumber, columnNumber);
        }
        return "%s:%d:%d".formatted(filename, lineNumber, columnNumber);
    }

    public void resetColumnNumber() {
        columnNumber = 0;
    }

    public void incrementColumnNumber() {
        ++columnNumber;
    }

    public void incrementLineNumber() {
        ++lineNumber;
    }

    @Override
    public Location clone() {
        try {
            return (Location) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
