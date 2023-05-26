package com.anczykowski.visitors;

import com.anczykowski.errormodule.ErrorElement;
import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.errormodule.exceptions.InterpreterException;
import com.anczykowski.interpreter.Context;
import com.anczykowski.interpreter.ContextManager;
import com.anczykowski.interpreter.PrintCodeBlock;
import com.anczykowski.interpreter.value.*;
import com.anczykowski.parser.structures.*;
import com.anczykowski.parser.structures.expressions.*;
import com.anczykowski.parser.structures.expressions.relops.*;
import com.anczykowski.parser.structures.statements.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

// TODO: void funkcja ma czyścić lastResult
// TODO: "fun();" bez przypisania ma również czyścić lastResult
// TODO: błędy powinny wskazywać lokalizację a najlepiej jeszcze fragment kodu

public class InterpreterVisitor implements Visitor {

    private final ErrorModule errorModule;

    private final PrintStream printStream;

    protected final ContextManager contextManager = new ContextManager();

    protected boolean isReturn = false;

    protected ValueProxy lastResult = null;

    ArrayList<Value> argumentsEvaluated = new ArrayList<>();

    public InterpreterVisitor(ErrorModule errorModule, PrintStream printStream) {
        this.errorModule = errorModule;
        this.printStream = printStream;
    }

    public InterpreterVisitor(ErrorModule errorModule) {
        this.errorModule = errorModule;
        this.printStream = System.out;
    }

    @Override
    public void visit(Program program) {
        contextManager.getGlobalSymbolManager().addFunctions(program.getFunctions());
        contextManager.getGlobalSymbolManager().addClasses(program.getClasses());

        loadBultins();

        var mainFunctionCall = new FunctionCallExpression("main", new ArrayList<>());
        mainFunctionCall.accept(this);
    }

    protected void loadBultins(){
        contextManager.getGlobalSymbolManager().addFunctions(new HashMap<>() {{
            put("print", new FuncDef(
                    "print",
                    new ArrayList<>() {{
                        add(new Parameter("valueToPrint"));
                    }},
                    new PrintCodeBlock()
            ));
        }});
    }

    @Override // TODO classDef
    public void visit(ClassDef classDef) {
        classDef.getClassBody().accept(this);
    }

    @Override
    public void visit(FuncDef funcDef) {
        if (funcDef.getParams().size() != argumentsEvaluated.size()) {
            errorModule.addError(ErrorElement.builder()
                    .errorType(ErrorType.UNMATCHED_ARGUMENTS)
                    .explanation("expected %d arguments but %d provided.".formatted(funcDef.getParams().size(), argumentsEvaluated.size()))
                    .build());
        }
        var newContext = new Context(true);
        var paramIterator = funcDef.getParams().iterator();
        var argIterator = argumentsEvaluated.iterator();
        while (paramIterator.hasNext() && argIterator.hasNext()) {
            var param = paramIterator.next();
            var argValue = argIterator.next();
            newContext.addVariable(param.getName(), new ValueProxy(argValue));
        }
        argumentsEvaluated.clear();
        contextManager.addContext(newContext);
        funcDef.getCodeBLock().accept(this);
        contextManager.popContext();
        isReturn = false;
    }

    @Override
    public void visit(FunctionCallExpression functionCallExpression) {
        for (Arg arg : functionCallExpression.getArgs()) {
            arg.accept(this);
            argumentsEvaluated.add(lastResult.getValue());
        }
        contextManager.getGlobalSymbolManager().getFunction(functionCallExpression.getIdentifier()).accept(this);
    }

    @Override // TODO classBody
    public void visit(ClassBody classBody) {
        classBody.getAttributes().values().forEach(attr -> attr.accept(this));
        classBody.getMethods().values().forEach(method -> method.accept(this));
    }

    @Override
    public void visit(VarStmt varStmt) {
        varStmt.getInitial().accept(this);
        contextManager.addVariable(varStmt.getName(), lastResult);
        lastResult = null;
    }

    @Override
    public void visit(Parameter parameter) {
    }

