package com.anczykowski.visitors;

import com.anczykowski.parser.structures.*;
import com.anczykowski.parser.structures.expressions.*;
import com.anczykowski.parser.structures.expressions.relops.*;
import com.anczykowski.parser.structures.statements.*;
import lombok.RequiredArgsConstructor;

import java.io.PrintStream;

@RequiredArgsConstructor
public class PrinterVisitor implements Visitor {

    private final PrintStream out;

    private int level = 1;

    private void printIndentation() {
        printIndentation("-");
    }

    private void printIndentation(String repeatedString) {
        out.printf("%s ", repeatedString.repeat(level));
    }

    @Override
    public void visit(Program program) {
        printIndentation();
        out.println("program");
        level++;
        program.getClasses().values().forEach(cls -> cls.accept(this));
        program.getFunctions().values().forEach(fun -> fun.accept(this));
        level--;
    }


    @Override
    public void visit(ClassDef classDef) {
        printIndentation();
        out.println("classDef: " + classDef.getName());
        level++;
        classDef.getClassBody().accept(this);
        level--;
    }

    @Override
    public void visit(FuncDef funcDef) {
        printIndentation("*");
        out.printf("funcDef: %s ", funcDef.getName());
        printIndentation("*");
        out.println();
        level++;
        funcDef.getParams().forEach(method -> method.accept(this));
        funcDef.getCodeBLock().accept(this);
        level--;
    }

    @Override
    public void visit(ClassBody classBody) {
        printIndentation();
        out.println("classBody: ");
        level++;
        classBody.getAttributes().values().forEach(attr -> attr.accept(this));
        classBody.getMethods().values().forEach(method -> method.accept(this));
        level--;
    }

    @Override
    public void visit(VarStmt varStmt) {
        printIndentation();
        out.println("varStmt: " + varStmt.getName());
        if (varStmt.getInitial() != null) {
            level++;
            varStmt.getInitial().accept(this);
            level--;
        }

    }

    @Override
    public void visit(Parameter parameter) {
        printIndentation();
        out.println("parameter: " + parameter.getName());
    }

    @Override
    public void visit(CodeBLock codeBLock) {
        printIndentation();
        out.println("codeBLock: ");
        level++;
        codeBLock.getStatements().forEach(statement -> statement.accept(this));
        level--;
    }

    @Override
    public void visit(Statement statement) {
        printIndentation();
        out.println("statement: ");
    }

    @Override
    public void visit(Expression expression) {
        printIndentation();
        out.println("expression: ");
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        printIndentation();
        out.println("identifierExpression: " + identifierExpression.getIdentifier());
    }

    @Override
    public void visit(ObjectAccessExpression objectAccessExpression) {
        printIndentation();
        out.println("objectAccessExpression: ");
        level++;
        objectAccessExpression.getCurrent().accept(this);
        objectAccessExpression.getChild().accept(this);
        level--;
    }

    @Override
    public void visit(Arg arg) {
        printIndentation();
        out.println("arg: " + (arg.isByReference() ? "(by reference)" : ""));
        level++;
        arg.getArgument().accept(this);
        level--;
    }

    @Override
    public void visit(FunctionCallExpression functionCallExpression) {
        printIndentation();
        out.println("functionCallExpression: " + functionCallExpression.getIdentifier());
        level++;
        functionCallExpression.getArgs().forEach(arg -> arg.accept(this));
        level--;
    }

    @Override
    public void visit(ClassInitExpression classInitExpression) {
        printIndentation();
        out.println("classInitExpression: " + classInitExpression.getIdentifier());
        level++;
        classInitExpression.getArgs().forEach(arg -> arg.accept(this));
        level--;
    }

