package com.anczykowski.interpreter;

import com.anczykowski.interpreter.value.Value;
import com.anczykowski.interpreter.value.ValueProxy;

import java.util.HashMap;

public class Context {
    HashMap<String, ValueProxy> variables = new HashMap<>();

    public void addVariable(String variable, ValueProxy value) {
        variables.put(variable, value);
    }
}