    @Override
    public void visit(CodeBLock codeBLock) {
        for (Statement statement : codeBLock.getStatements()) {
            statement.accept(this);
            if (isReturn) return;
        }
    }

    @Override
    public void visit(PrintCodeBlock printCodeBlock) {
        var valueToPrint = contextManager.getVariable("valueToPrint");
        printStream.println(valueToPrint.getValue().toString());
    }

    @Override
    public void visit(Statement statement) {
    }

    @Override
    public void visit(Expression expression) {
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        var declaredVariable = contextManager.getVariable(identifierExpression.getIdentifier());
        if (declaredVariable != null) {
            lastResult = declaredVariable;
        } else {
            errorModule.addError(ErrorElement.builder()
                    .errorType(ErrorType.UNDECLARED_VARIABLE)
                    .build()
            );
            throw new InterpreterException();
        }
    }

    @Override // TODO objectAccessExpression
    public void visit(ObjectAccessExpression objectAccessExpression) {
        objectAccessExpression.getCurrent().accept(this);
        objectAccessExpression.getChild().accept(this);
    }

    @Override
    public void visit(Arg arg) {
        arg.getArgument().accept(this);
    }

    @Override // TODO classInitExpression
    public void visit(ClassInitExpression classInitExpression) {
        classInitExpression.getArgs().forEach(arg -> arg.accept(this));
    }

    private void evaluateLeftRightNumerical(
            LeftRightExpression leftRightExpression,
            String operationName,
            BinaryOperator<Integer> integerOperation,
            BinaryOperator<Float> floatOperation
    ) {
        leftRightExpression.getLeft().accept(this);
        var leftValue = lastResult.getValue();
        lastResult = null;
        leftRightExpression.getRight().accept(this);
        var rightValue = lastResult.getValue();
        lastResult = null;
        if (leftValue instanceof IntValue left && rightValue instanceof IntValue right) {
            lastResult = new ValueProxy(new IntValue(integerOperation.apply(left.getValue(), right.getValue())));
        } else if (leftValue instanceof FloatValue left && rightValue instanceof FloatValue right) {
            lastResult = new ValueProxy(new FloatValue(floatOperation.apply(left.getValue(), right.getValue())));
        } else {
            errorModule.addError(ErrorElement.builder()
                    .errorType(ErrorType.UNSUPPORTED_OPERATION)
                    .explanation("%s is only supported on object of the same type. You may need to cast one of the expressions first.".formatted(operationName))
                    .build()
            );
        }
    }

    private void evaluateLeftRightRelational(
            LeftRightExpression leftRightExpression,
            String operationName,
            BiFunction<Integer, Integer, Boolean> integerOperation,
            BiFunction<Float, Float, Boolean> floatOperation
    ) {
        leftRightExpression.getLeft().accept(this);
        var leftValue = lastResult.getValue();
        lastResult = null;
        leftRightExpression.getRight().accept(this);
        var rightValue = lastResult.getValue();
        lastResult = null;
        if (leftValue instanceof IntValue left && rightValue instanceof IntValue right) {
            lastResult = new ValueProxy(new BoolValue(integerOperation.apply(left.getValue(), right.getValue())));
        } else if (leftValue instanceof FloatValue left && rightValue instanceof FloatValue right) {
            lastResult = new ValueProxy(new BoolValue(floatOperation.apply(left.getValue(), right.getValue())));
        } else {
            errorModule.addError(ErrorElement.builder()
                    .errorType(ErrorType.UNSUPPORTED_OPERATION)
                    .explanation("%s is only supported on object of the same type. You may need to cast one of the expressions first.".formatted(operationName))
                    .build()
            );
        }
    }

    private void evaluateLeftRightBinary(
            LeftRightExpression leftRightExpression,
            String operationName,
            BinaryOperator<Boolean> booleanOperation
    ) {
        leftRightExpression.getLeft().accept(this);
        var leftValue = lastResult.getValue();
        lastResult = null;
        leftRightExpression.getRight().accept(this);
        var rightValue = lastResult.getValue();
        lastResult = null;
        if (leftValue instanceof BoolValue left && rightValue instanceof BoolValue right) {
            lastResult = new ValueProxy(new BoolValue(booleanOperation.apply(left.getValue(), right.getValue())));
        } else {
            errorModule.addError(ErrorElement.builder()
                    .errorType(ErrorType.UNSUPPORTED_OPERATION)
                    .explanation("%s is only supported on object of the same type. You may need to cast one of the expressions first.".formatted(operationName))
                    .build()
            );
        }
    }

