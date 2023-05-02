package com.anczykowski.parser.visitors;

import com.anczykowski.parser.structures.*;
import com.anczykowski.parser.structures.expressions.*;
import com.anczykowski.parser.structures.expressions.relops.*;
import com.anczykowski.parser.structures.statements.*;

public interface Visitor {
    void visit(Program program);

    void visit(ClassDef classDef);

    void visit(FuncDef funcDef);

    void visit(ClassBody classBody);

    void visit(VarStmt varStmt);

    void visit(Parameter parameter);

    void visit(CodeBLock codeBLock);

    void visit(Statement statement);

    void visit(Expression expression);

    void visit(OrOpArg orOpArg);

    void visit(OrExpression orExpression);

    void visit(AndOpArg andOpArg);

    void visit(EqRelOpArg eqRelOpArg);

    void visit(NeRelOpArg neRelOpArg);

    void visit(GtRelOpArg gtRelOpArg);

    void visit(GeRelOpArg geRelOpArg);

    void visit(LtRelOpArg ltRelOpArg);

    void visit(LeRelOpArg leRelOpArg);

    void visit(AdditionTerm additionTerm);

    void visit(SubtractionTerm subtractionTerm);

    void visit(MultiplicationFactor multiplicationFactor);

    void visit(IntegerConstantExpr integerConstantExpr);

    void visit(FloatConstantExpr floatConstantExpr);

    void visit(DivisionFactor divisionFactor);

    void visit(NegatedExpression negatedExpression);

    void visit(AssignmentExpression assignmentExpression);

    void visit(CondStmt condStmt);

    void visit(WhileStmt whileStmt);

    void visit(ForStmt forStmt);

    void visit(SwitchStmt switchStmt);

    void visit(IdentifierExpression identifierExpression);

    void visit(ObjectAccessExpression identifierExpression);

    void visit(Arg arg);

    void visit(FunctionCallExpression functionCallExpression);

    void visit(ClassInitExpression classInitExpression);

    void visit(StringExpression stringExpression);

    void visit(CastExpression castExpression);

    void visit(SwitchLabel switchLabel);
}
