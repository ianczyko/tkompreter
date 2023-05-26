package com.anczykowski.visitors;

import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.errormodule.exceptions.InterpreterException;
import com.anczykowski.interpreter.Context;
import com.anczykowski.interpreter.value.BoolValue;
import com.anczykowski.interpreter.value.FloatValue;
import com.anczykowski.interpreter.value.IntValue;
import com.anczykowski.interpreter.value.ValueProxy;
import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.FuncDef;
import com.anczykowski.parser.structures.Parameter;
import com.anczykowski.parser.structures.expressions.*;
import com.anczykowski.parser.structures.expressions.relops.*;
import com.anczykowski.parser.structures.statements.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterVisitorTest {
    @Test
    void interpretIntegerAddition() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new AdditionTerm(
                new IntegerConstantExpr(2),
                new IntegerConstantExpr(1)
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(3, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatAddition() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new AdditionTerm(
                new FloatConstantExpr(2.0f),
                new FloatConstantExpr(1.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(3.0f, ((FloatValue) interpreter.lastResult.getValue()).getValue(), 0.000001f);
    }

    @Test
    void interpretUnsupportedAddition() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new AdditionTerm(
                new IntegerConstantExpr(2),
                new FloatConstantExpr(1.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNSUPPORTED_OPERATION, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    void interpretIntegerSubtraction() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new SubtractionTerm(
                new IntegerConstantExpr(2),
                new IntegerConstantExpr(1)
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(1, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatSubtraction() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new SubtractionTerm(
                new FloatConstantExpr(2.0f),
                new FloatConstantExpr(1.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(1.0f, ((FloatValue) interpreter.lastResult.getValue()).getValue(), 0.000001f);
    }

    @Test
    void interpretIntegerMult() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new MultiplicationFactor(
                new IntegerConstantExpr(2),
                new IntegerConstantExpr(3)
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(6, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatMult() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new MultiplicationFactor(
                new FloatConstantExpr(2.0f),
                new FloatConstantExpr(3.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(6.0f, ((FloatValue) interpreter.lastResult.getValue()).getValue(), 0.000001f);
    }

    @Test
    void interpretIntegerDiv() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new DivisionFactor(
                new IntegerConstantExpr(6),
                new IntegerConstantExpr(3)
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(2, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatDiv() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new DivisionFactor(
                new FloatConstantExpr(6.0f),
                new FloatConstantExpr(3.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(2.0f, ((FloatValue) interpreter.lastResult.getValue()).getValue(), 0.000001f);
    }

    @Test
    void interpretIntegerEq() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new EqRelExpr(
                new IntegerConstantExpr(1),
                new IntegerConstantExpr(1)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatEq() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new EqRelExpr(
                new FloatConstantExpr(1.0f),
                new FloatConstantExpr(1.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretUnsupportedEq() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new EqRelExpr(
                new IntegerConstantExpr(2),
                new FloatConstantExpr(1.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNSUPPORTED_OPERATION, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    void interpretIntegerNe() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new NeRelExpr(
                new IntegerConstantExpr(1),
                new IntegerConstantExpr(2)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatNe() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new NeRelExpr(
                new FloatConstantExpr(1.0f),
                new FloatConstantExpr(2.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretIntegerLt() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new LtRelExpr(
                new IntegerConstantExpr(1),
                new IntegerConstantExpr(2)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatLt() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new LtRelExpr(
                new FloatConstantExpr(1.0f),
                new FloatConstantExpr(2.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretIntegerLe() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new LeRelExpr(
                new IntegerConstantExpr(1),
                new IntegerConstantExpr(2)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatLe() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new LeRelExpr(
                new FloatConstantExpr(1.0f),
                new FloatConstantExpr(2.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretIntegerGt() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new GtRelExpr(
                new IntegerConstantExpr(2),
                new IntegerConstantExpr(1)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatGt() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new GtRelExpr(
                new FloatConstantExpr(2.0f),
                new FloatConstantExpr(1.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretIntegerGe() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new GeRelExpr(
                new IntegerConstantExpr(2),
                new IntegerConstantExpr(1)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretFloatGe() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new GeRelExpr(
                new FloatConstantExpr(2.0f),
                new FloatConstantExpr(1.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretAnd() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new AndExpr(
                new EqRelExpr(
                        new IntegerConstantExpr(1),
                        new IntegerConstantExpr(1)
                ),
                new EqRelExpr(
                        new IntegerConstantExpr(1),
                        new IntegerConstantExpr(1)
                )
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretOr() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new OrExpression(
                new EqRelExpr(
                        new IntegerConstantExpr(1),
                        new IntegerConstantExpr(1)
                ),
                new EqRelExpr(
                        new IntegerConstantExpr(1),
                        new IntegerConstantExpr(1)
                )
        );

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretUnsupportedOr() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new OrExpression(
                new FloatConstantExpr(2.0f),
                new FloatConstantExpr(1.0f)
        );

        // when
        expr.accept(interpreter);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNSUPPORTED_OPERATION, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    void interpretNegatedInt() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new NegatedExpression(new IntegerConstantExpr(1));

        // when
        expr.accept(interpreter);

        // then
        assertEquals(-1, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretNegatedFloat() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new NegatedExpression(new FloatConstantExpr(1.0f));

        // when
        expr.accept(interpreter);

        // then
        assertEquals(-1.0f, ((FloatValue) interpreter.lastResult.getValue()).getValue(), 0.000001f);
    }

    @Test
    void interpretNegatedBool() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new NegatedExpression(new EqRelExpr(
                new IntegerConstantExpr(1),
                new IntegerConstantExpr(2)
        ));

        // when
        expr.accept(interpreter);

        // then
        assertTrue(((BoolValue) interpreter.lastResult.getValue()).getValue());
    }


    @Test
    void interpretCastIntToFloat() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new CastExpression(
                new IntegerConstantExpr(1),
                "float"
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(1.0f, ((FloatValue) interpreter.lastResult.getValue()).getValue(), 0.000001f);
    }

    @Test
    void interpretCastFloatToFloat() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new CastExpression(
                new FloatConstantExpr(1.0f),
                "float"
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(1.0f, ((FloatValue) interpreter.lastResult.getValue()).getValue(), 0.000001f);
    }

    @Test
    void interpretCastFloatToInt() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new CastExpression(
                new FloatConstantExpr(1.0f),
                "int"
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(1, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretCastIntToInt() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new CastExpression(
                new IntegerConstantExpr(1),
                "int"
        );

        // when
        expr.accept(interpreter);

        // then
        assertEquals(1, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void interpretCastStringToInt() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);

        var expr = new CastExpression(
                new StringExpression("abc"),
                "int"
        );

        // when
        expr.accept(interpreter);

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNSUPPORTED_OPERATION, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    void interpretVarStmt() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);
        interpreter.contextManager.addContext(new Context());


        var varStmt = new VarStmt("x", new IntegerConstantExpr(1));

        // when
        varStmt.accept(interpreter);

        // then
        assertEquals(1, ((IntValue) interpreter.contextManager.getVariable("x").getValue()).getValue());
    }

    @Test
    void interpretAssignment() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);
        interpreter.contextManager.addContext(new Context());


        var varStmt = new VarStmt("x", new IntegerConstantExpr(1));
        var assignmentStmt = new AssignmentStatement(new IdentifierExpression("x"), new IntegerConstantExpr(2));

        // when
        varStmt.accept(interpreter);
        assignmentStmt.accept(interpreter);

        // then
        assertEquals(2, ((IntValue) interpreter.contextManager.getVariable("x").getValue()).getValue());
    }

    @Test
    void testVarInOutsideLocalScope() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);
        interpreter.contextManager.addContext(new Context(true));
        interpreter.contextManager.addVariable("x", new ValueProxy(new IntValue(1)));
        interpreter.contextManager.addContext(new Context());


        var identifierExpression = new IdentifierExpression("x");

        // when
        identifierExpression.accept(interpreter);

        // then
        assertEquals(1, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void testVarInOutsideFunctionScope() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);
        interpreter.contextManager.addContext(new Context(true));
        interpreter.contextManager.addVariable("x", new ValueProxy(new IntValue(1)));
        interpreter.contextManager.addContext(new Context(true));


        var identifierExpression = new IdentifierExpression("x");

        assertThrows(InterpreterException.class, () -> {
            // when
            identifierExpression.accept(interpreter);
        });

        // then
        assertFalse(errorModule.getErrors().isEmpty());
        assertEquals(ErrorType.UNDECLARED_VARIABLE, errorModule.getErrors().get(0).getErrorType());
    }

    @Test
    void testReturnOutOfNestedScope() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);
        interpreter.contextManager.addContext(new Context(true));

        var funcDef = new FuncDef("x", new ArrayList<>(), new CodeBLock(
                new ArrayList<>() {{
                    add(
                            new CondStmt(
                                    new EqRelExpr(new IntegerConstantExpr(1), new IntegerConstantExpr(1)),
                                    new CodeBLock(
                                            new ArrayList<>() {{
                                                add(new ReturnStatement(new IntegerConstantExpr(1)));
                                                add(new ReturnStatement(new IntegerConstantExpr(2)));
                                            }}
                                    ),
                                    null
                            )
                    );
                    add(new ReturnStatement(new IntegerConstantExpr(3)));
                }}
        ));

        // when
        funcDef.accept(interpreter);

        // then
        assertEquals(1, ((IntValue) interpreter.lastResult.getValue()).getValue());

    }

    @Test
    void testWhileStmtAccumulation() {
        // given
        var errorModule = new ErrorModule();
        var interpreter = new InterpreterVisitor(errorModule);
        interpreter.contextManager.addContext(new Context(true));

        var block = new CodeBLock(new ArrayList<>() {{
            add(new VarStmt("x", new IntegerConstantExpr(1)));
            add(new WhileStmt(
                    new LtRelExpr(new IdentifierExpression("x"), new IntegerConstantExpr(5)),
                    new CodeBLock(new ArrayList<>() {{
                        add(new AssignmentStatement(
                                new IdentifierExpression("x"),
                                new AdditionTerm(new IdentifierExpression("x"), new IntegerConstantExpr(1))
                        ));
                    }})));
            add(new ReturnStatement(new IdentifierExpression("x")));
        }});

        // when
        block.accept(interpreter);

        // then
        assertEquals(5, ((IntValue) interpreter.lastResult.getValue()).getValue());

    }

    @Test
    void testBuiltInPrint() {
        // given
        var errorModule = new ErrorModule();
        var outputStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outputStream);
        var interpreter = new InterpreterVisitor(errorModule, printStream);

        interpreter.contextManager.addContext(new Context(true));
        interpreter.loadBultins();

        var block = new CodeBLock(new ArrayList<>() {{
            add(new ExpressionStatement(new FunctionCallExpression(
                    "print",
                    new ArrayList<>() {{
                        add(new Arg(new StringExpression("abc"), false));
                    }}
            )));
        }});

        // when
        block.accept(interpreter);

        // then
        assertTrue(outputStream.toString().contains("abc"));
    }

    @Test
    void testPassByRef() {
        // given
        var errorModule = new ErrorModule();
        var outputStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outputStream);
        var interpreter = new InterpreterVisitor(errorModule, printStream);

        interpreter.contextManager.addContext(new Context(true));
        interpreter.contextManager.getGlobalSymbolManager().addFunctions(new HashMap<>() {{
            put("increment", new FuncDef(
                    "increment",
                    new ArrayList<>() {{
                        add(new Parameter("x"));
                    }},
                    new CodeBLock(new ArrayList<>() {{
                        add(new AssignmentStatement(
                                new IdentifierExpression("x"),
                                new SubtractionTerm(new IdentifierExpression("x"),
                                        new IntegerConstantExpr(1)
                                )
                        ));
                    }})
            ));
        }});


        var block = new CodeBLock(new ArrayList<>() {{
            add(new VarStmt("x", new IntegerConstantExpr(5)));
            add(new ExpressionStatement(new FunctionCallExpression(
                    "increment",
                    new ArrayList<>() {{
                        add(new Arg(new IdentifierExpression("x"), true));
                    }}
            )));
            add(new ReturnStatement(new IdentifierExpression("x")));
        }});

        // when
        block.accept(interpreter);

        // then
        assertEquals(4, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

    @Test
    void testPassByCopy() {
        // given
        var errorModule = new ErrorModule();
        var outputStream = new ByteArrayOutputStream();
        var printStream = new PrintStream(outputStream);
        var interpreter = new InterpreterVisitor(errorModule, printStream);

        interpreter.contextManager.addContext(new Context(true));
        interpreter.contextManager.getGlobalSymbolManager().addFunctions(new HashMap<>() {{
            put("increment", new FuncDef(
                    "increment",
                    new ArrayList<>() {{
                        add(new Parameter("x"));
                    }},
                    new CodeBLock(new ArrayList<>() {{
                        add(new AssignmentStatement(
                                new IdentifierExpression("x"),
                                new SubtractionTerm(new IdentifierExpression("x"),
                                        new IntegerConstantExpr(1)
                                )
                        ));
                    }})
            ));
        }});


        var block = new CodeBLock(new ArrayList<>() {{
            add(new VarStmt("x", new IntegerConstantExpr(5)));
            add(new ExpressionStatement(new FunctionCallExpression(
                    "increment",
                    new ArrayList<>() {{
                        add(new Arg(new IdentifierExpression("x"), false));
                    }}
            )));
            add(new ReturnStatement(new IdentifierExpression("x")));
        }});

        // when
        block.accept(interpreter);

        // then
        assertEquals(5, ((IntValue) interpreter.lastResult.getValue()).getValue());
    }

}