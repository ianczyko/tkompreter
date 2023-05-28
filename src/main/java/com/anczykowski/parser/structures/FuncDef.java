package com.anczykowski.parser.structures;

import com.anczykowski.visitors.Visitable;
import com.anczykowski.visitors.Visitor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@NoArgsConstructor
public class FuncDef implements Visitable {
    @Getter
    private String name = "";

    @Getter
    private ArrayList<Parameter> params = new ArrayList<>();

    @Getter
    private CodeBLock codeBLock = null;

    @Getter
    @Setter
    private Boolean isMethod = false;

    @Getter
    @Setter
    protected Boolean requireArgMatch = true;

    public FuncDef(String name, CodeBLock codeBLock) {
        this.name = name;
        this.codeBLock = codeBLock;
    }

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
