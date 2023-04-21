package com.anczykowski.lexer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.lexer.helpers.SourceHelpers;

class LexerTest {

    @Test
    void getBasicToken() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("abc def", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.IDENTIFIER, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("abc", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getEOF() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("abc", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.EOF, lexer.getCurrentToken().getType());
        }
    }

    @Test
    void getEOFNewline() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("abc\n", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();
            lexer.getNextToken();

            // then
            assertEquals(TokenType.EOF, lexer.getCurrentToken().getType());
        }
    }

    @Test
    void getBasicTokenUnicodePolish() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(SourceHelpers.createUnicodeString("ząb mądrości"), errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

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
            var lexer = new LexerImpl(src, errorModule);

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
            var lexer = new LexerImpl(src, errorModule);

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
            var lexer = new LexerImpl(src, errorModule);

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
            var lexer = new LexerImpl(src, errorModule);

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
    void filterOutComments(){
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("//abc", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);
            var lexerFiltered = new LexerFiltered(lexer, TokenFilters.getCommentFilter());

            // when
            lexerFiltered.getNextToken();

            // then
            assertEquals(TokenType.EOF, lexerFiltered.getCurrentToken().getType());
        }
    }

    @Test
    void getComment() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("a //bcd", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

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
            var lexer = new LexerImpl(src, errorModule);

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
            Arguments.of("new", TokenType.NEW_KEYWORD),
            Arguments.of("not", TokenType.NOT_KEYWORD)
        );
    }

    @ParameterizedTest
    @MethodSource("expectedKeywordTypes")
    void getKeyword(String keyword, TokenType tokenType) {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(keyword, errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(tokenType, lexer.getCurrentToken().getType());
        }
    }

    private static Stream<Arguments> expectedOperatorTypes() {
        return Stream.of(
            Arguments.of("*", TokenType.ASTERISK),
            Arguments.of("+", TokenType.PLUS),
            Arguments.of(",", TokenType.COMMA),
            Arguments.of(".", TokenType.PERIOD),
            Arguments.of("(", TokenType.LPAREN),
            Arguments.of(")", TokenType.RPAREN),
            Arguments.of("{", TokenType.LBRACE),
            Arguments.of("}", TokenType.RBRACE),
            Arguments.of(";", TokenType.SEMICOLON),

            Arguments.of("<", TokenType.LT),
            Arguments.of(">", TokenType.GT),
            Arguments.of("-", TokenType.MINUS),
            Arguments.of("=", TokenType.ASSIGNMENT),
            Arguments.of("!", TokenType.NEG),
            Arguments.of("/", TokenType.SLASH),

            Arguments.of("<=", TokenType.LE),
            Arguments.of(">=", TokenType.GE),
            Arguments.of("->", TokenType.ARROW),
            Arguments.of("==", TokenType.EQ),
            Arguments.of("!=", TokenType.NE)
        );
    }

    @ParameterizedTest
    @MethodSource("expectedOperatorTypes")
    void getOperator(String keyword, TokenType tokenType) {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(keyword, errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

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
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.STRING, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("aa", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getStringEmpty() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("''", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.STRING, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof StringToken);
            assertEquals("", ((StringToken)lexer.getCurrentToken()).getValue());
        }
    }

    @Test
    void getStringEscapedCharacter() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("'aa\\taa'", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

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
            var lexer = new LexerImpl(src, errorModule);

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
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(1, errorModule.getErrors().size());
            assertEquals(ErrorType.UNCLOSED_STRING, errorModule.getErrors().getFirst().getErrorType());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 100, 123, 321, 1234567})
    void getInteger(Integer value) {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(value.toString(), errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.INTEGER_NUMBER, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof IntegerToken);
            assertEquals(value, ((IntegerToken)lexer.getCurrentToken()).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(floats = {0f, 1f, 10f, 100f, 10.0f, 10.1f, 10.01f, 123.456f})
    void getFloat(Float value) {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource(value.toString(), errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(TokenType.FLOAT_NUMBER, lexer.getCurrentToken().getType());
            assertTrue(lexer.getCurrentToken() instanceof FloatToken);
            assertEquals(value, ((FloatToken)lexer.getCurrentToken()).getValue(), 0.000001f);
        }
    }

    @Test
    void getIntegerTooBig() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("100000000000000000", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(1, errorModule.getErrors().size());
            assertEquals(ErrorType.CONSTANT_TOO_BIG, errorModule.getErrors().getFirst().getErrorType());
        }
    }

    @Test
    void getFloatTooBig() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("1.11111111111111111", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(1, errorModule.getErrors().size());
            assertEquals(ErrorType.CONSTANT_TOO_BIG, errorModule.getErrors().getFirst().getErrorType());
        }
    }

    @Test
    void getFloatMalformed() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("100. xxx", errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(1, errorModule.getErrors().size());
            assertEquals(ErrorType.MALFORMED_NUMBER, errorModule.getErrors().getFirst().getErrorType());
        }
    }

    @Test
    void getIdentifierTooLong() {
        // given
        var errorModule = new ErrorModule();
        try (var src = SourceHelpers.thereIsSource("a".repeat(68), errorModule)) {
            var lexer = new LexerImpl(src, errorModule);

            // when
            lexer.getNextToken();

            // then
            assertEquals(1, errorModule.getErrors().size());
            assertEquals(ErrorType.TOKEN_TOO_LONG, errorModule.getErrors().getFirst().getErrorType());
        }
    }

}