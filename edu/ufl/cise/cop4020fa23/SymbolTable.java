package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.Map;
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
        boolean isComplete;

        public ScopeEntry(NameDef nameDef, int scopeSerial) {
            this.nameDef = nameDef;
            this.scopeSerial = scopeSerial;
            this.isComplete = isComplete;
        }
    }

    public void enterScope() {
        currentNum = nextNum++;
        scopeStack.push(currentNum);
    }

    public void leaveScope() {
        if(scopeStack.isEmpty()) {
            return;
        }
        int leavingScope = scopeStack.pop();
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
            return null;
        }

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            int currentScopeSerial = scopeStack.get(i);
            for (ScopeEntry entry : chain) {
                if (entry.scopeSerial == currentScopeSerial) {
                    return entry.nameDef;
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SymbolTable { \n");
        for (Map.Entry<String, Stack<ScopeEntry>> entry : table.entrySet()) {
            sb.append("  Identifier: ").append(entry.getKey()).append("\n");
            Stack<ScopeEntry> stack = entry.getValue();
            for (ScopeEntry scopeEntry : stack) {
                sb.append("    Scope: ").append(scopeEntry.scopeSerial)
                .append(", NameDef: ").append(scopeEntry.nameDef.toString()).append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public int getCurrentNum(){
        return currentNum;
    }

    public int getScopeOfSymbol(String name) {
        Stack<ScopeEntry> chain = table.get(name);
        if (chain == null) {
            return -1;
        }

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            int currentScopeSerial = scopeStack.get(i);
            for (ScopeEntry entry : chain) {
                if (entry.scopeSerial == currentScopeSerial) {
                    return currentScopeSerial;
                }
            }
        }
        return -1;
    }

}
