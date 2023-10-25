/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */
package edu.ufl.cise.cop4020fa23.ast;

import edu.ufl.cise.cop4020fa23.IToken;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

/**
 * 
 */
public class IdentExpr extends Expr {
	
	NameDef nameDef; //the name def declaring this ident.  Set during type checking, null until then. 

	public IdentExpr(IToken firstToken) {
		super(firstToken);
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitIdentExpr(this, arg);
	}
 
	public String getName() {
		return firstToken.text();
	}

	@Override
	public String toString() {
		return "IdentExpr [getName()=" + getName() + "]";
	}
	
	public NameDef getNameDef() {
		return nameDef;
	}
	
	public void setNameDef(NameDef def) {
		this.nameDef = def;
	}
}
