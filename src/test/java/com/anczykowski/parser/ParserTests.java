package com.anczykowski.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.lexer.FloatToken;
import com.anczykowski.lexer.IntegerToken;
import com.anczykowski.lexer.Location;
import com.anczykowski.lexer.StringToken;
import com.anczykowski.lexer.Token;
import com.anczykowski.lexer.TokenType;
import com.anczykowski.parser.helpers.ParserHelpers;
import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.expressions.AdditionTerm;
import com.anczykowski.parser.structures.expressions.AssignmentExpression;
import com.anczykowski.parser.structures.expressions.DivisionFactor;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.structures.expressions.IntegerConstantExpr;
import com.anczykowski.parser.structures.expressions.MultiplicationFactor;
import com.anczykowski.parser.structures.expressions.NegatedExpression;
import com.anczykowski.parser.structures.expressions.SubtractionTerm;
import com.anczykowski.parser.structures.expressions.relops.EqRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.GeRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.GtRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.LeRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.LtRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.NeRelOpArg;
import com.anczykowski.parser.structures.statements.CondStmt;
import com.anczykowski.parser.structures.statements.ForStmt;
import com.anczykowski.parser.structures.statements.VarStmt;
import com.anczykowski.parser.structures.statements.WhileStmt;

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
        assertTrue(codeBLock.getStatementsAndExpressions().isEmpty());
    }

    // TODO: non empty code block

    @Test
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

}