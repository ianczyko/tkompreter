package com.anczykowski.interpreter;

import com.anczykowski.interpreter.value.Value;
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

    public void addVariable(String variable, Value value) {
        if (contexts.peek() != null) {
            contexts.peek().addVariable(variable, value);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean updateVariable(String variable, Value value) {
        var it = contexts.descendingIterator();
        while(it.hasNext()){
            var context = it.next();
            if (context.variables.containsKey(variable)) {
                context.variables.put(variable, value);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public Value getVariable(String variable) {
        var it = contexts.descendingIterator();
        while(it.hasNext()){
            var context = it.next();
            if (context.variables.containsKey(variable)) {
                return context.variables.get(variable);
            }
        }
        return null;
    }

}
