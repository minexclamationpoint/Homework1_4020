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
public class GuardedBlock extends AST{
	
	final Expr guard;
	final Block block;
	/**
	 * @param firstToken
	 * @param guard
	 * @param block
	 */
	public GuardedBlock(IToken firstToken, Expr guard, Block block) {
		super(firstToken);
		this.guard = guard;
		this.block = block;
	}
	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitGuardedBlock(this,arg);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(block, guard);
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
		GuardedBlock other = (GuardedBlock) obj;
		return Objects.equals(block, other.block) && Objects.equals(guard, other.guard);
	}
	/**
	 * @return the guard
	 */
	public Expr getGuard() {
		return guard;
	}
	/**
	 * @return the block
	 */
	public Block getBlock() {
		return block;
	}
	@Override
	public String toString() {
		return "GuardedBlock [guard=" + guard + ", block=" + block + "]";
	}
	
	

}
