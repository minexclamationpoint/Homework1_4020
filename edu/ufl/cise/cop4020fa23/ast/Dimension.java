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
public class Dimension extends AST {

	final Expr width;
	final Expr height;
	
	
	
	/**
	 * @param firstToken
	 * @param width
	 * @param height
	 */
	public Dimension(IToken firstToken, Expr width, Expr height) {
		super(firstToken);
		this.width = width;
		this.height = height;
	}



	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitDimension(this, arg);
	}



	/**
	 * @return the width
	 */
	public Expr getWidth() {
		return width;
	}



	/**
	 * @return the height
	 */
	public Expr getHeight() {
		return height;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(height, width);
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
		Dimension other = (Dimension) obj;
		return Objects.equals(height, other.height) && Objects.equals(width, other.width);
	}



	@Override
	public String toString() {
		return "Dimension [width=" + width + ", height=" + height + "]";
	}

	
}
