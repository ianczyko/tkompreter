package com.anczykowski.parser.structures.expressions;

import com.anczykowski.parser.visitors.Visitable;
import com.anczykowski.parser.visitors.Visitor;
import lombok.Getter;

public class Expression implements Visitable {

    @Getter
    private boolean isReturn = false;

    public void setReturn(boolean isReturn) {
        this.isReturn = isReturn;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
