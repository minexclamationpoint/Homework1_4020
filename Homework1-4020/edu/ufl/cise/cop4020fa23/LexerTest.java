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

import static edu.ufl.cise.cop4020fa23.Kind.AND;
import static edu.ufl.cise.cop4020fa23.Kind.ASSIGN;
import static edu.ufl.cise.cop4020fa23.Kind.BANG;
import static edu.ufl.cise.cop4020fa23.Kind.BITAND;
import static edu.ufl.cise.cop4020fa23.Kind.BLOCK_OPEN;
import static edu.ufl.cise.cop4020fa23.Kind.BOOLEAN_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.BOX;
import static edu.ufl.cise.cop4020fa23.Kind.COMMA;
import static edu.ufl.cise.cop4020fa23.Kind.CONST;
import static edu.ufl.cise.cop4020fa23.Kind.DIV;
import static edu.ufl.cise.cop4020fa23.Kind.EOF;
import static edu.ufl.cise.cop4020fa23.Kind.EQ;
import static edu.ufl.cise.cop4020fa23.Kind.GT;
import static edu.ufl.cise.cop4020fa23.Kind.IDENT;
import static edu.ufl.cise.cop4020fa23.Kind.LE;
import static edu.ufl.cise.cop4020fa23.Kind.LSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.LT;
import static edu.ufl.cise.cop4020fa23.Kind.MINUS;
import static edu.ufl.cise.cop4020fa23.Kind.MOD;
import static edu.ufl.cise.cop4020fa23.Kind.NUM_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.PLUS;
import static edu.ufl.cise.cop4020fa23.Kind.QUESTION;
import static edu.ufl.cise.cop4020fa23.Kind.RARROW;
import static edu.ufl.cise.cop4020fa23.Kind.RES_blue;
import static edu.ufl.cise.cop4020fa23.Kind.RES_boolean;
import static edu.ufl.cise.cop4020fa23.Kind.RES_do;
import static edu.ufl.cise.cop4020fa23.Kind.RES_fi;
import static edu.ufl.cise.cop4020fa23.Kind.RES_green;
import static edu.ufl.cise.cop4020fa23.Kind.RES_height;
import static edu.ufl.cise.cop4020fa23.Kind.RES_if;
import static edu.ufl.cise.cop4020fa23.Kind.RES_image;
import static edu.ufl.cise.cop4020fa23.Kind.RES_int;
import static edu.ufl.cise.cop4020fa23.Kind.RES_nil;
import static edu.ufl.cise.cop4020fa23.Kind.RES_od;
import static edu.ufl.cise.cop4020fa23.Kind.RES_pixel;
import static edu.ufl.cise.cop4020fa23.Kind.RES_red;
import static edu.ufl.cise.cop4020fa23.Kind.RES_string;
import static edu.ufl.cise.cop4020fa23.Kind.RES_void;
import static edu.ufl.cise.cop4020fa23.Kind.RES_width;
import static edu.ufl.cise.cop4020fa23.Kind.RES_write;
import static edu.ufl.cise.cop4020fa23.Kind.RSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.SEMI;
import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;

/**
 * 
 */
class LexerTest {

	/** Switches on and off display of output via show */
	static final boolean VERBOSE = true;

