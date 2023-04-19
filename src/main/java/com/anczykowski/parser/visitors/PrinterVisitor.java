package com.anczykowski.parser.visitors;

import java.io.PrintStream;

import com.anczykowski.parser.structures.ClassBody;
import com.anczykowski.parser.structures.ClassDef;
import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.FuncDef;
import com.anczykowski.parser.structures.Parameter;
import com.anczykowski.parser.structures.Program;
import com.anczykowski.parser.structures.Statement;
import com.anczykowski.parser.structures.VarStmt;
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
    }

    @Override
    public void visit(Statement statement) {
        printIndentation();
        out.println("statement: ");
    }
}
