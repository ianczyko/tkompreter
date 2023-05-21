package com.anczykowski.interpreter;

import com.anczykowski.interpreter.value.Value;

import java.util.HashMap;

public class Context {
    HashMap<String, Value> variables = new HashMap<>();

    public void addVariable(String variable, Value value) {
        variables.put(variable, value);
    }
}
