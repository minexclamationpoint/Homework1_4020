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
import edu.ufl.cise.cop4020fa23.Kind;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

public class BinaryExpr extends Expr {

	final Expr leftExpr;
	final IToken op;
	final Expr rightExpr;

	/**
	 * @param firstToken
	 * @param leftExpr
	 * @param rightExpr
	 */
	public BinaryExpr(IToken firstToken, Expr leftExpr, IToken op, Expr rightExpr) {
		super(firstToken);
		this.leftExpr = leftExpr;
		this.rightExpr = rightExpr;
		this.op = op;
	}

	/**
	 * @return the op
	 */
	public Kind getOpKind() {
		return op.kind();
	}

	public IToken getOp() {
		return op;
	}

	/**
	 * @return the leftExpr
	 */
	public Expr getLeftExpr() {
		return leftExpr;
	}

	/**
	 * @return the rightExpr
	 */
	public Expr getRightExpr() {
		return rightExpr;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitBinaryExpr(this, arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(leftExpr, rightExpr);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		BinaryExpr other = (BinaryExpr) obj;
		return Objects.equals(leftExpr, other.leftExpr) && Objects.equals(rightExpr, other.rightExpr);
	}

	@Override
	public String toString() {
		return "BinaryExpr [leftExpr=" + leftExpr + ", op=" + op.kind() + ", rightExpr=" + rightExpr + "]";
	}

}
