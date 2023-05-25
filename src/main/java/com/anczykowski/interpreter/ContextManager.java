package com.anczykowski.interpreter;

import com.anczykowski.interpreter.value.ValueProxy;
import lombok.Getter;

import java.util.ArrayDeque;


public class ContextManager {
    private final ArrayDeque<Context> contexts = new ArrayDeque<>();

    public void addContext(Context context) {
        contexts.add(context);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Context popContext() {
        return contexts.pop();
    }

    @Getter
    private final SymbolManager globalSymbolManager = new SymbolManager();

    public void addVariable(String variable, ValueProxy value) {
        if (contexts.peek() != null) {
            contexts.peek().addVariable(variable, value);
        }
    }

    @SuppressWarnings("unused")
    public ValueProxy getVariable(String variable) {
        var it = contexts.descendingIterator();
        boolean foundFunction = false;
        while(it.hasNext() && !foundFunction){
            var context = it.next();
            if (context.variables.containsKey(variable)) {
                return context.variables.get(variable);
            }
            if(context.isFunctionContext()){
                foundFunction = true;
            }
        }
        return null;
    }

}
