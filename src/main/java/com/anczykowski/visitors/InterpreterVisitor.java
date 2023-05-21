package com.anczykowski.visitors;

import com.anczykowski.errormodule.ErrorElement;
import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.interpreter.Context;
import com.anczykowski.interpreter.ContextManager;
import com.anczykowski.interpreter.value.IntValue;
import com.anczykowski.interpreter.value.Value;
import com.anczykowski.parser.structures.*;
import com.anczykowski.parser.structures.expressions.*;
import com.anczykowski.parser.structures.expressions.relops.*;
import com.anczykowski.parser.structures.statements.*;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

// TODO: obsługa dzielenia przez 0
// TODO: void funkcja ma czyścić lastResult
// TODO: "fun();" bez przypisania ma również czyścić lastResult

@RequiredArgsConstructor
public class InterpreterVisitor implements Visitor {

    private final ErrorModule errorModule;

    ContextManager contextManager = new ContextManager();

    boolean isReturn = false;

    Value lastResult = null;

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

    @Override
    public void visit(AndExpr andExpr) {
        andExpr.getLeft().accept(this);
        andExpr.getRight().accept(this);
    }

    @Override
    public void visit(OrExpression orExpression) {
        orExpression.getLeft().accept(this);
        orExpression.getRight().accept(this);
    }

    @Override
    public void visit(EqRelExpr eqRelExpr) {
        eqRelExpr.getLeft().accept(this);
        eqRelExpr.getRight().accept(this);
    }

    @Override
    public void visit(GeRelExpr geRelOpArg) {
        geRelOpArg.getLeft().accept(this);
        geRelOpArg.getRight().accept(this);
    }

    @Override
    public void visit(GtRelExpr gtRelOpArg) {
        gtRelOpArg.getLeft().accept(this);
        gtRelOpArg.getRight().accept(this);
    }

    @Override
    public void visit(LtRelExpr ltRelOpArg) {
        ltRelOpArg.getLeft().accept(this);
        ltRelOpArg.getRight().accept(this);
    }

    @Override
    public void visit(NeRelExpr neRelOpArg) {
        neRelOpArg.getLeft().accept(this);
        neRelOpArg.getRight().accept(this);
    }

    @Override
    public void visit(LeRelExpr leRelOpArg) {
        leRelOpArg.getLeft().accept(this);
        leRelOpArg.getRight().accept(this);
    }

    @Override
    public void visit(AdditionTerm additionTerm) {
        additionTerm.getLeft().accept(this);
        additionTerm.getRight().accept(this);
    }

    @Override
    public void visit(SubtractionTerm subtractionTerm) {
        subtractionTerm.getLeft().accept(this);
        subtractionTerm.getRight().accept(this);
    }

    @Override
    public void visit(MultiplicationFactor multiplicationFactor) {
        multiplicationFactor.getLeft().accept(this);
        multiplicationFactor.getRight().accept(this);
    }

    @Override
    public void visit(DivisionFactor divisionFactor) {
        divisionFactor.getLeft().accept(this);
        divisionFactor.getRight().accept(this);
    }

    @Override
    public void visit(IntegerConstantExpr integerConstantExpr) {
        lastResult = new IntValue(integerConstantExpr.getValue());
    }

    @Override
    public void visit(FloatConstantExpr floatConstantExpr) {

    }

    @Override
    public void visit(NegatedExpression negatedExpression) {
        if (negatedExpression.getInner() != null) {
            negatedExpression.getInner().accept(this);
        }
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement) {
        assignmentStatement.getLval().accept(this);
        assignmentStatement.getRval().accept(this);
    }

    @Override
    public void visit(ExpressionStatement expressionStatement) {
        expressionStatement.getExpression().accept(this);
    }

    @Override
    public void visit(StringExpression stringExpression) {

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
