package com.anczykowski.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.lexer.IntegerToken;
import com.anczykowski.lexer.Location;
import com.anczykowski.lexer.StringToken;
import com.anczykowski.lexer.Token;
import com.anczykowski.lexer.TokenType;
import com.anczykowski.parser.helpers.ParserHelpers;
import com.anczykowski.parser.structures.expressions.AssignmentExpression;
import com.anczykowski.parser.structures.expressions.IdentifierExpression;
import com.anczykowski.parser.structures.expressions.IntegerConstantExpr;

class ParserIntegrationTests {

    @Test
    @SneakyThrows
    void parseProgramWithFunction() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new StringToken(TokenType.IDENTIFIER, new Location(), "fun"),
            new Token(TokenType.LPAREN, new Location()),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var program = parser.parse();

        // then
        assertTrue(program.getClasses().isEmpty());
        assertFalse(program.getFunctions().isEmpty());
        assertEquals("fun", program.getFunctions().get("fun").getName());
    }

    @Test
    @SneakyThrows
    void parseProgramWithFunctionWithParam() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new StringToken(TokenType.IDENTIFIER, new Location(), "fun"),
            new Token(TokenType.LPAREN, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "param"),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var program = parser.parse();

        // then
        assertTrue(program.getClasses().isEmpty());
        assertFalse(program.getFunctions().isEmpty());
        assertEquals("fun", program.getFunctions().get("fun").getName());
        assertEquals("param", program.getFunctions().get("fun").getParams().get(0).getName());
    }

    @Test
    @SneakyThrows
    void parseProgramWithFunctionWithParams() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new StringToken(TokenType.IDENTIFIER, new Location(), "fun"),
            new Token(TokenType.LPAREN, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "param1"),
            new Token(TokenType.COMMA, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "param2"),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var program = parser.parse();

        // then
        assertTrue(program.getClasses().isEmpty());
        assertFalse(program.getFunctions().isEmpty());
        assertEquals("fun", program.getFunctions().get("fun").getName());
        assertEquals("param1", program.getFunctions().get("fun").getParams().get(0).getName());
        assertEquals("param2", program.getFunctions().get("fun").getParams().get(1).getName());
    }

    @Test
    @SneakyThrows
    void parseProgramWithClass() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new Token(TokenType.CLASS_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "Circle"),
            new Token(TokenType.LBRACE, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var program = parser.parse();

        // then
        assertFalse(program.getClasses().isEmpty());
        assertTrue(program.getFunctions().isEmpty());
        assertEquals("Circle", program.getClasses().get("Circle").getName());
    }

    @Test
    @SneakyThrows
    void parseProgramWithClassWithAttributes() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new Token(TokenType.CLASS_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "Circle"),
            new Token(TokenType.LBRACE, new Location()),
            new Token(TokenType.VAR_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "rad"),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var program = parser.parse();

        // then
        assertFalse(program.getClasses().isEmpty());
        assertTrue(program.getFunctions().isEmpty());
        assertEquals("Circle", program.getClasses().get("Circle").getName());
        assertFalse(program.getClasses().get("Circle").getClassBody().getAttributes().isEmpty());
        assertEquals("rad", program.getClasses().get("Circle").getClassBody().getAttributes().get("rad").getName());
    }

    @Test
    @SneakyThrows
    void parseProgramWithFunctionWithBody() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new StringToken(TokenType.IDENTIFIER, new Location(), "fun"),
            new Token(TokenType.LPAREN, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "param1"),
            new Token(TokenType.COMMA, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "param2"),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "variable"),
            new Token(TokenType.ASSIGNMENT, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var program = parser.parse();

        // then
        assertTrue(program.getClasses().isEmpty());
        assertFalse(program.getFunctions().isEmpty());
        var fun = program.getFunctions().get("fun");
        assertEquals("fun", fun.getName());
        assertEquals("param1", fun.getParams().get(0).getName());
        assertEquals("param2", fun.getParams().get(1).getName());
        var firstExpr = (AssignmentExpression) fun.getCodeBLock().getStatementsAndExpressions().get(0);
        var firstExprLval = (IdentifierExpression) firstExpr.getLval();
        var firstExprRval = (IntegerConstantExpr) firstExpr.getRval();
        assertEquals("variable", firstExprLval.getIdentifier());
        assertEquals(2, firstExprRval.getValue());
    }

}