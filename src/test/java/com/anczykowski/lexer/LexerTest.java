package com.anczykowski.lexer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.lexer.helpers.SourceHelpers;

// TODO: Edge cases
// TODO: Cases for each token

class LexerTest {

    @Test
    void getBasicToken() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("abc def", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(lexer.getCurrentToken().getType(), TokenType.IDENTIFIER);
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("abc", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getBasicTokenUnicodePolish() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(SourceHelpers.createUnicodeString("ząb mądrości"), errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals(SourceHelpers.createUnicodeString("ząb"), ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getBasicTokenUnicodeChinese2Bytes() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(SourceHelpers.createUnicodeString("中国 token"), errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals(SourceHelpers.createUnicodeString("中国"), ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getBasicTokenUnicodeChinese3Bytes() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(SourceHelpers.createUnicodeString("你好 token"), errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals(SourceHelpers.createUnicodeString("你好"), ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    // <I could not find any 4 byte chinese characters that pass the alphanumeric test>

    @Test
    void getBasicTokenUnicodeEmoji() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(SourceHelpers.createUnicodeString("a🚀b token"), errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals(SourceHelpers.createUnicodeString("a🚀b"), ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getBasicTokenLast() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("abc cba", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("cba", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getLT() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("a<b", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.LT, lexer.getCurrentToken().getType());
        }
    }

    @Test
    void getLE() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("a<=b", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.LE, lexer.getCurrentToken().getType());
        }
    }

    @Test
    void getComment() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("a //bcd", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.COMMENT, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("bcd", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getCommentEdge() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("a //", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.COMMENT, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    private static Stream<Arguments> expectedKeywordTypes() {
        return Stream.of(
            Arguments.of("var", TokenType.VAR_KEYWORD),
            Arguments.of("if", TokenType.IF_KEYWORD),
            Arguments.of("else", TokenType.ELSE_KEYWORD),
            Arguments.of("and", TokenType.AND_KEYWORD),
            Arguments.of("or", TokenType.OR_KEYWORD),
            Arguments.of("while", TokenType.WHILE_KEYWORD),
            Arguments.of("for", TokenType.FOR_KEYWORD),
            Arguments.of("return", TokenType.RETURN_KEYWORD),
            Arguments.of("switch", TokenType.SWITCH_KEYWORD),
            Arguments.of("def", TokenType.DEFAULT_KEYWORD),
            Arguments.of("class", TokenType.CLASS_KEYWORD),
            Arguments.of("new", TokenType.NEW_KEYWORD)
        );
    }

    @ParameterizedTest
    @MethodSource("expectedKeywordTypes")
    void getKeyword(String keyword, TokenType tokenType) {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(keyword, errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(tokenType, lexer.getCurrentToken().getType());
        }
    }

    @Test
    void getStringSimple() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("'aa'", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.STRING, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("aa", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getStringEscapedCharacter() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("'aa\\taa'", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.STRING, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("aa\taa", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getStringEscapedQuote() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("'aa\\'aa", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.STRING, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("aa'aa", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getStringUnclosedQuote() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("'aa\nvar", errorModule)) {
            var lexer = new Lexer(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(1, errorModule.getErrors().size());
            assertEquals(ErrorType.UNCLOSED_STRING, errorModule.getErrors().getFirst().getErrorType());
        }
    }
}