    @Override
    public void visit(AndExpr andExpr) {
        evaluateLeftRightBinary(andExpr, "and", (a, b) -> a && b);
    }

    @Override
    public void visit(OrExpression orExpression) {
        evaluateLeftRightBinary(orExpression, "or", (a, b) -> a || b);
    }

    @Override
    public void visit(EqRelExpr eqRelExpr) {
        evaluateLeftRightRelational(eqRelExpr, "eq", Integer::equals, Float::equals);
    }

    @Override
    public void visit(GeRelExpr geRelExpr) {
        evaluateLeftRightRelational(geRelExpr, "ge", (a, b) -> a >= b, (a, b) -> a >= b);
    }

    @Override
    public void visit(GtRelExpr gtRelExpr) {
        evaluateLeftRightRelational(gtRelExpr, "gt", (a, b) -> a > b, (a, b) -> a > b);
    }

    @Override
    public void visit(LtRelExpr ltRelExpr) {
        evaluateLeftRightRelational(ltRelExpr, "lt", (a, b) -> a < b, (a, b) -> a < b);
    }

    @Override
    public void visit(LeRelExpr leRelExpr) {
        evaluateLeftRightRelational(leRelExpr, "le", (a, b) -> a <= b, (a, b) -> a <= b);
    }

    @Override
    public void visit(NeRelExpr neRelOpArg) {
        evaluateLeftRightRelational(neRelOpArg, "ne", (a, b) -> !a.equals(b), (a, b) -> !a.equals(b));
    }

    @Override
    @SuppressWarnings("Convert2MethodRef")
    public void visit(AdditionTerm additionTerm) {
        evaluateLeftRightNumerical(additionTerm, "addition", (a, b) -> a + b, (a, b) -> a + b);
    }

    @Override
    public void visit(SubtractionTerm subtractionTerm) {
        evaluateLeftRightNumerical(subtractionTerm, "subtraction", (a, b) -> a - b, (a, b) -> a - b);
    }

    @Override
    public void visit(MultiplicationFactor multiplicationFactor) {
        evaluateLeftRightNumerical(multiplicationFactor, "multiplication", (a, b) -> a * b, (a, b) -> a * b);
    }

    @Override
    public void visit(DivisionFactor divisionFactor) {
        evaluateLeftRightNumerical(divisionFactor, "division", (a, b) -> {
            if (b == 0) {
                errorModule.addError(ErrorElement.builder().errorType(ErrorType.DIVISION_BY_ZERO).build());
                throw new InterpreterException();
            }
            return a / b;
        }, (a, b) -> {
            if (b.compareTo(0.0f) == 0) {
                errorModule.addError(ErrorElement.builder().errorType(ErrorType.DIVISION_BY_ZERO).build());
                throw new InterpreterException();
            }
            return a / b;
        });
    }

    @Override
    public void visit(IntegerConstantExpr integerConstantExpr) {
        lastResult = new ValueProxy(new IntValue(integerConstantExpr.getValue()));
    }

    @Override
    public void visit(FloatConstantExpr floatConstantExpr) {
        lastResult = new ValueProxy(new FloatValue(floatConstantExpr.getValue()));
    }

    @Override
    public void visit(NegatedExpression negatedExpression) {
        if (negatedExpression.getInner() != null) {
            negatedExpression.getInner().accept(this);
            if (lastResult.getValue() instanceof IntValue intValue) {
                lastResult = new ValueProxy(new IntValue(-intValue.getValue()));
            } else if (lastResult.getValue() instanceof FloatValue floatValue) {
                lastResult = new ValueProxy(new FloatValue(-floatValue.getValue()));
            } else if (lastResult.getValue() instanceof BoolValue boolValue) {
                lastResult = new ValueProxy(new BoolValue(!boolValue.getValue()));
            }
        }
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement) {
        assignmentStatement.getLval().accept(this);
        var lval = lastResult;
        lastResult = null;
        assignmentStatement.getRval().accept(this);
        var rval = lastResult.getValue();
        lastResult = null;
        lval.setValue(rval);
    }

