/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */
package edu.ufl.cise.cop4020fa23;

import java.util.Objects;

/**
 * 
 */
public class Token implements IToken {

	final Kind kind;
	final int pos;
	final int length;
	final char[] source;
	final SourceLocation location;

	/**
	 * @param kind
	 * @param pos
	 * @param length
	 * @param source
	 * @param location
	 */
	public Token(Kind kind, int pos, int length, char[] source, SourceLocation location) {
		super();
		this.kind = kind;
		this.pos = pos;
		this.length = length;
		this.source = source;
		this.location = location;
	}

	@Override
	public SourceLocation sourceLocation() {
		return location;
	}

	@Override
	public Kind kind() {
		return kind;
	}

	@Override
	public String text() {
		if(length > 0) {
			return String.copyValueOf(source, pos, length);
		}
		return "";
	}


	
	@Override 
	public String toString() {
		return ("["+kind+" "+text()+"]" );
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, length, location, pos);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		return kind == other.kind && length == other.length && Objects.equals(location, other.location)
				&& pos == other.pos;
	}

}
