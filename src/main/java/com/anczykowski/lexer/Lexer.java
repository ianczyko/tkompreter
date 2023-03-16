package com.anczykowski.lexer;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.Character.isWhitespace;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Lexer implements Iterable<Token> {
    private final Source source;

    private static final int LEXEM_MAX_SIZE = 256;

    private Token currentToken;

    public Stream<Token> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public Token getNextToken() {

        // TODO: Trim whitespace?

        if (tryBuildSimpleTokens()
            || tryBuildNumber()
            || tryBuildString()
            || tryBuildWhitespace()
            || tryBuildIdentOrKeyword()
        ) {
            return currentToken;
        } else {
            // TODO: make this better
            StringBuilder stringBuilder = new StringBuilder();
            consume(stringBuilder);
            return Token.builder()
                .value(source.getCurrentCharacter().toString())
                .type(TokenType.UNKNOWN)
                .build();
        }
    }

    @Override
    public Iterator<Token> iterator() {
        return new Iterator<>() {
            public boolean hasNext() {
                return source.isNotEOF();
            }

            public Token next() {
                return Lexer.this.getNextToken();
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
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.NUMBER)
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
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.IDENTIFIER)
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
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.STRING)
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


        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.WHITESPACE)
            .build();
        return true;
    }

    private Boolean tryBuildSimpleTokens() {
        // TODO use map
        var lexemValueBuilder = new StringBuilder();
        TokenType tokenType;
        switch (source.getCurrentCharacter()) {
            case '/':
            case '*':
            case '+':
            case '-':
            case '=':
                tokenType = TokenType.OPERATOR;
                break;
            case ',':
            case '.':
            case ';':
                tokenType = TokenType.SEPARATOR;
                break;
            default:
                return false;
        }
        consume(lexemValueBuilder);
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(tokenType)
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
