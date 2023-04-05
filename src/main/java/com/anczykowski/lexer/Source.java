package com.anczykowski.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import lombok.Getter;

public class Source implements AutoCloseable {

    // TODO: Buffering N previous characters

    private final Reader reader;

    // UTF8 characters might not fit into a single character, because of that string is used
    private String currentCharacter = null;

    private String firstLineEnding;

    private String notConsumedCharacter = null;

    private boolean flagEOF = false;

    @Getter
    private Location currentLocation = new Location();

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
            currentLocation.incrementColumnNumber();
            if(notConsumedCharacter != null){
                currentCharacter = notConsumedCharacter;
                notConsumedCharacter = null;
                return;
            }
            innerFetchCharacter();
            handleNewline();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleNewline() throws IOException {
        var currentNewline = matchNewlineOfOrder("\r", "\n");
        if(currentNewline.isEmpty()) currentNewline = matchNewlineOfOrder("\n", "\r");
        if(currentNewline.isEmpty()) return;
        if(firstLineEnding == null) {
            firstLineEnding = currentNewline;
        }
        else if (!currentNewline.equals(firstLineEnding) && isNotEOF()){
            throw new RuntimeException("Inconsistent line endings"); // TODO: error module
        }
        currentCharacter = "\n";
        currentLocation.resetColumnNumber();
        currentLocation.incrementLineNumber();
    }

    private String matchNewlineOfOrder(String firstNewlineChar, String secondNewlineChar) throws IOException {
        var currentNewLine = new StringBuilder();
        if(currentCharacter.equals(firstNewlineChar)){
            currentNewLine.append(currentCharacter);
            innerFetchCharacter();
            if (currentCharacter.equals(secondNewlineChar)) {
                currentNewLine.append(currentCharacter);
            } else {
                notConsumedCharacter = currentCharacter;
            }
        }
        return currentNewLine.toString();
    }

    private void innerFetchCharacter() throws IOException {
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
    }

    public Source(Reader inputReader) {
        reader = new BufferedReader(inputReader);
    }

    public Source(Reader inputReader, String path) {
        currentLocation = new Location(path);
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
