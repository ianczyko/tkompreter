package com.anczykowski.parser.structures.statements;

import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WhileStmt extends Statement {

    @Getter
    private final Expression condition;

    @Getter
    private final CodeBLock codeBLock;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
