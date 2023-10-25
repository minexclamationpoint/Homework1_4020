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

import edu.ufl.cise.cop4020fa23.Kind;

/**
 * 
 */
public enum Type {
	INT,
	BOOLEAN,
	IMAGE,
	VOID,
	PIXEL,
	STRING;
	
	public static Type kind2type(Kind kind) {
		return switch(kind) {
		case RES_int -> INT;
		case RES_boolean -> BOOLEAN;
		case RES_image -> IMAGE;
		case RES_void -> VOID;
		case RES_pixel -> PIXEL;
		case RES_string -> STRING;
		default -> throw new UnsupportedOperationException("Compiler bug:  kind2type illegal argument");
		};
	}


}
