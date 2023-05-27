package com.anczykowski.parser.structures;

import com.anczykowski.visitors.Visitable;
import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class FuncDef implements Visitable {
    @Getter
    private final String name;

    @Getter
    private final ArrayList<Parameter> params;

    @Getter
    private final CodeBLock codeBLock;

    @Getter
    @Setter
    private Boolean isMethod = false;

    public FuncDef(String name, ArrayList<Parameter> params, CodeBLock codeBLock) {
        this.name = name;
        this.params = params;
        this.codeBLock = codeBLock;
    }

    public FuncDef(String name, ArrayList<Parameter> params, CodeBLock codeBLock, Boolean isMethod) {
        this.name = name;
        this.params = params;
        this.codeBLock = codeBLock;
        this.isMethod = isMethod;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
