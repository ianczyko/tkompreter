package com.anczykowski.lexer;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.anczykowski.errormodule.ErrorElement;
import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.vdurmont.emoji.EmojiManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Lexer implements Iterable<Token> {
    private final Source source;

    private final ErrorModule errorModule;

    private static final int TOKEN_MAX_SIZE = 64;

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
            if(source.isEOF()) return Token.builder()
                .type(TokenType.EOF)
                .location(source.getCurrentLocation().clone())
                .build();
            return handleUnknown();
        }
    }

    private Token handleUnknown() {
        StringBuilder stringBuilder = new StringBuilder();
        consume(stringBuilder);
        return Token.builder()
            .value(source.getCurrentCharacter())
            .location(source.getCurrentLocation().clone())
            .type(TokenType.UNKNOWN)
            .build();
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
        var currentLocation = source.getCurrentLocation().clone();
        do {
            consume(lexemValueBuilder);
        } while (source.isNotEOF() && StringUtils.isNumeric(source.getCurrentCharacter()));
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .location(currentLocation)
            .type(TokenType.NUMBER)
            .build();
        return true;
    }

    private Boolean tryBuildIdentOrKeyword() {
        if (!StringUtils.isAlpha(source.getCurrentCharacter()) && !EmojiManager.containsEmoji(source.getCurrentCharacter())) {
            return false;
        }
        var lexemValueBuilder = new StringBuilder();
        var startLocation = source.getCurrentLocation().clone();
        do {
            consume(lexemValueBuilder);
        } while (source.isNotEOF() &&
            (
                StringUtils.isAlpha(source.getCurrentCharacter())
                    || StringUtils.isNumeric(source.getCurrentCharacter())
                    || EmojiManager.containsEmoji(source.getCurrentCharacter())
            )
        );
        TokenType foundKeyword = matchKeyword(lexemValueBuilder.toString());
        if(foundKeyword != null){
            currentToken = Token.builder()
                .type(foundKeyword)
                .location(startLocation)
                .build();
            return true;
        }
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.IDENTIFIER)
            .location(startLocation)
            .build();
        return true;
    }

    private TokenType matchKeyword(String string) {
        return switch (string) {
            case "var" -> TokenType.VAR_KEYWORD;
            case "if" -> TokenType.IF_KEYWORD;
            case "else" -> TokenType.ELSE_KEYWORD;
            case "and" -> TokenType.AND_KEYWORD;
            case "or" -> TokenType.OR_KEYWORD;
            case "while" -> TokenType.WHILE_KEYWORD;
            case "for" -> TokenType.FOR_KEYWORD;
            case "return" -> TokenType.RETURN_KEYWORD;
            case "switch" -> TokenType.SWITCH_KEYWORD;
            case "def" -> TokenType.DEFAULT_KEYWORD;
            case "class" -> TokenType.CLASS_KEYWORD;
            case "new" -> TokenType.NEW_KEYWORD;
            default -> null;
        };
    }

    private Boolean tryBuildString() {
        // TODO: handle escaped \"
        // TODO: handle escaped characters like \t
        // TODO: handle empty string ""
        if (!source.getCurrentCharacter().equals("\"") && !source.getCurrentCharacter().equals("'")) {
            return false;
        }

        var lexemValueBuilder = new StringBuilder();
        var currentLocation = source.getCurrentLocation().clone();
        var endCharacter = source.getCurrentCharacter();
        do { // TODO: while instead of do while
            consume(lexemValueBuilder);
        } while (source.isNotEOF() && !source.getCurrentCharacter().equals(endCharacter));
        consume(lexemValueBuilder);
        currentToken = Token.builder()
            .value(lexemValueBuilder.toString())
            .type(TokenType.STRING)
            .location(currentLocation)
            .build();
        return true;
    }

    private Boolean tryBuildSimpleTokenOrComment() {
        var commentContent = new StringBuilder();
        var currentLocation = source.getCurrentLocation().clone();
        TokenType tokenType = switch (source.getCurrentCharacterSingle()) {
            case '*' -> TokenType.ASTERISK;
            case '+' -> TokenType.PLUS;
            case ',' -> TokenType.COMMA;
            case '.' -> TokenType.PERIOD;
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            case '{' -> TokenType.LBRACE;
            case '}' -> TokenType.RBRACE;
            case ';' -> TokenType.SEMICOLON;
            case '<' -> matchNextChar('=', TokenType.LE, TokenType.LT);
            case '>' -> matchNextChar('=', TokenType.GE, TokenType.GT);
            case '-' -> matchNextChar('>', TokenType.ARROW, TokenType.MINUS);
            case '=' -> matchNextChar('=', TokenType.EQ, TokenType.ASSIGNMENT);
            case '!' -> matchNextChar('=', TokenType.NE, TokenType.NEG);
            case '/' -> handleSlashOrComment(commentContent);
            default -> TokenType.UNKNOWN;
        };
        if (tokenType == TokenType.UNKNOWN) return false;
        source.fetchCharacter();

        var tokenBuilder = Token.builder().type(tokenType).location(currentLocation);
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
        if (lexemValueBuilder.length() >= TOKEN_MAX_SIZE) {
            errorModule.addError(
                ErrorElement.builder()
                    .errorType(ErrorType.TOKEN_TOO_LONG)
                    .location(source.getCurrentLocation().clone())
                    .codeLineBuffer(source.getCharacterBuffer().toString())
                    .underlineFragment(lexemValueBuilder.toString())
                    .explanation("%d character limit exceeded".formatted(TOKEN_MAX_SIZE))
                    .build()
            );
            lexemValueBuilder.setLength(0);
        }
        source.fetchCharacter();
    }
}
