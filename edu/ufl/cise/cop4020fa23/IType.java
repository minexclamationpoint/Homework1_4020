package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

public interface IType {
    
    AST TypeCheck(AST ast) throws TypeCheckException;
}
