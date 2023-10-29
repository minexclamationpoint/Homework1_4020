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

/**
 * 
 */
public class SyntheticNameDef extends NameDef {

	/** Declaration for implicitly declared variables.  This class is not used in the Parser */
	
	final String name;

	public SyntheticNameDef(String name) {
		super(null, null, null, null);
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Type getType() {
		return Type.INT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(name);
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
		SyntheticNameDef other = (SyntheticNameDef) obj;
		return Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "SyntheticNameDef [name=" + name + "]";
	}

	@Override
	public 
	IToken getIdentToken() {throw new UnsupportedOperationException();}
	
	@Override
	public 
	IToken getTypeToken() {throw new UnsupportedOperationException();}
	
	@Override
	public
	Dimension getDimension() {throw new UnsupportedOperationException();}
}
