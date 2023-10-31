package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;
import java.util.logging.Logger;

public class SymbolTable {
    private static final Logger logger = Logger.getLogger(SymbolTable.class.getName());

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
        logger.info("Entered scope: " + currentNum);
    }

    public void leaveScope() {
        if(scopeStack.isEmpty()) {
            logger.warning("Attempt to leave scope when no scope has been entered");
            return;
        }
        int leavingScope = scopeStack.pop();
        logger.info("Leaving scope: " + leavingScope);
        if (!scopeStack.isEmpty()) {
            currentNum = scopeStack.peek();
        }
    }

    public void insert(NameDef nameDef) throws TypeCheckException {
        Stack<ScopeEntry> chain = table.getOrDefault(nameDef.getName(), new Stack<>());
        chain.push(new ScopeEntry(nameDef, currentNum));
        table.put(nameDef.getName(), chain);
        logger.info("Inserted " + nameDef.getName() + " into scope: " + currentNum);
    }

    public NameDef lookup(String name) {
        logger.info("Looking up " + name);
        Stack<ScopeEntry> chain = table.get(name);
        if (chain == null) {
            logger.warning("Identifier not found in any scope: " + name);
            return null;
        }

        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            int currentScopeSerial = scopeStack.get(i);
            for (ScopeEntry entry : chain) {
            logger.info("Comparing " + entry.scopeSerial + " with " + currentScopeSerial);
                if (entry.scopeSerial == currentScopeSerial) {
                    logger.info("Identifier found in scope: " + currentScopeSerial);
                    return entry.nameDef; 
                }
            }
        }

        logger.warning("Identifier not found in the current scope or any enclosing scopes: " + name);
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

    

}