    @Override // TODO expressionStatement
    public void visit(ExpressionStatement expressionStatement) {
        expressionStatement.getExpression().accept(this);
    }

    @Override
    public void visit(StringExpression stringExpression) {
        lastResult = new ValueProxy(new StringValue(stringExpression.getValue()));
    }

    @Override
    public void visit(CastExpression castExpression) {
        castExpression.getInner().accept(this);
        switch (castExpression.getType()) {
            case "int" -> {
                if (lastResult.getValue() instanceof IntValue intValue) {
                    lastResult = new ValueProxy(new IntValue(intValue.getValue()));
                } else if (lastResult.getValue() instanceof FloatValue floatValue) {
                    lastResult = new ValueProxy(new IntValue((int) floatValue.getValue()));
                } else {
                    errorModule.addError(ErrorElement.builder()
                            .errorType(ErrorType.UNSUPPORTED_OPERATION)
                            .explanation("Can only cast int/float to int")
                            .build());
                }
            }
            case "float" -> {
                if (lastResult.getValue() instanceof IntValue intValue) {
                    lastResult = new ValueProxy(new FloatValue((float) intValue.getValue()));
                } else if (lastResult.getValue() instanceof FloatValue floatValue) {
                    lastResult = new ValueProxy(new FloatValue(floatValue.getValue()));
                } else {
                    errorModule.addError(ErrorElement.builder()
                            .errorType(ErrorType.UNSUPPORTED_OPERATION)
                            .explanation("Can only cast int/float to int")
                            .build());
                }
            }
        }
    }

    @Override
    public void visit(SwitchLabel switchLabel) {
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        if (returnStatement.getInner() != null) {
            returnStatement.getInner().accept(this);
            isReturn = true;
        }
    }

    @Override
    public void visit(CondStmt condStmt) {
        condStmt.getCondition().accept(this);
        var evaluatedCondition = lastResult.getValue();
        lastResult = null;
        if (evaluatedCondition instanceof BoolValue boolCondition) {
            if (boolCondition.getValue()) {
                contextManager.addContext(new Context());
                condStmt.getTrueBlock().accept(this);
                contextManager.popContext();
            } else {
                if (condStmt.getElseBlock() != null) {
                    contextManager.addContext(new Context());
                    condStmt.getElseBlock().accept(this);
                    contextManager.popContext();
                }
            }
        }
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        while (checkCondition(whileStmt.getCondition())) {
            contextManager.addContext(new Context());
            whileStmt.getCodeBLock().accept(this);
            contextManager.popContext();
            if (isReturn) {
                break;
            }
        }
    }

    private boolean checkCondition(Expression condition) {
        condition.accept(this);
        var conditionEvaluated = lastResult;
        lastResult = null;
        if (conditionEvaluated.getValue() instanceof BoolValue conditionEvaluatedBoolean) {
            return conditionEvaluatedBoolean.getValue();
        }
        errorModule.addError(ErrorElement.builder()
                .errorType(ErrorType.UNSUPPORTED_OPERATION)
                .explanation("condition in while statement must be evaluable as boolean value.")
                .build());
        return false;
    }

    @Override  // TODO forStmt
    public void visit(ForStmt forStmt) {
        forStmt.getIterable().accept(this);
        contextManager.addContext(new Context());
        forStmt.getCodeBLock().accept(this);
        contextManager.popContext();
    }

    @Override  // TODO switchStmt
    public void visit(SwitchStmt switchStmt) {
        switchStmt.getExpression().accept(this);
        switchStmt.getSwitchElements().forEach((attrKey, attr) -> {
            attrKey.accept(this);
            contextManager.addContext(new Context());
            attr.accept(this);
            contextManager.popContext();
        });
    }
}
