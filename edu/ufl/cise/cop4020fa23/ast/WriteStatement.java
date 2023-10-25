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

import java.util.Objects;

import edu.ufl.cise.cop4020fa23.IToken;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

/**
 * 
 */
public class WriteStatement extends Statement{
	
	final Expr expr;

	/**
	 * @param firstToken
	 * @param expr
	 */
	public WriteStatement(IToken firstToken, Expr expr) {
		super(firstToken);
		this.expr = expr;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitWriteStatement(this,  arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(expr);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		WriteStatement other = (WriteStatement) obj;
		return Objects.equals(expr, other.expr);
	}

	public Expr getExpr() {
		return expr;
	}

	@Override
	public String toString() {
		return "WriteStatement [expr=" + expr + "]";
	}


	
	

}
