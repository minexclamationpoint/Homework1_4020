/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */

package edu.ufl.cise.cop4020fa23.exceptions;

import edu.ufl.cise.cop4020fa23.SourceLocation;

/** 
 * A LexicalException is thrown when an error is discovered in the input during lexing. 
 */

@SuppressWarnings("serial")
public class LexicalException extends PLCCompilerException {

	public LexicalException() {
	}

	public LexicalException(String message) {
		super(message);
	}

	public LexicalException(SourceLocation location, String message) {
		super(location, message);
	}

}
