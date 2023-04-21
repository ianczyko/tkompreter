package com.anczykowski.parser.visitors;

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
import com.anczykowski.parser.structures.expressions.SubtractionTerm;
import com.anczykowski.parser.structures.expressions.relops.EqRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.GeRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.GtRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.LeRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.LtRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.NeRelOpArg;
import com.anczykowski.parser.structures.statements.Statement;
import com.anczykowski.parser.structures.statements.VarStmt;

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

    void visit(EqRelOpArg orOpArg);

    void visit(NeRelOpArg orOpArg);

    void visit(GtRelOpArg orOpArg);

    void visit(GeRelOpArg orOpArg);

    void visit(LtRelOpArg orOpArg);

    void visit(LeRelOpArg orOpArg);

    void visit(AdditionTerm orOpArg);

    void visit(SubtractionTerm orOpArg);

    void visit(MultiplicationFactor orOpArg);

    void visit(IntegerConstantExpr orOpArg);

    void visit(FloatConstantExpr orOpArg);

    void visit(DivisionFactor orOpArg);
}
