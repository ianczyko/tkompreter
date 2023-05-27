package com.anczykowski.interpreter;

import com.anczykowski.interpreter.value.ValueProxy;
import lombok.Getter;

import java.util.HashMap;

public class Context {
    @Getter
    private final HashMap<String, ValueProxy> variables = new HashMap<>();

    @Getter
    private final SymbolManager localSymbolManager = new SymbolManager();

    @Getter
    private final boolean isBarrierContext;

    public Context(boolean isBarrierContext) {
        this.isBarrierContext = isBarrierContext;
    }

    public Context() {
        this.isBarrierContext = false;
    }

    public void addVariable(String variable, ValueProxy value) {
        variables.put(variable, value);
    }
}
