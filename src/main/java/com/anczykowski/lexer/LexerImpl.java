package com.anczykowski.lexer;

import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.anczykowski.errormodule.ErrorElement;
import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.vdurmont.emoji.EmojiManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LexerImpl implements Lexer {
    private final Source source;

    private final ErrorModule errorModule;

    private static final int TOKEN_MAX_SIZE = 64;

    private Token currentToken;

    @Override
    public Location getCurrentLocation() {
        return source.getCurrentLocation();
    }

    @Override
    public String getCharacterBuffer() {
        return source.getCharacterBuffer().toString();
    }

    @Override
    public Token getNextToken() {

        trimWhitespace();

        if (source.isEOF()) {
            return currentToken = new Token(TokenType.EOF, source.getCurrentLocation().clone());
        }

        if (tryBuildSimpleTokenOrComment()
            || tryBuildNumber()
            || tryBuildString()
            || tryBuildIdentOrKeyword()
        ) {
            return currentToken;
        } else {
            source.fetchCharacter();
            return currentToken = new Token(TokenType.UNKNOWN, source.getCurrentLocation().clone());
        }
    }

    private void trimWhitespace() {
        while (source.isNotEOF() && StringUtils.isWhitespace(source.getCurrentCharacter())) {
            source.fetchCharacter();
        }
    }

    private Boolean tryBuildNumber() {
        if (!StringUtils.isNumeric(source.getCurrentCharacter())) {
            return false;
        }
        var currentLocation = source.getCurrentLocation().clone();
        int nominator = source.getCurrentCharacterSingle() - '0';
        if (!source.getCurrentCharacter().equals("0")) {
            source.fetchCharacter();
            while (source.isNotEOF() && StringUtils.isNumeric(source.getCurrentCharacter())) {
                int decimal = source.getCurrentCharacterSingle() - '0';
                if (willNotOverflow(currentLocation, nominator, decimal)) {
                    nominator = nominator * 10 + decimal;
                } else {
                    // ignore the rest of the number
                    while (source.isNotEOF() && StringUtils.isNumeric(source.getCurrentCharacter())) {
                        source.fetchCharacter();
                    }
                }
                source.fetchCharacter();
            }
        } else {
            source.fetchCharacter();
        }
        if (source.getCurrentCharacter().equals(".")) {
            source.fetchCharacter();
            if (!StringUtils.isNumeric(source.getCurrentCharacter())) {
                errorModule.addError(
                    ErrorElement.builder()
                        .errorType(ErrorType.MALFORMED_NUMBER)
                        .location(currentLocation)
                        .codeLineBuffer(source.getCharacterBuffer().toString())
                        .underlineFragment(nominator + ".")
                        .build()
                );
            }
            int denominator = source.getCurrentCharacterSingle() - '0';
            int decimalCount = 1;
            source.fetchCharacter();
            while (source.isNotEOF() && StringUtils.isNumeric(source.getCurrentCharacter())) {
                int decimal = source.getCurrentCharacterSingle() - '0';
                if (willNotOverflow(currentLocation, denominator, decimal)) {
                    denominator = denominator * 10 + decimal;
                } else {
                    // ignore the rest of the number
                    while (source.isNotEOF() && StringUtils.isNumeric(source.getCurrentCharacter())) {
                        source.fetchCharacter();
                    }
                }
                ++decimalCount;
                source.fetchCharacter();
            }
            var floatValue = nominator + denominator * Math.pow(10, -decimalCount);
            currentToken = new FloatToken(TokenType.FLOAT_NUMBER, currentLocation, (float) floatValue);
        } else {
            currentToken = new IntegerToken(TokenType.INTEGER_NUMBER, currentLocation, nominator);
        }
        return true;
    }

    private boolean willNotOverflow(Location currentLocation, int currentValue, int decimal) {
        if ((Integer.MAX_VALUE - decimal) / 10 <= currentValue) {
            errorModule.addError(
                ErrorElement.builder()
                    .errorType(ErrorType.CONSTANT_TOO_BIG)
                    .location(currentLocation)
                    .codeLineBuffer(source.getCharacterBuffer().toString())
                    .underlineFragment(String.valueOf(currentValue))
                    .build()
            );
            return false;
        }
        return true;
    }

    private Boolean tryBuildIdentOrKeyword() {
        if (!StringUtils.isAlpha(source.getCurrentCharacter()) && !EmojiManager.containsEmoji(
            source.getCurrentCharacter())) {
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
        if (foundKeyword != null) {
            currentToken = new Token(foundKeyword, startLocation);
            return true;
        }
        currentToken = new StringToken(TokenType.IDENTIFIER, startLocation, lexemValueBuilder.toString());
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
            case "not" -> TokenType.NOT_KEYWORD;
            case "in" -> TokenType.IN_KEYWORD;
            default -> null;
        };
    }

    private Boolean tryBuildString() {
        if (!source.getCurrentCharacter().equals("\"") && !source.getCurrentCharacter().equals("'")) {
            return false;
        }

        var lexemValueBuilder = new StringBuilder();
        var currentLocation = source.getCurrentLocation().clone();
        var endCharacter = source.getCurrentCharacter();
        source.fetchCharacter();
        while (insideString(lexemValueBuilder, endCharacter)) {
            consume(lexemValueBuilder);
        }

        if (source.getCurrentCharacter().equals("\n")) {
            errorModule.addError(
                ErrorElement.builder()
                    .errorType(ErrorType.UNCLOSED_STRING)
                    .location(currentLocation)
                    .codeLineBuffer(source.getPreviousLine())
                    .underlineFragment(lexemValueBuilder.toString())
                    .build()
            );
        }

        var unescapedString = unescapeJava(lexemValueBuilder.toString());

        source.fetchCharacter();
        currentToken = new StringToken(TokenType.STRING, currentLocation, unescapedString);
        return true;
    }

    private boolean insideString(StringBuilder sb, String endCharacter) {
        var isEscaped = !sb.isEmpty() && sb.charAt(sb.length() - 1) == '\\';
        var isEndQuote = source.getCurrentCharacter().equals(endCharacter);
        var isNewline = source.getCurrentCharacter().equals("\n");
        return source.isNotEOF() && !(isEndQuote && !isEscaped) && !isNewline;
    }

    private static final Set<TokenType> typesThatPeeked = new HashSet<>(Arrays.asList(
        TokenType.LT,
        TokenType.GT,
        TokenType.MINUS,
        TokenType.ASSIGNMENT,
        TokenType.NEG,
        TokenType.SLASH
    ));

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

        if(!typesThatPeeked.contains(tokenType)){
            source.fetchCharacter();
        }

        if (tokenType.equals(TokenType.COMMENT)) {
            currentToken = new StringToken(tokenType, currentLocation, commentContent.toString());
        } else {
            currentToken = new Token(tokenType, currentLocation);
        }
        return true;
    }

    private TokenType handleSlashOrComment(StringBuilder commentContent) {
        var slashToken = matchNextChar('/', TokenType.COMMENT, TokenType.SLASH);
        if (slashToken.equals(TokenType.COMMENT)) {
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

    @Override
    public Token getCurrentToken() {
        return this.currentToken;
    }
}
