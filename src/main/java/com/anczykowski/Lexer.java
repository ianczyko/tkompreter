package com.anczykowski;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.Character.isWhitespace;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Lexer implements Iterable<Lexem> {
    private final Source source;

    private static final int LEXEM_MAX_SIZE = 256;

    private Lexem Token;

    public Stream<Lexem> getLexemStream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public Lexem getLexem() {

        // TODO: Trim whitespace?

        if (tryBuildSimpleTokens()
            || tryBuildNumber()
            || tryBuildString()
            || tryBuildWhitespace()
            || tryBuildIdentOrKeyword()
        ) {
            return Token;
        } else {
            // TODO: make this better
            StringBuilder stringBuilder = new StringBuilder();
            consume(stringBuilder);
            return Lexem.builder()
                .value(source.getCurrentCharacter().toString())
                .type(LexemType.UNKNOWN)
                .build();
        }
    }

    @Override
    public Iterator<Lexem> iterator() {
        return new Iterator<>() {
            public boolean hasNext() {
                return source.isNotEOF();
            }

            public Lexem next() {
                return getLexem();
            }
        };
    }

    private Boolean tryBuildNumber() {
        // TODO: number value as number not as string
        if (!isDigit(source.getCurrentCharacter())) {
            return false;
        }
        var lexemValueBuilder = new StringBuilder();
        do {
            consume(lexemValueBuilder);
        } while (source.isNotEOF() && isDigit(source.getCurrentCharacter()));
        Token = Lexem.builder()
            .value(lexemValueBuilder.toString())
            .type(LexemType.NUMBER)
            .build();
        return true;
    }

    private Boolean tryBuildIdentOrKeyword() {
        if (!isLetter(source.getCurrentCharacter())) {
            return false;
        }
        var lexemValueBuilder = new StringBuilder();
        do {
            consume(lexemValueBuilder);
        } while (source.isNotEOF() && (isLetter(source.getCurrentCharacter()) || isDigit(
            source.getCurrentCharacter())));
        Token = Lexem.builder()
            .value(lexemValueBuilder.toString())
            .type(LexemType.IDENTIFIER)
            .build();
        return true;
    }

    private Boolean tryBuildString() {
        // TODO: handle escaped \"
        // TODO: handle escaped characters like \t
        if (!source.getCurrentCharacter().equals('"') && !source.getCurrentCharacter().equals('\'')) {
            return false;
        }

        var lexemValueBuilder = new StringBuilder();
        var endCharacter = source.getCurrentCharacter();
        do {
            consume(lexemValueBuilder);
        } while (source.isNotEOF() && !source.getCurrentCharacter().equals(endCharacter));
        consume(lexemValueBuilder);
        Token = Lexem.builder()
            .value(lexemValueBuilder.toString())
            .type(LexemType.STRING)
            .build();
        return true;
    }

    private Boolean tryBuildWhitespace() {
        if (!isWhitespace(source.getCurrentCharacter())) {
            return false;
        }

        var lexemValueBuilder = new StringBuilder();

        do {
            consume(lexemValueBuilder);
        } while (source.isNotEOF() && isWhitespace(source.getCurrentCharacter()));


        Token = Lexem.builder()
            .value(lexemValueBuilder.toString())
            .type(LexemType.WHITESPACE)
            .build();
        return true;
    }

    private Boolean tryBuildSimpleTokens() {
        // TODO use map
        var lexemValueBuilder = new StringBuilder();
        LexemType lexemType;
        switch (source.getCurrentCharacter()) {
            case '/':
            case '*':
            case '+':
            case '-':
            case '=':
                lexemType = LexemType.OPERATOR;
                break;
            case ',':
            case '.':
            case ';':
                lexemType = LexemType.SEPARATOR;
                break;
            default:
                return false;
        }
        consume(lexemValueBuilder);
        Token = Lexem.builder()
            .value(lexemValueBuilder.toString())
            .type(lexemType)
            .build();
        return true;
    }

    private void consume(StringBuilder lexemValueBuilder) {
        lexemValueBuilder.append(source.getCurrentCharacter());
        if (lexemValueBuilder.length() >= LEXEM_MAX_SIZE) {
            throw new RuntimeException(); // TODO: error module instead of those throws
        }
        source.fetchCharacter();
    }
}
