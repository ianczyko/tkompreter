package com.anczykowski.visitors;

import com.anczykowski.errormodule.ErrorElement;
import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.interpreter.Context;
import com.anczykowski.interpreter.ContextManager;
import com.anczykowski.interpreter.value.*;
import com.anczykowski.parser.structures.*;
import com.anczykowski.parser.structures.expressions.*;
import com.anczykowski.parser.structures.expressions.relops.*;
import com.anczykowski.parser.structures.statements.*;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

// TODO: obsługa dzielenia przez 0
// TODO: void funkcja ma czyścić lastResult
// TODO: "fun();" bez przypisania ma również czyścić lastResult

@RequiredArgsConstructor
public class InterpreterVisitor implements Visitor {

    private final ErrorModule errorModule;

    private final ContextManager contextManager = new ContextManager();

    protected boolean isReturn = false;

    protected Value lastResult = null;

    ArrayList<Value> argumentsEvaluated = new ArrayList<>();

    @Override
    public void visit(Program program) {
        contextManager.getGlobalSymbolManager().addFunctions(program.getFunctions());
        contextManager.getGlobalSymbolManager().addClasses(program.getClasses());

        var mainFunctionCall = new FunctionCallExpression("main", new ArrayList<>());
        mainFunctionCall.accept(this);
    }


    @Override
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
        var newContext = new Context();
        var paramIterator = funcDef.getParams().iterator();
        var argIterator = argumentsEvaluated.iterator();
        while (paramIterator.hasNext() && argIterator.hasNext()) {
            var param = paramIterator.next();
            var argValue = argIterator.next();
            newContext.addVariable(param.getName(), argValue);
        }
        contextManager.addContext(newContext);
        funcDef.getCodeBLock().accept(this);
        contextManager.popContext();
        isReturn = false;
    }

    @Override
    public void visit(FunctionCallExpression functionCallExpression) {
        argumentsEvaluated.clear();
        for (Arg arg : functionCallExpression.getArgs()) {
            arg.accept(this);
            argumentsEvaluated.add(lastResult);
        }
        contextManager.getGlobalSymbolManager().getFunction(functionCallExpression.getIdentifier()).accept(this);
    }

    @Override
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
        codeBLock.getStatements().forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(Statement statement) {

    }

    @Override
    public void visit(Expression expression) {

    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        lastResult = new IdentifierValue(identifierExpression.getIdentifier());
    }

    @Override
    public void visit(ObjectAccessExpression objectAccessExpression) {
        objectAccessExpression.getCurrent().accept(this);
        objectAccessExpression.getChild().accept(this);
    }

    @Override
    public void visit(Arg arg) {
        arg.getArgument().accept(this);
    }

    @Override
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
        var leftValue = lastResult;
        lastResult = null;
        leftRightExpression.getRight().accept(this);
        var rightValue = lastResult;
        lastResult = null;
        if (leftValue instanceof IntValue left && rightValue instanceof IntValue right) {
            lastResult = new IntValue(integerOperation.apply(left.getValue(), right.getValue()));
        } else if (leftValue instanceof FloatValue left && rightValue instanceof FloatValue right) {
            lastResult = new FloatValue(floatOperation.apply(left.getValue(), right.getValue()));
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
        var leftValue = lastResult;
        lastResult = null;
        leftRightExpression.getRight().accept(this);
        var rightValue = lastResult;
        lastResult = null;
        if (leftValue instanceof IntValue left && rightValue instanceof IntValue right) {
            lastResult = new BoolValue(integerOperation.apply(left.getValue(), right.getValue()));
        } else if (leftValue instanceof FloatValue left && rightValue instanceof FloatValue right) {
            lastResult = new BoolValue(floatOperation.apply(left.getValue(), right.getValue()));
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
        var leftValue = lastResult;
        lastResult = null;
        leftRightExpression.getRight().accept(this);
        var rightValue = lastResult;
        lastResult = null;
        if (leftValue instanceof BoolValue left && rightValue instanceof BoolValue right) {
            lastResult = new BoolValue(booleanOperation.apply(left.getValue(), right.getValue()));
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
        evaluateLeftRightNumerical(divisionFactor, "division", (a, b) -> a / b, (a, b) -> a / b);
    }

    @Override
    public void visit(IntegerConstantExpr integerConstantExpr) {
        lastResult = new IntValue(integerConstantExpr.getValue());
    }

    @Override
    public void visit(FloatConstantExpr floatConstantExpr) {
        lastResult = new FloatValue(floatConstantExpr.getValue());
    }

    @Override
    public void visit(NegatedExpression negatedExpression) {
        if (negatedExpression.getInner() != null) {
            negatedExpression.getInner().accept(this);
            if (lastResult instanceof IntValue intValue) {
                lastResult = new IntValue(-intValue.getValue());
            } else if (lastResult instanceof FloatValue floatValue){
                lastResult = new FloatValue(-floatValue.getValue());
            } else if (lastResult instanceof BoolValue boolValue){
                lastResult = new BoolValue(!boolValue.getValue());
            }
        }
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement) {
        assignmentStatement.getLval().accept(this);
        var lval = lastResult;
        assignmentStatement.getRval().accept(this);
        var rval = lastResult;
        lastResult = null;
        if (lval instanceof IdentifierValue identifierValue){
            contextManager.updateVariable(identifierValue.getValue(), rval);
        } else {
            errorModule.addError(ErrorElement.builder()
                    .errorType(ErrorType.UNDECLARED_VARIABLE)
                    .build()
            );
        }
    }

    @Override
    public void visit(ExpressionStatement expressionStatement) {
        expressionStatement.getExpression().accept(this);
    }

    @Override
    public void visit(StringExpression stringExpression) {
        lastResult = new StringValue(stringExpression.getValue());
    }

    @Override
    public void visit(CastExpression castExpression) {
        castExpression.getInner().accept(this);
    }

    @Override
    public void visit(SwitchLabel switchLabel) {

    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        if (returnStatement.getInner() != null) {
            returnStatement.getInner().accept(this);
        }
    }

    @Override
    public void visit(CondStmt condStmt) {
        condStmt.getCondition().accept(this);
        condStmt.getTrueBlock().accept(this);
        if (condStmt.getElseBlock() != null) {
            condStmt.getElseBlock().accept(this);
        }

    }

    @Override
    public void visit(WhileStmt whileStmt) {
        whileStmt.getCondition().accept(this);
        whileStmt.getCodeBLock().accept(this);
    }

    @Override
    public void visit(ForStmt forStmt) {
        forStmt.getIterable().accept(this);
        forStmt.getCodeBLock().accept(this);
    }

    @Override
    public void visit(SwitchStmt switchStmt) {
        switchStmt.getExpression().accept(this);
        switchStmt.getSwitchElements().forEach((attrKey, attr) -> {
            attrKey.accept(this);
            attr.accept(this);
        });
    }
}
