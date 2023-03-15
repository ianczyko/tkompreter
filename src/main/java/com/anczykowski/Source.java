package com.anczykowski;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class Source implements AutoCloseable {

    private final Reader reader;

    private Character currentCharacter = null;

    private boolean isEOF = false;

    public boolean isNotEOF() {
        return !isEOF;
    }

    public Character getCurrentCharacter() {
        if (currentCharacter == null) {
            fetchCharacter();
        }
        return currentCharacter;
    }

    public void fetchCharacter() {
        try {
            int c;
            if ((c = reader.read()) != -1) {
                currentCharacter = (char) c;
            } else {
                isEOF = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Source(Reader inputReader) {
        reader = new BufferedReader(inputReader);
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
