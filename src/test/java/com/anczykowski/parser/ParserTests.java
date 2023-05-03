package com.anczykowski.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.errormodule.exceptions.ParserException;
import com.anczykowski.parser.structures.SwitchLabel;
import com.anczykowski.parser.structures.expressions.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.lexer.FloatToken;
import com.anczykowski.lexer.IntegerToken;
import com.anczykowski.lexer.Location;
import com.anczykowski.lexer.StringToken;
import com.anczykowski.lexer.Token;
import com.anczykowski.lexer.TokenType;
import com.anczykowski.parser.helpers.ParserHelpers;
import com.anczykowski.parser.structures.ClassDef;
import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.FuncDef;
import com.anczykowski.parser.structures.expressions.relops.EqRelExpr;
import com.anczykowski.parser.structures.expressions.relops.GeRelExpr;
import com.anczykowski.parser.structures.expressions.relops.GtRelExpr;
import com.anczykowski.parser.structures.expressions.relops.LeRelExpr;
import com.anczykowski.parser.structures.expressions.relops.LtRelExpr;
import com.anczykowski.parser.structures.expressions.relops.NeRelExpr;
import com.anczykowski.parser.structures.statements.CondStmt;
import com.anczykowski.parser.structures.statements.ForStmt;
import com.anczykowski.parser.structures.statements.SwitchStmt;
import com.anczykowski.parser.structures.statements.VarStmt;
import com.anczykowski.parser.structures.statements.WhileStmt;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class ParserTests {
    @Test
    @SneakyThrows
    void parseProgram() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of());
        var parser = new Parser(lexer, errorModule);

        // when
        assertDoesNotThrow( () -> {
            var program = parser.parse();

            // then
            assertTrue(program.getClasses().isEmpty());
            assertTrue(program.getFunctions().isEmpty());
        });

    }

    @Test
    @SneakyThrows
    void parseCodeBlockMissingRBrace() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.LBRACE, new Location()),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseCodeBlock();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
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
        assertTrue(codeBLock.getStatementsAndExpressions().isEmpty());
    }

    private static Stream<Arguments> binaryOperatorsMapping() {
        return Stream.of(
                Arguments.of(TokenType.OR_KEYWORD, OrExpression.class),
                Arguments.of(TokenType.AND_KEYWORD, AndExpr.class),

                Arguments.of(TokenType.EQ, EqRelExpr.class),
                Arguments.of(TokenType.NE, NeRelExpr.class),
                Arguments.of(TokenType.LT, LtRelExpr.class),
                Arguments.of(TokenType.LE, LeRelExpr.class),
                Arguments.of(TokenType.GT, GtRelExpr.class),
                Arguments.of(TokenType.GE, GeRelExpr.class),

                Arguments.of(TokenType.PLUS, AdditionTerm.class),
                Arguments.of(TokenType.MINUS, SubtractionTerm.class),

                Arguments.of(TokenType.ASTERISK, MultiplicationFactor.class),
                Arguments.of(TokenType.SLASH, DivisionFactor.class)
        );
    }

    @ParameterizedTest
    @SneakyThrows
    @MethodSource("binaryOperatorsMapping")
    <T extends LeftRightExpression> void parseExprOfType(TokenType binaryOperator, Class<T> binaryClass) {

        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
                new Token(binaryOperator, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = parser.parseExpr();

        // then
        T exprConcrete = binaryClass.cast(expr);
        assertTrue(exprConcrete.getLeft() instanceof IntegerConstantExpr);
        assertTrue(exprConcrete.getRight() instanceof IntegerConstantExpr);
        assertEquals(1, ((IntegerConstantExpr) exprConcrete.getLeft()).getValue());
        assertEquals(2, ((IntegerConstantExpr) exprConcrete.getRight()).getValue());
    }

    @ParameterizedTest
    @SneakyThrows
    @MethodSource("binaryOperatorsMapping")
    <T extends LeftRightExpression> void parseExprOfTypeMissingSecondOperand(TokenType binaryOperator, Class<T> ignored) {

        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
                new Token(binaryOperator, new Location()),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseExpr();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseEqUnsupportedChains() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
                new Token(TokenType.EQ, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
                new Token(TokenType.EQ, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 3),
                new Token(TokenType.EQ, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 4)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseRelExpr();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNSUPPORTED_CHAINING, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseIdentifier() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new StringToken(TokenType.IDENTIFIER, new Location(), "abc")
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var identifier = (IdentifierExpression) parser.parseIdentOrFunCall();

        // then
        assertEquals("abc", identifier.getIdentifier());
    }

    @Test
    @SneakyThrows
    void parseStringExpr() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new StringToken(TokenType.STRING, new Location(), "abc")
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var stringExpression = (StringExpression) parser.parseString();

        // then
        assertEquals("abc", stringExpression.getValue());
    }

    @Test
    @SneakyThrows
    void parseArg() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var arg = (Arg) parser.parseArg();

        // then
        assertEquals(1, ((IntegerConstantExpr) arg.getArgument()).getValue());
        assertFalse(arg.isByReference());
    }

    @Test
    @SneakyThrows
    void parseArgRef() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.REF_KEYWORD, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var arg = (Arg) parser.parseArg();

        // then
        assertEquals(1, ((IntegerConstantExpr) arg.getArgument()).getValue());
        assertTrue(arg.isByReference());
    }

    @Test
    @SneakyThrows
    void parseArgRefWithoutExpr() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.REF_KEYWORD, new Location()),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseArg();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseObjAccess() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new StringToken(TokenType.IDENTIFIER, new Location(), "aaa"),
            new Token(TokenType.PERIOD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "bbb")
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var objAccess = (ObjectAccessExpression) parser.parseObjAccess();

        // then
        var first = (IdentifierExpression) objAccess.getCurrent();
        var second = (IdentifierExpression) objAccess.getChild();
        assertEquals("bbb", first.getIdentifier());
        assertEquals("aaa", second.getIdentifier());
    }

    @Test
    @SneakyThrows
    void parseObjAccessNoSecondIdentifierAfterDot() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new StringToken(TokenType.IDENTIFIER, new Location(), "aaa"),
                new Token(TokenType.PERIOD, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseObjAccess();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseObjAccessDeeper() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new StringToken(TokenType.IDENTIFIER, new Location(), "aaa"),
            new Token(TokenType.PERIOD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "bbb"),
            new Token(TokenType.PERIOD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "ccc")
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var objAccess = (ObjectAccessExpression) parser.parseObjAccess();

        // then
        var first = (IdentifierExpression) objAccess.getCurrent();
        var child = (ObjectAccessExpression) objAccess.getChild();
        var second = (IdentifierExpression) child.getCurrent();
        var third = (IdentifierExpression) child.getChild();
        assertEquals("ccc", first.getIdentifier());
        assertEquals("bbb", second.getIdentifier());
        assertEquals("aaa", third.getIdentifier());
    }

    @Test
    @SneakyThrows
    void parseFunctionCall() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new StringToken(TokenType.IDENTIFIER, new Location(), "fun"),
            new Token(TokenType.LPAREN, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "arg1"),
            new Token(TokenType.COMMA, new Location()),
            new Token(TokenType.REF_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "arg2"),
            new Token(TokenType.RPAREN, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var funCall = (FunctionCallExpression) parser.parseIdentOrFunCall();

        // then
        assertEquals("fun", funCall.getIdentifier());

        assertFalse(funCall.getArgs().get(0).isByReference());
        var firstArg = (IdentifierExpression) funCall.getArgs().get(0).getArgument();
        assertEquals("arg1", firstArg.getIdentifier());

        assertTrue(funCall.getArgs().get(1).isByReference());
        var secondArg = (IdentifierExpression) funCall.getArgs().get(1).getArgument();
        assertEquals("arg2", secondArg.getIdentifier());
    }

    @Test
    @SneakyThrows
    void parseFunctionCallMissingRParen() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new StringToken(TokenType.IDENTIFIER, new Location(), "fun"),
                new Token(TokenType.LPAREN, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "arg1"),
                new Token(TokenType.COMMA, new Location()),
                new Token(TokenType.REF_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "arg2")
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseIdentOrFunCall();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseFunctionDefinition() {
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

        var functions = new HashMap<String, FuncDef>();

        // when
        parser.parseFunDef(functions);

        // then
        var parsedFunction = functions.get("fun");
        assertEquals("fun", parsedFunction.getName());

        assertEquals("param1", parsedFunction.getParams().get(0).getName());
        assertEquals("param2", parsedFunction.getParams().get(1).getName());

        assertTrue(parsedFunction.getCodeBLock().getStatementsAndExpressions().isEmpty());
    }

    @Test
    @SneakyThrows
    void parseFunctionDefinitionWithoutLParen() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new StringToken(TokenType.IDENTIFIER, new Location(), "fun"),
                new StringToken(TokenType.IDENTIFIER, new Location(), "param1"),
                new Token(TokenType.COMMA, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "param2"),
                new Token(TokenType.RPAREN, new Location()),
                new Token(TokenType.LBRACE, new Location()),
                new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        var functions = new HashMap<String, FuncDef>();

        // when
        parser.parseFunDef(functions);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseFunctionDefinitionWithoutRParen() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new StringToken(TokenType.IDENTIFIER, new Location(), "fun"),
                new Token(TokenType.LPAREN, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "param1"),
                new Token(TokenType.COMMA, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "param2"),
                new Token(TokenType.LBRACE, new Location()),
                new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        var functions = new HashMap<String, FuncDef>();

        // when
        parser.parseFunDef(functions);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseFunctionDefinitionRedefinition() {
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

        var functions = new HashMap<String, FuncDef>();
        functions.put("fun", null);

        // when
        parser.parseFunDef(functions);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.ALREADY_DECLARED, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    void parseFunctionDefinitionThrows() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new StringToken(TokenType.IDENTIFIER, new Location(), "fun"),
                new Token(TokenType.LPAREN, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "param1"),
                new Token(TokenType.COMMA, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "param2"),
                new Token(TokenType.RPAREN, new Location()),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        var functions = new HashMap<String, FuncDef>();

        // then
        assertThrows(ParserException.class, () -> {
            // when
            parser.parseFunDef(functions);
        });
        assertFalse(errorModule.getErrors().isEmpty());
    }

    @Test
    @SneakyThrows
    void parseClassDefinition() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new Token(TokenType.CLASS_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "Circle"),
            new Token(TokenType.LBRACE, new Location()),
            new Token(TokenType.VAR_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "field1"),
            new Token(TokenType.ASSIGNMENT, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.SEMICOLON, new Location()),

            new StringToken(TokenType.IDENTIFIER, new Location(), "method1"),
            new Token(TokenType.LPAREN, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "arg1"),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new Token(TokenType.RBRACE, new Location()),

            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        var classes = new HashMap<String, ClassDef>();

        // when
        parser.parseClassDef(classes);

        // then
        var parsedClass = classes.get("Circle");
        assertEquals("Circle", parsedClass.getName());

        var method1 = parsedClass.getClassBody().getMethods().get("method1");

        assertEquals("arg1", method1.getParams().get(0).getName());

        assertTrue(method1.getCodeBLock().getStatementsAndExpressions().isEmpty());
    }

    @Test
    @SneakyThrows
    void parseClassDefinitionNoIdentifier() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.CLASS_KEYWORD, new Location()),
                new Token(TokenType.LBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        var classes = new HashMap<String, ClassDef>();

        // when
        parser.parseClassDef(classes);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseClassDefinitionClassRedefinition() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.CLASS_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "Circle"),
                new Token(TokenType.LBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        var classes = new HashMap<String, ClassDef>();

        classes.put("Circle", null);

        // when
        parser.parseClassDef(classes);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.ALREADY_DECLARED, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseClassDefinitionNoBody() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.CLASS_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "Circle"),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        var classes = new HashMap<String, ClassDef>();

        // when
        parser.parseClassDef(classes);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseClassInit() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new Token(TokenType.NEW_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "Circle"),
            new Token(TokenType.LPAREN, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "arg1"),
            new Token(TokenType.COMMA, new Location()),
            new Token(TokenType.REF_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "arg2"),
            new Token(TokenType.RPAREN, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var classInit = (ClassInitExpression) parser.parseClassInit();

        // then
        assertEquals("Circle", classInit.getIdentifier());

        assertFalse(classInit.getArgs().get(0).isByReference());
        var firstArg = (IdentifierExpression) classInit.getArgs().get(0).getArgument();
        assertEquals("arg1", firstArg.getIdentifier());

        assertTrue(classInit.getArgs().get(1).isByReference());
        var secondArg = (IdentifierExpression) classInit.getArgs().get(1).getArgument();
        assertEquals("arg2", secondArg.getIdentifier());
    }

    @Test
    @SneakyThrows
    void parseClassInitWithoutIdentifier() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.NEW_KEYWORD, new Location()),
                new Token(TokenType.LPAREN, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseClassInit();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseClassInitWithoutLParen() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.NEW_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "Circle"),
                new StringToken(TokenType.IDENTIFIER, new Location(), "arg1"),
                new Token(TokenType.COMMA, new Location()),
                new Token(TokenType.REF_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "arg2"),
                new Token(TokenType.RPAREN, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseClassInit();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseClassInitWithoutRParen() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.NEW_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "Circle"),
                new Token(TokenType.LPAREN, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "arg1"),
                new Token(TokenType.COMMA, new Location()),
                new Token(TokenType.REF_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "arg2")
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseClassInit();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseExprParenthesized() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.LPAREN, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
                new Token(TokenType.RPAREN, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = parser.parseExprParenthesized();

        // then
        assertEquals(1, ((IntegerConstantExpr) expr).getValue());
    }

    @Test
    @SneakyThrows
    void parseExprParenthesizedNoExpr() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.LPAREN, new Location()),
                new Token(TokenType.RPAREN, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseExprParenthesized();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseExprParenthesizedRParen() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
                new Token(TokenType.LPAREN, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseExprParenthesized();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseParamsOne() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new StringToken(TokenType.IDENTIFIER, new Location(), "par1")
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var params = parser.parseParams();

        // then
        assertEquals(1, params.size());
        assertEquals("par1", params.get(0).getName());
    }

    @Test
    @SneakyThrows
    void parseFloatConstant() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new FloatToken(TokenType.FLOAT_NUMBER, new Location(), 2.5f)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var floatConstant = parser.parseFloatConstant();

        // then
        assertEquals(2.5f, floatConstant.getValue(), 0.000001f);
    }

    @Test
    @SneakyThrows
    void parseIntegerConstant() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 123)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var integerConstant = parser.parseIntegerConstant();

        // then
        assertEquals(123, integerConstant.getValue());
    }

    @Test
    @SneakyThrows
    void parseAddition() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.PLUS, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (AdditionTerm) parser.parseAddExpr();

        // then
        assertEquals(1, ((IntegerConstantExpr) expr.getLeft()).getValue());
        assertEquals(2, ((IntegerConstantExpr) expr.getRight()).getValue());
    }

    @Test
    @SneakyThrows
    void parseSubtraction() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.MINUS, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (SubtractionTerm) parser.parseAddExpr();

        // then
        assertEquals(1, ((IntegerConstantExpr) expr.getLeft()).getValue());
        assertEquals(2, ((IntegerConstantExpr) expr.getRight()).getValue());
    }

    @Test
    @SneakyThrows
    void parseMultiplication() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.ASTERISK, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (MultiplicationFactor) parser.parseTerm();

        // then
        assertEquals(1, ((IntegerConstantExpr) expr.getLeft()).getValue());
        assertEquals(2, ((IntegerConstantExpr) expr.getRight()).getValue());
    }

    @Test
    @SneakyThrows
    void parseDivision() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.SLASH, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (DivisionFactor) parser.parseTerm();

        // then
        assertEquals(1, ((IntegerConstantExpr) expr.getLeft()).getValue());
        assertEquals(2, ((IntegerConstantExpr) expr.getRight()).getValue());
    }

    @Test
    @SneakyThrows
    void testAdditionMultiplicationOrder() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.PLUS, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.ASTERISK, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 3)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (AdditionTerm) parser.parseAddExpr();

        // then
        var left = (IntegerConstantExpr) expr.getLeft();
        var right = (MultiplicationFactor) expr.getRight();
        var rightLeft = (IntegerConstantExpr) right.getLeft();
        var rightRight = (IntegerConstantExpr) right.getRight();
        assertEquals(1, left.getValue());
        assertEquals(2, rightLeft.getValue());
        assertEquals(3, rightRight.getValue());
    }

    @Test
    @SneakyThrows
    void testAdditionMultiplicationOrderParenthesized() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.LPAREN, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.PLUS, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.ASTERISK, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 3)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (MultiplicationFactor) parser.parseAddExpr();

        // then
        var left = (AdditionTerm) expr.getLeft();
        var right = (IntegerConstantExpr) expr.getRight();
        var leftLeft = (IntegerConstantExpr) left.getLeft();
        var leftRight = (IntegerConstantExpr) left.getRight();
        assertEquals(1, leftLeft.getValue());
        assertEquals(2, leftRight.getValue());
        assertEquals(3, right.getValue());
    }

    @Test
    @SneakyThrows
    void parseVarWithAssignment() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.VAR_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "variable"),
            new Token(TokenType.ASSIGNMENT, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var variables = new HashMap<String, VarStmt>();
        var varStmt = parser.parseVarStmt(variables);

        // then
        assertTrue(variables.containsKey("variable"));
        assertEquals("variable", varStmt.getName());
        assertEquals(2, ((IntegerConstantExpr) varStmt.getInitial()).getValue());
    }

    @Test
    @SneakyThrows
    void parseVarWithAssignmentNoIdentifier() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.VAR_KEYWORD, new Location()),
            new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var variables = new HashMap<String, VarStmt>();
        parser.parseVarStmt(variables);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseVarWithAssignmentVarAlreadyDeclared() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.VAR_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "variable"),
                new Token(TokenType.ASSIGNMENT, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var variables = new HashMap<String, VarStmt>();
        variables.put("variable", null);

        parser.parseVarStmt(variables);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.ALREADY_DECLARED, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseVarWithAssignmentWithoutAssignment() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.VAR_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "variable"),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var variables = new HashMap<String, VarStmt>();

        assertThrows(ParserException.class, () -> parser.parseVarStmt(variables));

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseVarWithAssignmentMissingSemicolon() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.VAR_KEYWORD, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "variable"),
                new Token(TokenType.ASSIGNMENT, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
                new Token(TokenType.VAR_KEYWORD, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var variables = new HashMap<String, VarStmt>();

        parser.parseVarStmt(variables);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseExprWithReturn() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.RETURN_KEYWORD, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var statementsAndExpressions = new ArrayList<Expression>();
        parser.parseRetStmt(statementsAndExpressions);

        // then
        var expr = (ReturnExpression) statementsAndExpressions.get(0);
        assertEquals(1, ((IntegerConstantExpr) expr.getInner()).getValue());
    }

    @Test
    @SneakyThrows
    void parseExprWithReturnMissingSemicolon() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.RETURN_KEYWORD, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
                new Token(TokenType.RETURN_KEYWORD, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var statementsAndExpressions = new ArrayList<Expression>();
        parser.parseRetStmt(statementsAndExpressions);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseNegatedFactor() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.MINUS, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = parser.parseFactor();

        // then
        var negatedExpr = (NegatedExpression) expr;
        var inner = (IntegerConstantExpr) negatedExpr.getInner();
        assertEquals(1, inner.getValue());
    }

    @Test
    @SneakyThrows
    void parseNegatedFactorNegationNullExpr() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.MINUS, new Location()),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseFactor();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }


    @Test
    @SneakyThrows
    void parseCastedFactor() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.AS_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "int")
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = parser.parseFactor();

        // then
        var castedExpr = (CastExpression) expr;
        var inner = (IntegerConstantExpr) castedExpr.getInner();
        assertEquals(1, inner.getValue());
        assertEquals("int", castedExpr.getType());
    }

    @Test
    @SneakyThrows
    void parseCastedFactorWithoutExpr() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
                new Token(TokenType.AS_KEYWORD, new Location()),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        parser.parseFactor();

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseAssignmentExpression() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.ASSIGNMENT, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var statementsAndExpressions = new ArrayList<Expression>();
        parser.parseExprInsideCodeBlock(statementsAndExpressions);

        // then
        var expr = (AssignmentExpression) statementsAndExpressions.get(0);
        var lval = (IntegerConstantExpr) expr.getLval();
        var rval = (IntegerConstantExpr) expr.getRval();
        assertEquals(1, lval.getValue());
        assertEquals(2, rval.getValue());
    }

    @Test
    @SneakyThrows
    void parseAssignmentExpressionMissingRvalAfterAssign() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
                new Token(TokenType.ASSIGNMENT, new Location()),
                new Token(TokenType.SEMICOLON, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var statementsAndExpressions = new ArrayList<Expression>();
        parser.parseExprInsideCodeBlock(statementsAndExpressions);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseAssignmentExpressionMissingSemicolon() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
                new Token(TokenType.ASSIGNMENT, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var statementsAndExpressions = new ArrayList<Expression>();
        parser.parseExprInsideCodeBlock(statementsAndExpressions);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNEXPECTED_TOKEN, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    @SneakyThrows
    void parseConditionalStatement() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.IF_KEYWORD, new Location()),
            new Token(TokenType.LPAREN, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var condExpr = (CondStmt) parser.parseConditionalStmt();

        // then
        var cond = (IntegerConstantExpr) condExpr.getCondition();
        var trueBLock = (CodeBLock) condExpr.getTrueBlock();
        var trueBlockFirstStmt = (IntegerConstantExpr) trueBLock.getStatementsAndExpressions().get(0);
        var elseBlock = condExpr.getElseBlock();
        assertEquals(1, cond.getValue());
        assertEquals(2, trueBlockFirstStmt.getValue());
        assertNull(elseBlock);
    }

    @Test
    void parseConditionalStatementThrows() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.IF_KEYWORD, new Location()),
                new Token(TokenType.LPAREN, new Location()),
                new Token(TokenType.RPAREN, new Location()),
                new Token(TokenType.LBRACE, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
                new Token(TokenType.SEMICOLON, new Location()),
                new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when, then
        assertThrows(ParserException.class, parser::parseConditionalStmt);
        assertFalse(errorModule.getErrors().isEmpty());
    }

    @Test
    @SneakyThrows
    void parseConditionalStatementWithElseBlock() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.IF_KEYWORD, new Location()),
            new Token(TokenType.LPAREN, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location()),
            new Token(TokenType.ELSE_KEYWORD, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 3),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var condExpr = (CondStmt) parser.parseConditionalStmt();

        // then
        var cond = (IntegerConstantExpr) condExpr.getCondition();
        var trueBLock = (CodeBLock) condExpr.getTrueBlock();
        var trueBlockFirstStmt = (IntegerConstantExpr) trueBLock.getStatementsAndExpressions().get(0);
        var elseBlock = condExpr.getElseBlock();
        var elseBlockFirstStmt = (IntegerConstantExpr) elseBlock.getStatementsAndExpressions().get(0);
        assertEquals(1, cond.getValue());
        assertEquals(2, trueBlockFirstStmt.getValue());
        assertEquals(3, elseBlockFirstStmt.getValue());
    }

    @Test
    @SneakyThrows
    void parseWhileStmt() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.WHILE_KEYWORD, new Location()),
            new Token(TokenType.LPAREN, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var whileStmt = (WhileStmt) parser.parseWhileStmt();

        // then
        var cond = (IntegerConstantExpr) whileStmt.getCondition();
        var trueBLock = (CodeBLock) whileStmt.getCodeBLock();
        var trueBlockFirstStmt = (IntegerConstantExpr) trueBLock.getStatementsAndExpressions().get(0);
        assertEquals(1, cond.getValue());
        assertEquals(2, trueBlockFirstStmt.getValue());
    }

    @Test
    void parseWhileStmtThrows() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.WHILE_KEYWORD, new Location()),
                new Token(TokenType.LPAREN, new Location()),
                new Token(TokenType.RPAREN, new Location()),
                new Token(TokenType.LBRACE, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
                new Token(TokenType.SEMICOLON, new Location()),
                new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when, then
        assertThrows(ParserException.class, parser::parseWhileStmt);
        assertFalse(errorModule.getErrors().isEmpty());
    }

    @Test
    @SneakyThrows
    void parseForStmt() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.FOR_KEYWORD, new Location()),
            new Token(TokenType.LPAREN, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "iter"),
            new Token(TokenType.IN_KEYWORD, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var forStmt = (ForStmt) parser.parseForStmt();

        // then
        var iteratorIdentifier = forStmt.getIteratorIdentifier();
        var iterable = (IntegerConstantExpr) forStmt.getIterable();
        var codeBLock = (CodeBLock) forStmt.getCodeBLock();
        var codeBlockFirstStmt = (IntegerConstantExpr) codeBLock.getStatementsAndExpressions().get(0);
        assertEquals("iter", iteratorIdentifier);
        assertEquals(1, iterable.getValue());
        assertEquals(2, codeBlockFirstStmt.getValue());
    }

    @Test
    void parseForStmtThrows() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.FOR_KEYWORD, new Location()),
                new Token(TokenType.LPAREN, new Location()),
                new StringToken(TokenType.IDENTIFIER, new Location(), "iter"),
                new Token(TokenType.IN_KEYWORD, new Location()),
                new Token(TokenType.RPAREN, new Location()),
                new Token(TokenType.LBRACE, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
                new Token(TokenType.SEMICOLON, new Location()),
                new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when, then
        assertThrows(ParserException.class, parser::parseForStmt);
        assertFalse(errorModule.getErrors().isEmpty());
    }

    @Test
    @SneakyThrows
    void parseSwitchStmtDefault() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.SWITCH_KEYWORD, new Location()),
            new Token(TokenType.LPAREN, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),

            new Token(TokenType.DEFAULT_KEYWORD, new Location()),
            new Token(TokenType.ARROW, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location()),

            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var forStmt = (SwitchStmt) parser.parseSwitchStmt();

        // then
        var expr = (IntegerConstantExpr) forStmt.getExpression();
        assertEquals(1, expr.getValue());
        var switchElements = forStmt.getSwitchElements();
        var defaultLabel = new SwitchLabel("default");
        assertTrue(switchElements.containsKey(defaultLabel));
        var defFirstExpr = (IntegerConstantExpr) switchElements.get(defaultLabel).getStatementsAndExpressions().get(0);
        assertEquals(2, defFirstExpr.getValue());
    }

    @Test
    void parseSwitchStmtThrows() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
                new Token(TokenType.SWITCH_KEYWORD, new Location()),
                new Token(TokenType.LPAREN, new Location()),
                new Token(TokenType.RPAREN, new Location()),
                new Token(TokenType.LBRACE, new Location()),

                new Token(TokenType.DEFAULT_KEYWORD, new Location()),
                new Token(TokenType.ARROW, new Location()),
                new Token(TokenType.LBRACE, new Location()),
                new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
                new Token(TokenType.SEMICOLON, new Location()),
                new Token(TokenType.RBRACE, new Location()),

                new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when, then
        assertThrows(ParserException.class, parser::parseSwitchStmt);
        assertFalse(errorModule.getErrors().isEmpty());
    }

    @Test
    @SneakyThrows
    void parseSwitchStmtDefaultAndIntLabel() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new Token(TokenType.SWITCH_KEYWORD, new Location()),
            new Token(TokenType.LPAREN, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.RPAREN, new Location()),
            new Token(TokenType.LBRACE, new Location()),

            new Token(TokenType.DEFAULT_KEYWORD, new Location()),
            new Token(TokenType.ARROW, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location()),

            new StringToken(TokenType.IDENTIFIER, new Location(), "float"),
            new Token(TokenType.ARROW, new Location()),
            new Token(TokenType.LBRACE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 3),
            new Token(TokenType.SEMICOLON, new Location()),
            new Token(TokenType.RBRACE, new Location()),

            new Token(TokenType.RBRACE, new Location())
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var forStmt = (SwitchStmt) parser.parseSwitchStmt();

        // then
        var expr = (IntegerConstantExpr) forStmt.getExpression();
        assertEquals(1, expr.getValue());

        var switchElements = forStmt.getSwitchElements();
        var defaultLabel = new SwitchLabel("default");
        assertTrue(switchElements.containsKey(defaultLabel));
        var defFirstExpr = (IntegerConstantExpr) switchElements.get(defaultLabel).getStatementsAndExpressions().get(0);
        assertEquals(2, defFirstExpr.getValue());

        var floatLabel = new SwitchLabel("float");
        assertTrue(switchElements.containsKey(floatLabel));
        var floatFirstExpr = (IntegerConstantExpr) switchElements.get(floatLabel).getStatementsAndExpressions().get(0);
        assertEquals(3, floatFirstExpr.getValue());
    }

}