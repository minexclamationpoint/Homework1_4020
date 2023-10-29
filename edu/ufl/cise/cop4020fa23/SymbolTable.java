package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.Stack;
import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

public class SymbolTable {
    private HashMap<String, Stack<ScopeEntry>> table = new HashMap<>();
    private Stack<Integer> scopeStack = new Stack<>();
    private int currentNum = 0;
    private int nextNum = 1;

    private static class ScopeEntry {
        NameDef nameDef;
        int scopeSerial;

        public ScopeEntry(NameDef nameDef, int scopeSerial) {
            this.nameDef = nameDef;
            this.scopeSerial = scopeSerial;
        }
    }

    public void enterScope() {
        currentNum = nextNum++;
        scopeStack.push(currentNum);
    }

    public void leaveScope() {
        scopeStack.pop();
        if (!scopeStack.isEmpty()) {
            currentNum = scopeStack.peek();
        }
    }

    public void insert(NameDef nameDef) throws TypeCheckException {
        Stack<ScopeEntry> chain = table.getOrDefault(nameDef.getName(), new Stack<>());
        chain.push(new ScopeEntry(nameDef, currentNum));
        table.put(nameDef.getName(), chain);
    }

    public NameDef lookup(String name) {
        Stack<ScopeEntry> chain = table.get(name);
        if (chain == null) {
            return null; // Identifier not found in any scope
        }

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            int currentScopeSerial = scopeStack.get(i);
            for (ScopeEntry entry : chain) {
                if (entry.scopeSerial == currentScopeSerial) {
                    return entry.nameDef; // Identifier found in the current scope or an enclosing scope
                }
            }
        }
        return null; // Identifier not found in the current scope or any enclosing scopes
    }
}
