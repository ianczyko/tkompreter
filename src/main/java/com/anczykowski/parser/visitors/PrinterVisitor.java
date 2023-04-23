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
import com.anczykowski.parser.structures.expressions.Arg;
import com.anczykowski.parser.structures.expressions.AssignmentExpression;
import com.anczykowski.parser.structures.expressions.CastExpression;
import com.anczykowski.parser.structures.expressions.ClassInitExpression;
import com.anczykowski.parser.structures.expressions.DivisionFactor;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.structures.expressions.FloatConstantExpr;
import com.anczykowski.parser.structures.expressions.FunctionCallExpression;
import com.anczykowski.parser.structures.expressions.IdentifierExpression;
import com.anczykowski.parser.structures.expressions.IntegerConstantExpr;
import com.anczykowski.parser.structures.expressions.MultiplicationFactor;
import com.anczykowski.parser.structures.expressions.NegatedExpression;
import com.anczykowski.parser.structures.expressions.ObjectAccessExpression;
import com.anczykowski.parser.structures.expressions.OrExpression;
import com.anczykowski.parser.structures.expressions.OrOpArg;
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
import com.anczykowski.parser.structures.statements.Statement;
import com.anczykowski.parser.structures.statements.SwitchStmt;
import com.anczykowski.parser.structures.statements.VarStmt;
import com.anczykowski.parser.structures.statements.WhileStmt;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PrinterVisitor implements Visitor {

    private final PrintStream out;

    private int level = 1;

    String additionalInfo = "";

    private void printIndentation() {
        printIndentation("-");
    }

    private void printIndentation(String repeatedString) {
        out.printf("%s ", repeatedString.repeat(level));
        consumeAdditionalInfo();
    }

    private void consumeAdditionalInfo() {
        if (!additionalInfo.isEmpty()) {
            out.printf("(%s) ", additionalInfo);
            additionalInfo = "";
        }
    }

    private void printIsReturnable(Expression expr) {
        if (expr.isReturn()) out.print("(with return) ");
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
        printIsReturnable(expression);
        out.println("expression: ");
    }

    @Override
    public void visit(IdentifierExpression identifierExpression) {
        printIndentation();
        printIsReturnable(identifierExpression);
        out.println("identifierExpression: " + identifierExpression.getIdentifier());
    }

    @Override
    public void visit(ObjectAccessExpression objectAccessExpression) {
        printIndentation();
        printIsReturnable(objectAccessExpression);
        out.println("objectAccessExpression: ");
        level++;
        objectAccessExpression.getCurrent().accept(this);
        objectAccessExpression.getChild().accept(this);
        level--;
    }

    @Override
    public void visit(Arg arg) {
        printIndentation();
        printIsReturnable(arg);
        out.println("arg: " + (arg.isByReference() ? "(by reference)" : ""));
        level++;
        arg.getArgument().accept(this);
        level--;
    }

    @Override
    public void visit(FunctionCallExpression functionCallExpression) {
        printIndentation();
        printIsReturnable(functionCallExpression);
        out.println("functionCallExpression: " + functionCallExpression.getIdentifier());
        level++;
        functionCallExpression.getArgs().forEach(arg -> arg.accept(this));
        level--;
    }

    @Override
    public void visit(ClassInitExpression classInitExpression) {
        printIndentation();
        printIsReturnable(classInitExpression);
        out.println("classInitExpression: " + classInitExpression.getIdentifier());
        level++;
        classInitExpression.getArgs().forEach(arg -> arg.accept(this));
        level--;
    }

    @Override
    public void visit(OrOpArg orOpArg) {
        printIndentation();
        printIsReturnable(orOpArg);
        out.println("orOpArg");
        level++;
        orOpArg.getLeft().accept(this);
        orOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(OrExpression orExpression) {
        printIndentation();
        printIsReturnable(orExpression);
        out.println("orOpArg");
        level++;
        orExpression.getLeft().accept(this);
        orExpression.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(AndOpArg andOpArg) {
        printIndentation();
        printIsReturnable(andOpArg);
        out.println("andOpArg");
        level++;
        andOpArg.getLeft().accept(this);
        andOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(EqRelOpArg eqRelOpArg) {
        printIndentation();
        printIsReturnable(eqRelOpArg);
        out.println("eqRelOpArg");
        level++;
        eqRelOpArg.getLeft().accept(this);
        eqRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(GeRelOpArg geRelOpArg) {
        printIndentation();
        printIsReturnable(geRelOpArg);
        out.println("geRelOpArg");
        level++;
        geRelOpArg.getLeft().accept(this);
        geRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(GtRelOpArg gtRelOpArg) {
        printIndentation();
        printIsReturnable(gtRelOpArg);
        out.println("gtRelOpArg");
        level++;
        gtRelOpArg.getLeft().accept(this);
        gtRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(LtRelOpArg ltRelOpArg) {
        printIndentation();
        printIsReturnable(ltRelOpArg);
        out.println("ltRelOpArg");
        level++;
        ltRelOpArg.getLeft().accept(this);
        ltRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(NeRelOpArg neRelOpArg) {
        printIndentation();
        printIsReturnable(neRelOpArg);
        out.println("neRelOpArg");
        level++;
        neRelOpArg.getLeft().accept(this);
        neRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(LeRelOpArg leRelOpArg) {
        printIndentation();
        printIsReturnable(leRelOpArg);
        out.println("leRelOpArg");
        level++;
        leRelOpArg.getLeft().accept(this);
        leRelOpArg.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(AdditionTerm additionTerm) {
        printIndentation();
        printIsReturnable(additionTerm);
        out.println("additionTerm");
        level++;
        additionTerm.getLeft().accept(this);
        additionTerm.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(SubtractionTerm subtractionTerm) {
        printIndentation();
        printIsReturnable(subtractionTerm);
        out.println("subtractionTerm");
        level++;
        subtractionTerm.getLeft().accept(this);
        subtractionTerm.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(MultiplicationFactor multiplicationFactor) {
        printIndentation();
        printIsReturnable(multiplicationFactor);
        out.println("multiplicationFactor");
        level++;
        multiplicationFactor.getLeft().accept(this);
        multiplicationFactor.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(DivisionFactor divisionFactor) {
        printIndentation();
        printIsReturnable(divisionFactor);
        out.println("divisionFactor");
        level++;
        divisionFactor.getLeft().accept(this);
        divisionFactor.getRight().accept(this);
        level--;
    }

    @Override
    public void visit(IntegerConstantExpr integerConstantExpr) {
        printIndentation();
        printIsReturnable(integerConstantExpr);
        out.println("integerConstantExpr: " + integerConstantExpr.getValue());
    }

    @Override
    public void visit(FloatConstantExpr floatConstantExpr) {
        printIndentation();
        printIsReturnable(floatConstantExpr);
        out.println("floatConstantExpr: " + floatConstantExpr.getValue());
    }

    @Override
    public void visit(NegatedExpression negatedExpression) {
        printIndentation();
        printIsReturnable(negatedExpression);
        out.println("negatedExpression: ");
        if (negatedExpression.getInner() != null) {
            level++;
            negatedExpression.getInner().accept(this);
            level--;
        }
    }

    @Override
    public void visit(AssignmentExpression assignmentExpression) {
        printIndentation();
        printIsReturnable(assignmentExpression);
        out.println("assignmentExpression: ");
        level++;
        additionalInfo = "lval";
        assignmentExpression.getLval().accept(this);
        additionalInfo = "rval";
        assignmentExpression.getRval().accept(this);
        level--;
    }

    @Override
    public void visit(StringExpression stringExpression) {
        printIndentation();
        printIsReturnable(stringExpression);
        out.println("stringExpression: " + stringExpression.getValue());
    }

    @Override
    public void visit(CastExpression castExpression) {
        printIndentation();
        printIsReturnable(castExpression);
        out.printf("castedExpression: (casting to: %s)%n", castExpression.getType());
        level++;
        castExpression.getInner().accept(this);
        level--;
    }

    @Override
    public void visit(CondStmt condStmt) {
        printIndentation();
        printIsReturnable(condStmt);
        out.println("condStmt: ");
        level++;
        additionalInfo = "cond";
        condStmt.getCondition().accept(this);
        additionalInfo = "block";
        condStmt.getTrueBlock().accept(this);
        if (condStmt.getElseBlock() != null) {
        additionalInfo = "elseBlock";
            condStmt.getElseBlock().accept(this);
        }
        level--;
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        printIndentation();
        printIsReturnable(whileStmt);
        out.println("whileStmt: ");
        level++;
        whileStmt.getCondition().accept(this);
        whileStmt.getCodeBLock().accept(this);
        level--;
    }

    @Override
    public void visit(ForStmt forStmt) {
        printIndentation();
        printIsReturnable(forStmt);
        out.println("forStmt: ");
        level++;
        additionalInfo = "iterator";
        forStmt.getIterator().accept(this);
        additionalInfo = "iterable";
        forStmt.getIterable().accept(this);
        additionalInfo = "block";
        forStmt.getCodeBLock().accept(this);
        level--;
    }

    @Override
    public void visit(SwitchStmt switchStmt) {
        printIndentation();
        printIsReturnable(switchStmt);
        out.println("switchStmt: ");
        level++;
        switchStmt.getExpression().accept(this);
        level++;
        switchStmt.getSwitchElements().forEach((attrKey, attr) -> {
            additionalInfo = attrKey;
            attr.accept(this);
        });
        level--;
        level--;
    }
}