	/** Output object to consol */
	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}

	/**
	 * Checks that IToken T has expected Kind
	 * 
	 * @param expectedKind
	 * @param t
	 */
	void checkToken(Kind expectedKind, IToken t) {
		assertEquals(expectedKind, t.kind());
	}

	/**
	 * Checks that IToken t has expected Kind and text
	 * 
	 * @param expectedKind
	 * @param expectedText
	 * @param t
	 */
	void checkToken(Kind expectedKind, String expectedText, IToken t) {
		assertEquals(expectedKind, t.kind());
		assertEquals(expectedText, t.text());
	}

	/**
	 * Checks that IToken t has expected Kind, text, and position
	 * 
	 * @param expectedKind
	 * @param expectedText
	 * @param expectedLine
	 * @param expectedColumn
	 * @param t
	 */
	void checkToken(Kind expectedKind, String expectedText, int expectedLine, int expectedColumn, IToken t) {
		assertEquals(expectedKind, t.kind());
		assertEquals(expectedText, t.text());
		SourceLocation loc = t.sourceLocation();
		assertEquals(expectedLine, loc.line());
		assertEquals(expectedColumn, loc.column());
		;
	}

	/*
	 * Checks that IToken t is an EOF Token
	 */
	void checkEOF(IToken t) {
		checkToken(EOF, t);
	}

	/**
	 * Checks that IToken t has kind STRING_LIT, and checks that the characters are
	 * as expected.
	 * 
	 * For convenience, stringValue is provided String without surrounding quotes
	 * (although they will have surrounding quotes in
	 * the Java source code of the test.
	 * 
	 * The text of the token should be surrounded with quotes, so we check that the
	 * first and last
	 * characters are " and then compare the token text after removing the first and
	 * last characters
	 * with the given String.
	 * 
	 * This is simply for convenience so that we can write "expected string" in
	 * tests rather than "\"expected string\"".
	 * 
	 * @param expectedStringValue
	 * @param t
	 */
	void checkString(String expectedStringValue, IToken t) {
		assertEquals(STRING_LIT, t.kind());
		String s = t.text();
		assertEquals('\"', s.charAt(0)); // check that first char is "
		assertEquals('\"', s.charAt(s.length() - 1));
		assertEquals(expectedStringValue, s.substring(1, s.length() - 1));
	}

	/**
	 * Checks that IToken t is a NUM_LIT with given
	 * 
	 * @param expectedNumlitText
	 * @param t
	 */
	void checkNumLit(String expectedNumlitText, IToken t) {
		assertEquals(NUM_LIT, t.kind());
		assertEquals(expectedNumlitText, t.text());
	}

	/**
	 * Checks that IToken t is a NUM_LIT with given int value
	 * 
	 * @param expectedNumLitValue
	 * @param t
	 */
	void checkNumLit(int expectedNumLitValue, IToken t) {
		checkNumLit(Integer.toString(expectedNumLitValue), t);
	}

	/**
	 * Checks that IToken t is a BOOLEAN_LIT with given value
	 * 
	 * @param expectedBooleanValue
	 * @param t
	 */
	void checkBooleanLit(boolean expectedBooleanValue, IToken t) {
		assertEquals(BOOLEAN_LIT, t.kind());
		String text = t.text();
		String expectedText = expectedBooleanValue ? "TRUE" : "FALSE";
		assertEquals(expectedText, text);
	}

	/**
	 * checks that IToken t is an IDENT with given name
	 * 
	 * @param expectedText
	 * @param t
	 */
	private void checkIdent(String expectedText, IToken t) {
		assertEquals(IDENT, t.kind());
		assertEquals(expectedText, t.text());
	}

	/**
	 * Displays all the tokens generated for the given input String
	 * 
	 * @param input
	 * @throws LexicalException
	 */
	void showTokens(String input) throws LexicalException {
		ILexer lexer = ComponentFactory.makeLexer(input);
		IToken token = lexer.next();
		while (token.kind() != EOF) {
			show(token);
			token = lexer.next();
		}
		show(token);
	}

	/**
	 * Empty input is OK, should add EOF token
	 * 
	 * @throws LexicalException
	 */
	@Test
	void test0() throws LexicalException {
		String input = "";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkEOF(lexer.next());
	}

	@Test
	void test1() throws LexicalException {
		String input = ",[   ]%+";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, lexer.next());
		checkToken(LSQUARE, lexer.next());
		checkToken(RSQUARE, lexer.next());
		checkToken(MOD, lexer.next());
		checkToken(PLUS, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test1a() throws LexicalException {
		String input = ",[]%+";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, lexer.next());
		checkToken(BOX, lexer.next());
		checkToken(MOD, lexer.next());
		checkToken(PLUS, lexer.next());
	}

	@Test
	void test4() throws LexicalException {
		String input = """
				    , [ ]
				##{ }.
				% + /
				? !;
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(COMMA, ",", lexer.next());
		checkToken(LSQUARE, "[", lexer.next());
		checkToken(RSQUARE, "]", lexer.next());
		checkToken(MOD, "%", lexer.next());
		checkToken(PLUS, "+", lexer.next());
		checkToken(DIV, "/", lexer.next());
		checkToken(QUESTION, "?", lexer.next());
		checkToken(BANG, "!", lexer.next());
		checkToken(SEMI, ";", lexer.next());
		checkEOF(lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test5() throws LexicalException {
		String input = """
				& &&
				&&& &&&&
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(BITAND, "&", 1, 1, lexer.next());
		checkToken(AND, "&&", 1, 3, lexer.next());
		checkToken(AND, "&&", 2, 1, lexer.next());
		checkToken(BITAND, "&", 2, 3, lexer.next());
		checkToken(AND, "&&", 2, 5, lexer.next());
		checkToken(AND, "&&", 2, 7, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test6() throws LexicalException {
		String input = """
				<< <= <: <<: <,
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(LT, lexer.next());
		checkToken(LT, lexer.next());
		checkToken(LE, lexer.next());
		checkToken(BLOCK_OPEN, lexer.next());
		checkToken(LT, lexer.next());
		checkToken(BLOCK_OPEN, lexer.next());
		checkToken(LT, lexer.next());
		checkToken(COMMA, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test7() throws LexicalException {
		String input = """
				+== = == ===
				====-> - > ->>
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(PLUS, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(ASSIGN, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(ASSIGN, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(EQ, lexer.next());
		checkToken(RARROW, lexer.next());
		checkToken(MINUS, lexer.next());
		checkToken(GT, lexer.next());
		checkToken(RARROW, lexer.next());
		checkToken(GT, lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test8() throws LexicalException {
		String input = """
				a+b
				ccc def
				BLACK
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(IDENT, "a", lexer.next());
		checkToken(PLUS, lexer.next());
		checkToken(IDENT, "b", lexer.next());
		checkToken(IDENT, "ccc", lexer.next());
		checkToken(IDENT, "def", lexer.next());
		checkToken(CONST, "BLACK", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test8a() throws LexicalException {
		String input = """
				a
				ccc
				RED
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(IDENT, "a", lexer.next());
		checkToken(IDENT, "ccc", lexer.next());
		checkToken(CONST, "RED", lexer.next());
		checkEOF(lexer.next());
	}

	// @Test
	// void test9() throws LexicalException {
	// String input = """
	// if fi
	// od do
	// red blue green
	// nil
	// image int string pixel boolean
	// void
	// width height
	// write
	// DARK_GRAY MAGENTA Z
	// """;
	// ILexer lexer = ComponentFactory.makeLexer(input);
	// checkToken(RES_if, "if", lexer.next());
	// checkToken(RES_fi, "fi", lexer.next());
	// checkToken(RES_od, "od", lexer.next());
	// checkToken(RES_do, "do", lexer.next());
	// checkToken(RES_red, "red", lexer.next());
	// checkToken(RES_blue, "blue", lexer.next());
	// checkToken(RES_green, "green", lexer.next());
	// checkToken(RES_nil, "nil", lexer.next());
	// checkToken(RES_image, "image", lexer.next());
	// checkToken(RES_int, "int", lexer.next());
	// checkToken(RES_string, "string", lexer.next());
	// checkToken(RES_pixel, "pixel", lexer.next());
	// checkToken(RES_boolean, "boolean", lexer.next());
	// checkToken(RES_void, "void", lexer.next());
	// checkToken(RES_width, "width", lexer.next());
	// checkToken(RES_height, "height", lexer.next());
	// checkToken(RES_write, "write", lexer.next());
	// checkToken(CONST, "DARK_GRAY", lexer.next());
	// checkToken(CONST, "MAGENTA", lexer.next());
	// checkToken(CONST, "Z", lexer.next());
	// }
	//
	//
	//
	@Test
	void test10() throws LexicalException {
		String input = """
				01 010
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkNumLit("0", lexer.next());
		checkNumLit("1", lexer.next());
		checkNumLit("0", lexer.next());
		checkNumLit("10", lexer.next());
	}

	/**
	 * This test shows how to write a test that will pass only if a LexicalExcption
	 * is thrown.
	 * In this case, the number 9999999999999999999999999999999999999999 is too big.
	 * 
	 * Note that correct tokens before the token with the error should be returned
	 * normally.
	 * 
	 * @throws LexicalException
	 */

	// thows exception
	@Test
	void test11() throws LexicalException {
		String input = """
				23 9999999999999999999999999999999999999999
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkNumLit("23", lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test11: " + e.getMessage());
	}

	@Test
	void test12() throws LexicalException {
		String input = """
				"hello"
				"abc"
				"abcde@#$%"
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkString("hello", lexer.next());
		checkString("abc", lexer.next());
		checkString("abcde@#$%", lexer.next());
		checkEOF(lexer.next());
	}
	/*
	 * 
	 */

	@Test
	void test13() throws LexicalException {
		String input = "\n\r\n";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkEOF(lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test14() throws LexicalException {
		String input = """
				abc ##hello there !@#$#%;
				123
				abc123
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkIdent("abc", lexer.next());
		checkNumLit("123", lexer.next());
		checkIdent("abc123", lexer.next());
		checkEOF(lexer.next());
	}

	@Test
	void test15() throws LexicalException {
		String input = """
				abc123+123abc##1233435
				"abc123+123abc##1233435"
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkIdent("abc123", lexer.next());
		checkToken(PLUS, lexer.next());
		checkNumLit("123", lexer.next());
		checkIdent("abc", lexer.next());
		checkString("abc123+123abc##1233435", lexer.next());
		checkEOF(lexer.next());
		checkEOF(lexer.next());

	}

	@Test
	void test16() throws LexicalException {
		String input = """
				a[b,c]
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkIdent("a", lexer.next());
		checkToken(LSQUARE, lexer.next());
		checkIdent("b", lexer.next());
		checkToken(COMMA, lexer.next());
		checkIdent("c", lexer.next());
		checkToken(RSQUARE, lexer.next());
	}

	// throws exception
	@Test
	void test17() throws Exception {
		String input = """
				555 #
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkNumLit("555", lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test17: " + e.getMessage());
	}

	// throws exception
	@Test
	void test18() throws Exception {
		String input = """
				555 @
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkNumLit("555", lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test18: " + e.getMessage());
	}

	// throws exception
	@Test
	void test19() throws Exception {
		String input = """
				"@"
				## @ is legal in a comment
				@
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkString("@", lexer.next());
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test19: " + e.getMessage());
	}

	@Test
	void test20() throws Exception {
		String input = """
				FALSE TRUE
				True false true
				""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkToken(BOOLEAN_LIT, "FALSE", lexer.next());
		checkToken(BOOLEAN_LIT, "TRUE", lexer.next());
		checkIdent("True", lexer.next());
		checkIdent("false", lexer.next());
		checkIdent("true", lexer.next());
	}

	@Test
	void test21() throws Exception {
	//Checks wether \r\n properly terminates a comment
		String input = """
				##\r\n hello 
				"\r\n"
				\r\n
		""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkIdent("hello", lexer.next());
		checkString("\r\n", lexer.next());
		checkEOF(lexer.next());
		checkEOF(lexer.next());
	}

	// throws exception
	@Test
	void test22() throws Exception {
	//Checks wether an error is properly called if the final token of the lexer is a comment
		String input = """
		 ##hello\n hello
		 "## woah"
		 ##hello\n hello
		 ##hello
		""";
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkIdent("hello", lexer.next());
		checkString("## woah", lexer.next());
		checkIdent("hello", lexer.next());
		lexer.next();
		LexicalException e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
		show("Error message from test22: " + e.getMessage());
	}
}
	

