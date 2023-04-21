package com.anczykowski.parser.visitors;

import java.io.PrintStream;

import com.anczykowski.parser.structures.ClassBody;
import com.anczykowski.parser.structures.ClassDef;
import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.FuncDef;
import com.anczykowski.parser.structures.Parameter;
import com.anczykowski.parser.structures.Program;
import com.anczykowski.parser.structures.expressions.AdditionTerm;
import com.anczykowski.parser.structures.expressions.AndOpArg;
import com.anczykowski.parser.structures.expressions.DivisionFactor;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.structures.expressions.FloatConstantExpr;
import com.anczykowski.parser.structures.expressions.IntegerConstantExpr;
import com.anczykowski.parser.structures.expressions.MultiplicationFactor;
import com.anczykowski.parser.structures.expressions.OrExpression;
import com.anczykowski.parser.structures.expressions.OrOpArg;
import com.anczykowski.parser.structures.expressions.RelOpArg;
import com.anczykowski.parser.structures.expressions.SubtractionTerm;
import com.anczykowski.parser.structures.statements.Statement;
import com.anczykowski.parser.structures.statements.VarStmt;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PrinterVisitor implements Visitor {

    private final PrintStream out;

    private int level = 1;

    private void printIndentation() {
        out.printf("%s ", "-".repeat(level));
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
        printIndentation();
        out.println("funcDef: " + funcDef.getName());
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
        classBody.getMethods().values().forEach(method -> method.accept(this));
        classBody.getAttributes().values().forEach(attr -> attr.accept(this));
        level--;
    }

    @Override
    public void visit(VarStmt varStmt) {
        printIndentation();
        out.println("varStmt: " + varStmt.getName());
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
        codeBLock.getStatementsAndExpressions().forEach(statement -> statement.accept(this));
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
    public void visit(OrOpArg orOpArg) {
        printIndentation();
        out.println("orOpArg");
        level++;
        orOpArg.getLeft().accept(this);
        orOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(OrExpression orOpArg) {
        printIndentation();
        out.println("orOpArg");
        level++;
        orOpArg.getLeft().accept(this);
        orOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(AndOpArg andOpArg) {
        printIndentation();
        out.println("andOpArg");
        level++;
        andOpArg.getLeft().accept(this);
        andOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(RelOpArg relOpArg) {
        printIndentation();
        out.println("relOpArg");
        level++;
        relOpArg.getLeft().accept(this);
        relOpArg.getRight().accept(this);
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
}
