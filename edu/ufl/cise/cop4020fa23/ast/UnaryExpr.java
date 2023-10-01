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

public class UnaryExpr extends Expr {

	final IToken op;
	final Expr e;

	/**
	 * @param firstToken
	 * @param op
	 * @param e
	 */
	public UnaryExpr(IToken firstToken, IToken op, Expr e) {
		super(firstToken);
		this.op = op;
		this.e = e;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitUnaryExpr(this, arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(e, op);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		UnaryExpr other = (UnaryExpr) obj;
		return Objects.equals(e, other.e) && Objects.equals(op, other.op);
	}

	public Kind getOp() {
		return op.kind();
	}

	public Expr getExpr() {
		return e;
	}

}
