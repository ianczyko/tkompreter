package com.anczykowski.parser.structures.statements;

import java.util.Map;

import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.SwitchLabel;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SwitchStmt extends Statement {

    @Getter
    private final Expression expression;

    @Getter
    private final Map<SwitchLabel, CodeBLock> switchElements;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
