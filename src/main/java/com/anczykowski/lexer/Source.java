package com.anczykowski.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import com.anczykowski.errormodule.ErrorElement;
import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import lombok.Getter;

public class Source implements AutoCloseable {

    private static final int LINE_BUFFER_LIMIT = 80;

    private final Reader reader;

    private final ErrorModule errorModule;

    // UTF8 characters might not fit into a single character, because of that string is used
    private String currentCharacter = null;

    private String firstLineEnding;

    private String notConsumedCharacter = null;

    private boolean flagEOF = false;

    @Getter
    private Location currentLocation = new Location();

    @Getter
    private final Stack<String> characterBuffer = new FixedCharacterStack(LINE_BUFFER_LIMIT);


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
                characterBuffer.push(notConsumedCharacter);
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
            errorModule.addError(
                ErrorElement.builder()
                    .errorType(ErrorType.INCONSISTENT_LINE_ENDINGS)
                    .location(getCurrentLocation().clone())
                    .codeLineBuffer(getCharacterBuffer().toString())
                    .build()
            );
        }
        currentCharacter = "\n";
        currentLocation.resetColumnNumber();
        characterBuffer.clear();
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
            characterBuffer.push(currentCharacter);
            return;
        }

        int lowUnit = reader.read();
        if (!Character.isLowSurrogate((char)lowUnit)){
            errorModule.addError(
                ErrorElement.builder()
                    .errorType(ErrorType.UNKNOWN_CHARACTER)
                    .location(getCurrentLocation().clone())
                    .codeLineBuffer(getCharacterBuffer().toString())
                    .build());
            return;
        }

        currentCharacter = Character.toString(Character.toCodePoint((char)highUnit, (char)lowUnit));
        characterBuffer.push(currentCharacter);
    }

    public Source(ErrorModule errorModule, Reader inputReader) {
        this.errorModule = errorModule;
        this.reader = new BufferedReader(inputReader);
    }

    public Source(ErrorModule errorModule, Reader inputReader, String path) {
        this.errorModule = errorModule;
        this.currentLocation = new Location(path);
        this.reader = new BufferedReader(inputReader);
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
