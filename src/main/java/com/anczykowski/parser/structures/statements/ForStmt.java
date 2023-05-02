package com.anczykowski.parser.structures.statements;

import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ForStmt extends Statement {

    @Getter
    private final String iteratorIdentifier;

    @Getter
    private final Expression iterable;

    @Getter
    private final CodeBLock codeBLock;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
