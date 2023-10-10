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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.Kind;
import edu.ufl.cise.cop4020fa23.IToken;
import edu.ufl.cise.cop4020fa23.SourceLocation;
import edu.ufl.cise.cop4020fa23.ILexer;
import edu.ufl.cise.cop4020fa23.ComponentFactory;

import java.time.Duration;

import static edu.ufl.cise.cop4020fa23.Kind.*;

/**
 * 
 */
class HW0Tests {
	
	// makes it easy to turn output on and off (and less typing than
	// System.out.println)
	static final boolean VERBOSE = true;

	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}
	
	// check that this token has the expected kind
	void checkToken(Kind expectedKind, IToken t) {
		assertEquals(expectedKind, t.kind());
	}
	
	// check that this token has the expected kind and characters
	void checkToken(Kind expectedKind, String expectedChars, IToken t) {
		assertEquals(expectedKind, t.kind());
		assertEquals(expectedChars, t.text());
	}
	
	//check that token has expected Kind, characters, and position
	void checkToken(Kind expectedKind, String expectedChars, int expectedLine, int expectedColumn, IToken t) {
		assertEquals(expectedKind, t.kind());
		assertEquals(expectedChars, t.text());
		SourceLocation loc = t.sourceLocation();
		assertEquals(expectedLine, loc.line());
		assertEquals(expectedColumn, loc.column());
		;
	}
	
	void checkEOF(IToken t) {
		checkToken(EOF,t);
	}
	
	/**
	 * For convenience, in this method we give the value of the String without surrounding quotes.
	 * The text of the token should be surrounded with quotes, so we check that the first and last
	 * characters are " and then compare the token text after removing the first and last characters
	 * with the given String.    This is simply for convenience so that we can write "expected string" in
	 * tests rather than "\"expected string\"".
	 * 
	 * @param stringValue
	 * @param t
	 */
	void checkString(String stringValue, IToken t) {
		assertEquals(STRING_LIT, t.kind());
		String s = t.text();  
		assertEquals('\"', s.charAt(0));  //check that first char is "
		assertEquals('\"', s.charAt(s.length()-1));
		assertEquals(stringValue, s.substring(1, s.length() - 1));
	}
	
	void checkNumLit(String numText, IToken t) {
		assertEquals(NUM_LIT, t.kind());
		assertEquals(numText, t.text());
	}
	
	void checkNumLit(int value, IToken t) {
		checkNumLit(Integer.toString(value),t);
	}
	
	void checkBooleanLit(boolean value, IToken t) {
		assertEquals(BOOLEAN_LIT, t.kind());
		String text = t.text();
		String expectedText = value ? "T" : "F";
		assertEquals(expectedText, text);
	}
	
	private void checkIdent(String text, IToken t) {
		assertEquals(IDENT, t.kind());
		assertEquals(text, t.text());
	}
	
	void showTokens(String input) throws LexicalException {
		ILexer lexer = ComponentFactory.makeLexer(input);
		IToken token = lexer.next();
		while (token.kind() != EOF) {
			show(token);
			token = lexer.next();
		}
		show(token);
	}

	@Test
	void test0() throws LexicalException {
	String input = "";
	assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
		showTokens(input);
		ILexer lexer = ComponentFactory.makeLexer(input);
		checkEOF(lexer.next());
	});
	}
	
	@Test
	void test1() throws LexicalException {
	String input = ",[   ]%+";
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			showTokens(input);
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(COMMA, lexer.next());
			checkToken(LSQUARE, lexer.next());
			checkToken(RSQUARE, lexer.next());
			checkToken(MOD, lexer.next());
			checkToken(PLUS, lexer.next());
			checkEOF(lexer.next());
		});
	}
		
	@Test
	void test1a() throws LexicalException {
	String input = ",[]%+";
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			showTokens(input);
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(COMMA, lexer.next());
			checkToken(BOX, lexer.next());
			checkToken(MOD, lexer.next());
			checkToken(PLUS, lexer.next());
		});
	}	
	
	@Test
	void test2() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					,[]
					##{}.
					%+
					""";
			showTokens(input);
		});
	}
	
	@Test
	void test3() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					    , [ ]
					##{ }.
					% +
					""";
			showTokens(input);
		});
	}

	@Test
	void test4() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
	}	
	
	@Test
	void test5() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					& && &&& &&&&
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(BITAND, "&", 1, 1, lexer.next());
			checkToken(AND, "&&", 1, 3, lexer.next());
			checkToken(AND, "&&", 1, 6, lexer.next());
			checkToken(BITAND, "&", 1, 8, lexer.next());
			checkToken(AND, "&&", 1, 10, lexer.next());
			checkToken(AND, "&&", 1, 12, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void testString() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					"hello"
					"there"
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(STRING_LIT, "\"hello\"", 1, 1, lexer.next());
			checkToken(STRING_LIT, "\"there\"", 2, 1, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void testSourceLocation() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					if a
					   b + 2
					fi
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_if, "if", 1, 1, lexer.next());
			checkToken(IDENT, "a", 1, 4, lexer.next());
			checkToken(IDENT, "b", 2, 4, lexer.next());
			checkToken(PLUS, "+", 2, 6, lexer.next());
			checkToken(NUM_LIT, "2", 2, 8, lexer.next());
			checkToken(RES_fi, "fi", 3, 1, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void checkNumLit() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					0100
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(NUM_LIT, "0", 1, 1, lexer.next());
			checkToken(NUM_LIT, "100", 1, 2, lexer.next());
			checkEOF(lexer.next());
		});
	}
	
	@Test
	void test6() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
	}
	
	@Test
	void test7() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
	}
	
	@Test
	void test8() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
	}
	
	@Test
	void test8a() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
	}
	
	@Test
	void test9() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					if fi
					od do
					red blue green
					image int string pixel boolean
					void 
					width height
					write
					DARK_GRAY MAGENTA Z
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_if, "if", lexer.next());
			checkToken(RES_fi, "fi", lexer.next());
			checkToken(RES_od, "od", lexer.next());
			checkToken(RES_do, "do", lexer.next());
			checkToken(RES_red, "red", lexer.next());
			checkToken(RES_blue, "blue", lexer.next());
			checkToken(RES_green, "green", lexer.next());
			checkToken(RES_image, "image", lexer.next());
			checkToken(RES_int, "int", lexer.next());
			checkToken(RES_string, "string", lexer.next());
			checkToken(RES_pixel, "pixel", lexer.next());
			checkToken(RES_boolean, "boolean", lexer.next());
			checkToken(RES_void, "void", lexer.next());
			checkToken(RES_width, "width", lexer.next());
			checkToken(RES_height, "height", lexer.next());
			checkToken(RES_write, "write", lexer.next());
			checkToken(CONST, "DARK_GRAY", lexer.next());
			checkToken(CONST, "MAGENTA", lexer.next());
			checkToken(CONST, "Z", lexer.next());
		});
	}

	
    @Test
    void test10() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					23 59 
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkNumLit("23", lexer.next());
			checkNumLit("59", lexer.next());
		});
    }
    
    //throws exception
    @Test
    void test11() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					23 9999999999999999999999999999999999999999
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkNumLit("23", lexer.next());
			assertThrows(LexicalException.class, () -> {
				lexer.next();
			});
		});
    }
    
    @Test
    void test12() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
    }
    
    @Test
    void test13() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = "\n\r\n";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkEOF(lexer.next());
			checkEOF(lexer.next());
		});
    }
    
    @Test
    void test14() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
    }
    
    @Test
    void test15() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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

		});
    }

    @Test
    void test16() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
    }
    //throws exception
    @Test
    void test17() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					555 #
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkNumLit("555", lexer.next());
			LexicalException e = assertThrows(LexicalException.class, () -> {
				lexer.next();
			});
			show(e.getMessage());
		});
    }
    
    //throws exception
    @Test
    void test18() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					555 @
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkNumLit("555", lexer.next());
			LexicalException e = assertThrows(LexicalException.class, () -> {
				lexer.next();
			});
			show(e.getMessage());
		});
    }
    
    //throws exception
    @Test
    void test19() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
    }

	@Test
	void testNotClosedString() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = "\"";
			ILexer lexer = ComponentFactory.makeLexer(input);
			LexicalException e = assertThrows(LexicalException.class, () -> {
				lexer.next();
			});
			show("Error message from testNotClosedString: " + e.getMessage());
		});
	}

	@Test
	void testCommentAtEOF() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = "##";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkEOF(lexer.next());
		});
	}
    
    @Test
    void test20() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
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
		});
    }

	@Test
	void testUnderscoreInIdent() throws LexicalException {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = "_";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkIdent("_", lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator1() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					,
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(COMMA, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator2() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					;
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(SEMI, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator3() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					?
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(QUESTION, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator4() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					:
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(COLON, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator5() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					(
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(LPAREN, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator6() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					)
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RPAREN, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator7() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					<
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(LT, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator8() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					>
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(GT, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator9() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					[
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(LSQUARE, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator10() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					]
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RSQUARE, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator11() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					=
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(ASSIGN, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator12() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					==
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(EQ, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator13() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					<=
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(LE, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator14() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					>=
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(GE, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator15() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					!
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(BANG, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator16() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					&
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(BITAND, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator17() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					&&
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(AND, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator18() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					|
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(BITOR, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator19() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					||
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(OR, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator20() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					+
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(PLUS, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator21() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					-
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(MINUS, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator22() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					*
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(TIMES, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator23() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					**
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(EXP, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator24() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					/
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(DIV, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator25() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					%
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(MOD, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator26() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					<:
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(BLOCK_OPEN, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator27() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					:>
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(BLOCK_CLOSE, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator28() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					^
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RETURN, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator29() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					->
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RARROW, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void operator30() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					[]
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(BOX, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant1() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					Z
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant2() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					BLACK
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant3() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					BLUE
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant4() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					CYAN
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant5() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					DARK_GRAY
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant6() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					GRAY
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant7() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					GREEN
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant8() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					LIGHT_GRAY
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant9() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					MAGENTA
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant10() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					ORANGE
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant11() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					PINK
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant12() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					RED
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant13() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					WHITE
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void constant14() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					YELLOW
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(CONST, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved1() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					image
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_image, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved2() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					pixel
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_pixel, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved3() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					int
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_int, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved4() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					string
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_string, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved5() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					void
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_void, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved6() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					boolean
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_boolean, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved7() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					write
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_write, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved8() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					height
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_height, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved9() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					width
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_width, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved10() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					if
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_if, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved11() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					fi
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_fi, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved12() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					do
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_do, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved13() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					od
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_od, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved14() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					red
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_red, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved15() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					green
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_green, lexer.next());
			checkEOF(lexer.next());
		});
	}

	@Test
	void reserved16() throws Exception {
		assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
			String input = """
					blue
					""";
			ILexer lexer = ComponentFactory.makeLexer(input);
			checkToken(RES_blue, lexer.next());
			checkEOF(lexer.next());
		});
	}
}
