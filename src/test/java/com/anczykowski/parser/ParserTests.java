package com.anczykowski.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.lexer.Location;
import com.anczykowski.lexer.StringToken;
import com.anczykowski.lexer.Token;
import com.anczykowski.lexer.TokenType;
import com.anczykowski.parser.helpers.ParserHelpers;

class ParserTests {
    @Test
    void parseProgram() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of());
        var parser = new Parser(lexer, errorModule);

        // when
        var program = parser.parse();

        // then
        assertTrue(program.getClasses().isEmpty());
        assertTrue(program.getFunctions().isEmpty());
    }

    @Test
    void parseCodeBlockEmpty() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new Token(TokenType.LBRACE, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var codeBLock = parser.parseCodeBlock();

        // then
        assertTrue(true); // TODO: code block attributes
    }

    // TODO: non empty code block

    @Test
    void parseParamsOne() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new StringToken(TokenType.IDENTIFIER, new Location(), "par1"),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var params = parser.parseParams();

        // then
        assertEquals(1, params.size());
        assertEquals("par1", params.get(0).getName());
    }
}