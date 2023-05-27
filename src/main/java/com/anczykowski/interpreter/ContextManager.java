package com.anczykowski.interpreter;

import com.anczykowski.interpreter.value.ValueProxy;
import com.anczykowski.parser.structures.FuncDef;
import lombok.Getter;

import java.util.ArrayDeque;


public class ContextManager {
    private final ArrayDeque<Context> contexts = new ArrayDeque<>();

    public void addContext(Context context) {
        contexts.add(context);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Context popContext() {
        return contexts.removeLast();
    }

    @Getter
    private final SymbolManager globalSymbolManager = new SymbolManager();


    public FuncDef getFunction(String function) {
        if (contexts.peekLast() != null) {
            var localDef = contexts.peekLast().getLocalSymbolManager().getFunction(function);
            if (localDef != null) {
                return localDef;
            }
        }
        return globalSymbolManager.getFunction(function);
    }

    public void addVariable(String variable, ValueProxy value) {
        if (contexts.peekLast() != null) {
            contexts.peekLast().addVariable(variable, value);
        }
    }

    @SuppressWarnings("unused")
    public ValueProxy getVariable(String variable) {
        var it = contexts.descendingIterator();
        boolean foundFunction = false;
        while(it.hasNext() && !foundFunction){
            var context = it.next();
            if (context.getVariables().containsKey(variable)) {
                return context.getVariables().get(variable);
            }
            if(context.isBarrierContext()){
                foundFunction = true;
            }
        }
        return null;
    }

}
