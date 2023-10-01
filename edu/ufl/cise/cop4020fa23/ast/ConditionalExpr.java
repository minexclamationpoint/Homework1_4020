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

public class ConditionalExpr extends Expr {

	final Expr guard;
	final Expr trueExpr;
	final Expr falseExpr;

	/**
	 * @param firstToken
	 * @param guard
	 * @param trueExpr
	 * @param falseExpr
	 */
	public ConditionalExpr(IToken firstToken, Expr guard, Expr trueExpr, Expr falseExpr) {
		super(firstToken);
		this.guard = guard;
		this.trueExpr = trueExpr;
		this.falseExpr = falseExpr;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitConditionalExpr(this, arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(falseExpr, guard, trueExpr);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		ConditionalExpr other = (ConditionalExpr) obj;
		return Objects.equals(falseExpr, other.falseExpr) && Objects.equals(guard, other.guard)
				&& Objects.equals(trueExpr, other.trueExpr);
	}

	@Override
	public String toString() {
		return "ConditionalExpr [\n  guard=" + guard + ",\n   trueExpr=" + trueExpr + ",\n   falseExpr=" + falseExpr
				+ "]";
	}

	public Expr getGuardExpr() {
		return guard;
	}

	public Expr getTrueExpr() {
		return trueExpr;
	}

	public Expr getFalseExpr() {
		return falseExpr;
	}
}
