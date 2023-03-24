package com.anczykowski.lexer;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.vdurmont.emoji.EmojiManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Lexer implements Iterable<Token> {
    private final Source source;

    private static final int LEXEM_MAX_SIZE = 256;

    @Getter
    private Token currentToken;

    public Stream<Token> stream() {
        return StreamSupport.stream(this.spliterator(), false);
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
                .value(source.getCurrentCharacter())
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
        if (!StringUtils.isNumeric(source.getCurrentCharacter())) {
            return false;
        }
        var lexemValueBuilder = new StringBuilder();
        do {
            consume(lexemValueBuilder);
        } while (source.isNotEOF() && StringUtils.isNumeric(source.getCurrentCharacter()));
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.NUMBER)
            .build();
        return true;
    }

    private Boolean tryBuildIdentOrKeyword() {
        if (!StringUtils.isAlpha(source.getCurrentCharacter()) && !EmojiManager.containsEmoji(source.getCurrentCharacter())) {
            return false;
        }
        var lexemValueBuilder = new StringBuilder();
        do {
            consume(lexemValueBuilder);
        } while (source.isNotEOF() &&
            (
                StringUtils.isAlpha(source.getCurrentCharacter())
                    || StringUtils.isNumeric(source.getCurrentCharacter())
                    || EmojiManager.containsEmoji(source.getCurrentCharacter())
            )
        );
        // TODO: keywords
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.IDENTIFIER)
            .build();
        return true;
    }

    private Boolean tryBuildString() {
        // TODO: handle escaped \"
        // TODO: handle escaped characters like \t
        // TODO: handle empty string ""
        if (!source.getCurrentCharacter().equals("\"") && !source.getCurrentCharacter().equals("'")) {
            return false;
        }

        var lexemValueBuilder = new StringBuilder();
        var endCharacter = source.getCurrentCharacter();
        do { // TODO: while instead of do while
            consume(lexemValueBuilder);
        } while (source.isNotEOF() && !source.getCurrentCharacter().equals(endCharacter));
        consume(lexemValueBuilder);
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.STRING)
            .build();
        return true;
    }

    // TODO: Remove this and replace with comments (and filter them out)
    private Boolean tryBuildWhitespace() {
        if (!StringUtils.isWhitespace(source.getCurrentCharacter())) {
            return false;
        }

        var lexemValueBuilder = new StringBuilder();

        do {
            consume(lexemValueBuilder);
        } while (source.isNotEOF() && StringUtils.isWhitespace(source.getCurrentCharacter()));


        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.WHITESPACE)
            .build();
        return true;
    }

    private Boolean tryBuildSimpleTokens() {
        var lexemValueBuilder = new StringBuilder(); //TODO: remove StringBuilder
        // TODO: inline matchSimpleTokenType
        TokenType tokenType = TokenType.matchSimpleTokenType(lexemValueBuilder + source.getCurrentCharacter());
        if (tokenType == TokenType.UNKNOWN) return false;
        if (tokenType == TokenType.INDISTINGUISHABLE) {
            consume(lexemValueBuilder);
            tokenType = TokenType.matchSimpleTokenType(lexemValueBuilder + source.getCurrentCharacter());
            if(tokenType.getArity() == 2){
                consume(lexemValueBuilder);
            }
        }
        else {
            consume(lexemValueBuilder);
        }
        currentToken = Token.builder()
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
