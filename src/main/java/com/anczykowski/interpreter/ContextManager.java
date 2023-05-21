package com.anczykowski.interpreter;

import java.util.ArrayList;
import java.util.Stack;

import com.anczykowski.interpreter.value.Value;
import lombok.Getter;


public class ContextManager {
    private final Stack<Context> contexts = new Stack<>();

    public void addContext(Context context){
        contexts.add(context);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Context popContext(){
        return contexts.pop();
    }

    @Getter
    private final SymbolManager globalSymbolManager = new SymbolManager();

    public void addVariable(String variable, Value value) {
        contexts.peek().addVariable(variable, value);
    }
}
