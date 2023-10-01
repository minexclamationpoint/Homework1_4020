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

public class PixelSelector extends AST {

	final Expr xExpr;
	final Expr yExpr;

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitPixelSelector(this, arg);
	}

	/**
	 * @param firstToken
	 * @param xExpr
	 * @param yExpr
	 */
	public PixelSelector(IToken firstToken, Expr xExpr, Expr yExpr) {
		super(firstToken);
		this.xExpr = xExpr;
		this.yExpr = yExpr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(xExpr, yExpr);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		PixelSelector other = (PixelSelector) obj;
		return Objects.equals(xExpr, other.xExpr) && Objects.equals(yExpr, other.yExpr);
	}

	@Override
	public String toString() {
		return "PixelSelector [xExpr=" + xExpr + ",\n   yExpr=" + yExpr + "]";
	}

	public Expr xExpr() {
		return xExpr;
	}

	public Expr yExpr() {
		return yExpr;
	}
}
