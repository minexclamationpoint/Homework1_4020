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

public abstract class AST {

	public final IToken firstToken;

	public AST(IToken firstToken) {
		super();
		this.firstToken = firstToken;
	}

	public abstract Object visit(ASTVisitor v, Object arg) throws PLCCompilerException;

	public IToken firstToken() {
		return firstToken;
	}

	public int line() {
		return firstToken.sourceLocation().line();
	}

	public int column() {
		return firstToken.sourceLocation().column();
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstToken);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		AST other = (AST) obj;
		return Objects.equals(firstToken, other.firstToken);
	}

}
