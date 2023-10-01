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

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;

/**
 * Factory class providing static methods to create and return various components of the compiler
 */
public class ComponentFactory {

		public static ILexer makeLexer(String input) {
			return new Lexer(input);
		}
		
		public static IParser makeExpressionParser(ILexer lexer) throws LexicalException {
			return new ExpressionParser(lexer);
		}
		
		public static IParser makeExpressionParser(String input) throws LexicalException {
			return new ExpressionParser(makeLexer(input));
		}
		
}
