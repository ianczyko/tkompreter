package com.anczykowski.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class Source implements AutoCloseable {

    private final Reader reader;

    // UTF8 characters might not fit into a single character, because of that string is used
    private String currentCharacter = null;

    private boolean flagEOF = false;

    public boolean isNotEOF() {
        return !flagEOF;
    }

    public boolean isEOF() {
        return flagEOF;
    }

    public String getCurrentCharacter() {
        if (currentCharacter == null) {
            fetchCharacter();
        }
        return currentCharacter;
    }

    public Character getCurrentCharacterSingle(){
        return currentCharacter.charAt(0);
    }

    public void fetchCharacter() {
        try {
            int highUnit = reader.read();
            if (highUnit < 0){
                flagEOF = true;
                return;
            }
            if (!Character.isHighSurrogate((char)highUnit))
            {
                currentCharacter = Character.toString(highUnit);
                return;
            }

            int lowUnit = reader.read();
            if (!Character.isLowSurrogate((char)lowUnit))
                throw new RuntimeException("Unmatched utf8 surrogate pair");

            currentCharacter = Character.toString(Character.toCodePoint((char)highUnit, (char)lowUnit));


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
