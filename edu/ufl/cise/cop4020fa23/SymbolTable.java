package edu.ufl.cise.cop4020fa23;

import java.util.HashMap;
import java.util.Stack;
import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

public class SymbolTable {
    private HashMap<String, NameDef> table = new HashMap<>();
    private Stack<Integer> scopeStack = new Stack<>();
    private int currentNum = 0;
    private int nextNum = 1;

    public void enterScope() {
        currentNum = nextNum++;
        scopeStack.push(currentNum);
    }

    public void leaveScope() {
        currentNum = scopeStack.pop();
    }

    public void insert(NameDef nameDef) throws TypeCheckException {
        // Implement checks to ensure name is not already defined in current scope
        // For simplicity, this example uses just the NameDef class. You would need to extend this
        table.put(nameDef.getName(), nameDef);
    }

    public NameDef lookup(String name) {
        // Implement logic to look up name in current scope
        return table.get(name);
    }
}
