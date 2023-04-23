package com.anczykowski.parser.visitors;

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

    void visit(StringExpression stringExpression);
}
