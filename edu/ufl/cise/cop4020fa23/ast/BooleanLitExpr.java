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

public class BooleanLitExpr extends Expr {

	/**
	 * @param firstToken
	 */
	public BooleanLitExpr(IToken firstToken) {
		super(firstToken);
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitBooleanLitExpr(this, arg);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		return true;
	}

	public String getText() {
		return firstToken.text();
	}

	@Override
	public String toString() {
		return "BooleanLitExpr [" + getText() + "]";
	}

}
