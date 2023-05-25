package com.anczykowski.interpreter;

import com.anczykowski.interpreter.value.ValueProxy;
import lombok.Getter;

import java.util.HashMap;

public class Context {
    HashMap<String, ValueProxy> variables = new HashMap<>();

    @Getter
    private final boolean isFunctionContext;

    public Context(boolean isFunctionContext) {
        this.isFunctionContext = isFunctionContext;
    }

    public Context() {
        this.isFunctionContext = false;
    }

    public void addVariable(String variable, ValueProxy value) {
        variables.put(variable, value);
    }
}
