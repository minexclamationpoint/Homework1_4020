package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

public class TypeCheckVisitor implements ASTVisitor {
    private SymbolTable st = new SymbolTable();
    private Program root;

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        root = program;
        Type type = Type.kind2type(program.getTypeToken().kind());
        program.setType(type);
        st.enterScope();
        // ... Visit children nodes
        st.leaveScope();
        return type;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        st.enterScope();
        // ... Visit children nodes
        st.leaveScope();
        return block;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        // Implementation here
        // ... Check constraints, visit children
        return assignmentStatement;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        // Implementation here
        // ... Check constraints, visit children
        return binaryExpr;
    }

    // ... similar methods for the rest of the AST nodes

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        // Implementation here
        // ... Check constraints, visit children
        return declaration;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        // Implementation here
        // ... Check constraints, insert into SymbolTable
        return nameDef;
    }

    // ... and so on for all methods in the ASTVisitor interface

    private void check(boolean condition, AST node, String message) throws TypeCheckException {
        if (!condition) {
            throw new TypeCheckException("Type error at " + node + ": " + message);
        }
    }
}
