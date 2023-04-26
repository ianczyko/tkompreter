package com.anczykowski.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import com.anczykowski.parser.structures.expressions.AdditionTerm;
import com.anczykowski.parser.structures.expressions.AssignmentExpression;
import com.anczykowski.parser.structures.expressions.CastExpression;
import com.anczykowski.parser.structures.expressions.ClassInitExpression;
import com.anczykowski.parser.structures.expressions.DivisionFactor;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.structures.expressions.FunctionCallExpression;
import com.anczykowski.parser.structures.expressions.IdentifierExpression;
import com.anczykowski.parser.structures.expressions.IntegerConstantExpr;
import com.anczykowski.parser.structures.expressions.MultiplicationFactor;
import com.anczykowski.parser.structures.expressions.NegatedExpression;
import com.anczykowski.parser.structures.expressions.ObjectAccessExpression;
import com.anczykowski.parser.structures.expressions.StringExpression;
import com.anczykowski.parser.structures.expressions.SubtractionTerm;
import com.anczykowski.parser.structures.expressions.relops.EqRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.GeRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.GtRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.LeRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.LtRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.NeRelOpArg;
import com.anczykowski.parser.structures.statements.CondStmt;
import com.anczykowski.parser.structures.statements.ForStmt;
import com.anczykowski.parser.structures.statements.SwitchStmt;
import com.anczykowski.parser.structures.statements.VarStmt;
import com.anczykowski.parser.structures.statements.WhileStmt;

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
    // TODO: non empty code block

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
        assertEquals("aaa", first.getIdentifier());
        assertEquals("bbb", second.getIdentifier());
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
        assertEquals("aaa", first.getIdentifier());
        assertEquals("bbb", second.getIdentifier());
        assertEquals("ccc", third.getIdentifier());
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
    void parseClassDefinition() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(Arrays.asList(
            new Token(TokenType.CLASS_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "Circle"),
            new Token(TokenType.LBRACE, new Location()),
            new Token(TokenType.VAR_KEYWORD, new Location()),
            new StringToken(TokenType.IDENTIFIER, new Location(), "field1"),
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
        var expr = (AdditionTerm) parser.parseRelOpArg();

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
        var expr = (SubtractionTerm) parser.parseRelOpArg();

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
        var expr = (AdditionTerm) parser.parseRelOpArg();

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
        var expr = (MultiplicationFactor) parser.parseRelOpArg();

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
    void parseEq() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.EQ, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (EqRelOpArg) parser.parseAndOpArg();

        // then
        var left = (IntegerConstantExpr) expr.getLeft();
        var right = (IntegerConstantExpr) expr.getRight();
        assertEquals(1, left.getValue());
        assertEquals(2, right.getValue());
    }

    @Test
    @SneakyThrows
    void parseNe() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.NE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (NeRelOpArg) parser.parseAndOpArg();

        // then
        var left = (IntegerConstantExpr) expr.getLeft();
        var right = (IntegerConstantExpr) expr.getRight();
        assertEquals(1, left.getValue());
        assertEquals(2, right.getValue());
    }

    @Test
    @SneakyThrows
    void parseLt() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.LT, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (LtRelOpArg) parser.parseAndOpArg();

        // then
        var left = (IntegerConstantExpr) expr.getLeft();
        var right = (IntegerConstantExpr) expr.getRight();
        assertEquals(1, left.getValue());
        assertEquals(2, right.getValue());
    }

    @Test
    @SneakyThrows
    void parseLe() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.LE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (LeRelOpArg) parser.parseAndOpArg();

        // then
        var left = (IntegerConstantExpr) expr.getLeft();
        var right = (IntegerConstantExpr) expr.getRight();
        assertEquals(1, left.getValue());
        assertEquals(2, right.getValue());
    }

    @Test
    @SneakyThrows
    void parseGt() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.GT, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (GtRelOpArg) parser.parseAndOpArg();

        // then
        var left = (IntegerConstantExpr) expr.getLeft();
        var right = (IntegerConstantExpr) expr.getRight();
        assertEquals(1, left.getValue());
        assertEquals(2, right.getValue());
    }

    @Test
    @SneakyThrows
    void parseGe() {
        // given
        var errorModule = new ErrorModule();

        var lexer = ParserHelpers.thereIsLexer(List.of(
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 1),
            new Token(TokenType.GE, new Location()),
            new IntegerToken(TokenType.INTEGER_NUMBER, new Location(), 2)
        ));
        var parser = new Parser(lexer, errorModule);

        // when
        var expr = (GeRelOpArg) parser.parseAndOpArg();

        // then
        var left = (IntegerConstantExpr) expr.getLeft();
        var right = (IntegerConstantExpr) expr.getRight();
        assertEquals(1, left.getValue());
        assertEquals(2, right.getValue());
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
        parser.parseExprInsideCodeBlock(statementsAndExpressions);

        // then
        var expr = statementsAndExpressions.get(0);
        assertTrue(expr.isReturn());
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
    void parseAssignmentExpression() {
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
        var expr = (AssignmentExpression) statementsAndExpressions.get(0);
        var lval = (IntegerConstantExpr) expr.getLval();
        var rval = (IntegerConstantExpr) expr.getRval();
        assertEquals(1, lval.getValue());
        assertEquals(2, rval.getValue());
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
    @SneakyThrows
    void parseForStmt() {
        // given
        var errorModule = new ErrorModule();
        var variables = new HashMap<String, VarStmt>();

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
        var forStmt = (ForStmt) parser.parseForStmt(variables);

        // then
        var iterator = (VarStmt) forStmt.getIterator();
        var iterable = (IntegerConstantExpr) forStmt.getIterable();
        var codeBLock = (CodeBLock) forStmt.getCodeBLock();
        var codeBlockFirstStmt = (IntegerConstantExpr) codeBLock.getStatementsAndExpressions().get(0);
        assertEquals("iter", iterator.getName());
        assertEquals(1, iterable.getValue());
        assertEquals(2, codeBlockFirstStmt.getValue());
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
        assertTrue(switchElements.containsKey("default"));
        var defFirstExpr = (IntegerConstantExpr) switchElements.get("default").getStatementsAndExpressions().get(0);
        assertEquals(2, defFirstExpr.getValue());
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
        assertTrue(switchElements.containsKey("default"));
        var defFirstExpr = (IntegerConstantExpr) switchElements.get("default").getStatementsAndExpressions().get(0);
        assertEquals(2, defFirstExpr.getValue());

        assertTrue(switchElements.containsKey("float"));
        var floatFirstExpr = (IntegerConstantExpr) switchElements.get("float").getStatementsAndExpressions().get(0);
        assertEquals(3, floatFirstExpr.getValue());
    }

}