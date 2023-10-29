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
 * Superclass of all Exceptions thrown during compilation.
 * This class includes all constructors belonging to the superclass. 
 * See documentation of java.lang.Exception for information about constructor parameters.
 */
@SuppressWarnings("serial")
public class PLCCompilerException extends Exception {


	public PLCCompilerException() {
	}

	public PLCCompilerException(String message) {
		super(message);
	}

	public PLCCompilerException(SourceLocation location, String message) {
		super(location + ": " + message);
	}

}