    @Override
    public void visit(AndExpr andExpr) {
        printIndentation();
        out.println("orOpArg");
        level++;
        andExpr.getLeft().accept(this);
        andExpr.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(OrExpression orExpression) {
        printIndentation();
        out.println("orOpArg");
        level++;
        orExpression.getLeft().accept(this);
        orExpression.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(EqRelExpr eqRelExpr) {
        printIndentation();
        out.println("eqRelOpArg");
        level++;
        eqRelExpr.getLeft().accept(this);
        eqRelExpr.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(GeRelExpr geRelOpArg) {
        printIndentation();
        out.println("geRelOpArg");
        level++;
        geRelOpArg.getLeft().accept(this);
        geRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(GtRelExpr gtRelOpArg) {
        printIndentation();
        out.println("gtRelOpArg");
        level++;
        gtRelOpArg.getLeft().accept(this);
        gtRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(LtRelExpr ltRelOpArg) {
        printIndentation();
        out.println("ltRelOpArg");
        level++;
        ltRelOpArg.getLeft().accept(this);
        ltRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(NeRelExpr neRelOpArg) {
        printIndentation();
        out.println("neRelOpArg");
        level++;
        neRelOpArg.getLeft().accept(this);
        neRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(LeRelExpr leRelOpArg) {
        printIndentation();
        out.println("leRelOpArg");
        level++;
        leRelOpArg.getLeft().accept(this);
        leRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(AdditionTerm additionTerm) {
        printIndentation();
        out.println("additionTerm");
        level++;
        additionTerm.getLeft().accept(this);
        additionTerm.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(SubtractionTerm subtractionTerm) {
        printIndentation();
        out.println("subtractionTerm");
        level++;
        subtractionTerm.getLeft().accept(this);
        subtractionTerm.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(MultiplicationFactor multiplicationFactor) {
        printIndentation();
        out.println("multiplicationFactor");
        level++;
        multiplicationFactor.getLeft().accept(this);
        multiplicationFactor.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(DivisionFactor divisionFactor) {
        printIndentation();
        out.println("divisionFactor");
        level++;
        divisionFactor.getLeft().accept(this);
        divisionFactor.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(IntegerConstantExpr integerConstantExpr) {
        printIndentation();
        out.println("integerConstantExpr: " + integerConstantExpr.getValue());
    }

    @Override
    public void visit(FloatConstantExpr floatConstantExpr) {
        printIndentation();
        out.println("floatConstantExpr: " + floatConstantExpr.getValue());
    }

    @Override
    public void visit(NegatedExpression negatedExpression) {
        printIndentation();
        out.println("negatedExpression: ");
        if (negatedExpression.getInner() != null) {
            level++;
            negatedExpression.getInner().accept(this);
            level--;
        }
    }

    @Override
    public void visit(AssignmentStatement assignmentStatement) {
        printIndentation();
        out.println("assignmentExpression: (lval/rval) ");
        level++;
        assignmentStatement.getLval().accept(this);
        assignmentStatement.getRval().accept(this);
        level--;
    }

    @Override
    public void visit(ExpressionStatement expressionStatement) {
        printIndentation();
        out.println("expressionStatement: ");
        level++;
        expressionStatement.getExpression().accept(this);
        level--;
    }

    @Override
    public void visit(StringExpression stringExpression) {
        printIndentation();
        out.println("stringExpression: " + stringExpression.getValue());
    }

    @Override
    public void visit(CastExpression castExpression) {
        printIndentation();
        out.printf("castedExpression: (casting to: %s)%n", castExpression.getType());
        level++;
        castExpression.getInner().accept(this);
        level--;
    }

    @Override
    public void visit(SwitchLabel switchLabel) {
        printIndentation();
        out.println("switchLabel: " + switchLabel.getLabel());
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        printIndentation();
        out.println("returnExpression: ");
        level++;
        if(returnStatement.getInner() != null){
            returnStatement.getInner().accept(this);
        }
        level--;
    }

    @Override
    public void visit(CondStmt condStmt) {
        printIndentation();
        out.println("condStmt: (cond/block/[elseBlock])");
        level++;
        condStmt.getCondition().accept(this);
        condStmt.getTrueBlock().accept(this);
        if (condStmt.getElseBlock() != null) {
            condStmt.getElseBlock().accept(this);
        }
        level--;
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        printIndentation();
        out.println("whileStmt: ");
        level++;
        whileStmt.getCondition().accept(this);
        whileStmt.getCodeBLock().accept(this);
        level--;
    }

    @Override
    public void visit(ForStmt forStmt) {
        printIndentation();
        out.printf("forStmt: iterator=[%s] (iterable/block)%n", forStmt.getIteratorIdentifier());
        level++;
        forStmt.getIterable().accept(this);
        forStmt.getCodeBLock().accept(this);
        level--;
    }

    @Override
    public void visit(SwitchStmt switchStmt) {
        printIndentation();
        out.println("switchStmt: ");
        level++;
        switchStmt.getExpression().accept(this);
        level++;
        switchStmt.getSwitchElements().forEach((attrKey, attr) -> {
            attrKey.accept(this);
            attr.accept(this);
        });
        level--;
        level--;
    }
}
