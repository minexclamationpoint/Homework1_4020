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
public class ExpandedPixelExpr extends Expr {

	final Expr red;
	final Expr green;
	final Expr blue;

	/**
	 * @param firstToken
	 * @param red
	 * @param green
	 * @param blue
	 */
	public ExpandedPixelExpr(IToken firstToken, Expr red, Expr grn, Expr blu) {
		super(firstToken);
		this.red = red;
		this.green = grn;
		this.blue = blu;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(blue, green, red);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		ExpandedPixelExpr other = (ExpandedPixelExpr) obj;
		return Objects.equals(blue, other.blue) && Objects.equals(green, other.green) && Objects.equals(red, other.red);
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitExpandedPixelExpr(this, arg);
	}

	public Expr getRed() {
		return red;
	}

	public Expr getGreen() {
		return green;
	}

	public Expr getBlue() {
		return blue;
	}

	@Override
	public String toString() {
		return "ExpandedPixelExpr [red=" + red + ", green=" + green + ", blue=" + blue + "]";
	}

}