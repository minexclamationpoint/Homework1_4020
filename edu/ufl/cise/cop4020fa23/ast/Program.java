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


/**
 * 
 */
public class Program extends AST {

	final IToken typeToken;
	final IToken nameToken;
	final List<NameDef> params;
	final Block block;
	
	Type type;
	
	/**
	 * @param firstToken
	 * @param type
	 * @param name
	 * @param params
	 * @param block
	 */
	public Program(IToken firstToken, IToken type, IToken name, List<NameDef> params, Block block) {
		super(firstToken);
		this.typeToken = type;
		this.nameToken = name;
		this.params = params;
		this.block = block;
	}


	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitProgram(this, arg);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(block, nameToken, params, typeToken);
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
		Program other = (Program) obj;
		return Objects.equals(block, other.block) && Objects.equals(nameToken, other.nameToken)
				&& Objects.equals(params, other.params) && Objects.equals(typeToken, other.typeToken);
	}


	public IToken getTypeToken() {
		return typeToken;
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public IToken getNameToken() {
		return nameToken;
	}
	
	public String getName() {
		return nameToken.text();
	}


	public List<NameDef> getParams() {
		return params;
	}


	public Block getBlock() {
		return block;
	}


	@Override
	public String toString() {
		return "Program [type=" + typeToken + ", name=" + nameToken.text() + ", params=" + params + ", block=" + block + "]";
	}

	
}
