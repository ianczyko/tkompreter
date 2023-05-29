package com.anczykowski.parser.structures.statements;

import com.anczykowski.lexer.Location;
import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class ForStmt extends Statement {

    @Getter
    private final String iteratorIdentifier;

    @Getter
    private final Expression iterable;

    @Getter
    private final CodeBLock codeBLock;

    @Getter
    private Location location = null;

    @Getter
    private String characterBuffer = null;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
