package com.anczykowski.lexer;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Objects;
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

        trimWhitespace();

        if (tryBuildSimpleTokenOrComment()
            || tryBuildNumber()
            || tryBuildString()
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

    private void trimWhitespace() {
        while (source.isNotEOF() && StringUtils.isWhitespace(source.getCurrentCharacter())){
            source.fetchCharacter();
        }
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

    private Boolean tryBuildSimpleTokenOrComment() {
        var commentContent = new StringBuilder();
        TokenType tokenType = switch (source.getCurrentCharacterSingle()) {
            case '*' -> TokenType.ASTERISK;
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case ',' -> TokenType.COMMA;
            case '.' -> TokenType.PERIOD;
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            case '%' -> TokenType.PERCENT;
            case ';' -> TokenType.SEMICOLON;
            case '<' -> matchNextChar('=', TokenType.LE, TokenType.LT);
            case '>' -> matchNextChar('=', TokenType.GE, TokenType.GT);
            case '=' -> matchNextChar('=', TokenType.EQ, TokenType.ASSIGNMENT);
            case '!' -> matchNextChar('=', TokenType.NE, TokenType.NEG);
            case '/' -> handleSlashOrComment(commentContent);
            default -> TokenType.UNKNOWN;
        };
        if (tokenType == TokenType.UNKNOWN) return false;
        source.fetchCharacter();

        var tokenBuilder = Token.builder().type(tokenType);
        if(tokenType.equals(TokenType.COMMENT)){
            tokenBuilder.value(commentContent.toString());
        }
        currentToken = tokenBuilder.build();
        return true;
    }

    private TokenType handleSlashOrComment(StringBuilder commentContent) {
        var slashToken = matchNextChar('/', TokenType.COMMENT, TokenType.SLASH);
        if (slashToken.equals(TokenType.COMMENT)){
            buildComment(commentContent);
            if(commentContent.isEmpty()) commentContent.setLength(0);
        }
        return slashToken;
    }

    private TokenType matchNextChar(Character nextChar, TokenType returnMatch, TokenType returnMismatch) {
        source.fetchCharacter();
        var secondChar = source.isEOF() ? null : source.getCurrentCharacterSingle();
        if (Objects.equals(secondChar, nextChar)) {
            source.fetchCharacter();
            return returnMatch;
        }
        return returnMismatch;
    }

    private void buildComment(StringBuilder commentContent) {
        while (source.isNotEOF() && !source.getCurrentCharacter().equals("\n")) {
            consume(commentContent);
        }
    }

    private void consume(StringBuilder lexemValueBuilder) {
        lexemValueBuilder.append(source.getCurrentCharacter());
        if (lexemValueBuilder.length() >= LEXEM_MAX_SIZE) {
            throw new RuntimeException(); // TODO: error module instead of those throws
        }
        source.fetchCharacter();
    }
}
