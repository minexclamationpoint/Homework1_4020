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

import java.util.List;
import java.util.Objects;

import edu.ufl.cise.cop4020fa23.IToken;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

public class Block extends AST {

	public abstract static class BlockElem extends AST {

		public BlockElem(IToken firstToken) {
			super(firstToken);
		}
	}

	final List<BlockElem> elems;


	public Block(IToken firstToken, List<BlockElem> elems) {
		super(firstToken);
		this.elems = elems;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitBlock(this, arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(elems);
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
		Block other = (Block) obj;
		return Objects.equals(elems, other.elems);
	}

	public List<BlockElem> getElems() {
		return elems;
	}

	@Override
	public String toString() {
		return "Block [elems=" + elems + "]";
	}

}
