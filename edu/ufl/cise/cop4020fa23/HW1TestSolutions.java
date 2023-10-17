package edu.ufl.cise.cop4020fa23;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
import edu.ufl.cise.cop4020fa23.Kind;
import edu.ufl.cise.cop4020fa23.ComponentFactory;
class HW1TestSolutions {
	static final int TIMEOUT_MILLIS = 1000;
	/**
	 * Constructs a scanner and parser for the given input string, scans and parses
	 * the input and returns and AST.
	 *
	 * @param input String representing program to be tested
	 * @return AST representing the program
	 * @throws PLCCompilerException
	 */
	AST getAST(String input) throws PLCCompilerException {
		return ComponentFactory.makeExpressionParser(input).parse();
	}
	/**
	 * Checks that the given AST e has type NumLitExpr with the indicated value.
	 * Returns the given AST cast to NumLitExpr.
	 *
	 * @param e
	 * @param value
	 * @return
	 */
	NumLitExpr checkNumLitExpr(AST e, int value) {
		assertThat("", e, instanceOf(NumLitExpr.class));
		NumLitExpr ne = (NumLitExpr) e;
		assertEquals(Integer.toString(value), ne.getText());
		return ne;
	}
	/**
	 * Checks that the given AST e has type StringLitExpr with the given String
	 * value. For convenience, we do not require the value to include the enclosing
	 * quotes. So we can write checkStringLitExpr(ast, "hello") rather than
	 * checkStringLitExpr(ast,""hello"");
	 * <p>
	 * Returns the given AST cast to StringLitExpr.
	 *
	 * @param e
	 * @param value
	 * @return
	 */
	StringLitExpr checkStringLitExpr(AST e, String value) {
		assertThat("", e, instanceOf(StringLitExpr.class));
		StringLitExpr se = (StringLitExpr) e;
		String s = se.getText();
		assertEquals('"', s.charAt(0)); // check that first char is "
		assertEquals('"', s.charAt(s.length() - 1));
		assertEquals(value, s.substring(1, s.length() - 1));
		return se;
	}
	BooleanLitExpr checkBooleanLitExpr(AST e, boolean value) {
		assertThat("", e, instanceOf(BooleanLitExpr.class));
		BooleanLitExpr be = (BooleanLitExpr) e;
		assertEquals(Boolean.toString(value), be.getText());
		return be;
	}
	/**
	 * Checks that the given AST e has type UnaryExpr with the given operator.
	 * Returns the given AST cast to UnaryExpr.
	 *
	 * @param e
	 * @param op Kind of expected operator
	 * @return
	 */
	private UnaryExpr checkUnaryExpr(AST e, Kind op) {
		assertThat("", e, instanceOf(UnaryExpr.class));
		assertEquals(op, ((UnaryExpr) e).getOp());
		return (UnaryExpr) e;
	}
	/**
	 * Checks that the given AST e has type ConditionalExpr. Returns the given AST
	 * cast to ConditionalExpr.
	 *
	 * @param e
	 * @return
	 */
	private ConditionalExpr checkConditionalExpr(AST e) {
		assertThat("", e, instanceOf(ConditionalExpr.class));
		return (ConditionalExpr) e;
	}
	/**
	 * Checks that the given AST e has type BinaryExpr with the given operator.
	 * Returns the given AST cast to BinaryExpr.
	 *
	 * @param e
	 * @param expectedOp Kind of expected operator
	 * @return
	 */
	BinaryExpr checkBinaryExpr(AST e, Kind expectedOp) {
		assertThat("", e, instanceOf(BinaryExpr.class));
		BinaryExpr be = (BinaryExpr) e;
		assertEquals(expectedOp, be.getOp().kind());
		return be;
	}
	/**
	 * Checks that the given AST e has type IdentExpr with the given name. Returns
	 * the given AST cast to IdentExpr.
	 *
	 * @param e
	 * @param name
	 * @return
	 */
	IdentExpr checkIdentExpr(AST e, String name) {
		assertThat("", e, instanceOf(IdentExpr.class));
		IdentExpr ident = (IdentExpr) e;
		assertEquals(name, ident.getName());
		return ident;
	}
	/**
	 * Checks that the given AST e has type ChannelSelector with the indicated
	 color
	 *
	 * @param e
	 * @param expectedColor
	 * @return
	 */
	ChannelSelector checkChannelSelector(AST e, Kind expectedColor) {
		assertThat("", e, instanceOf(ChannelSelector.class));
		ChannelSelector chan = (ChannelSelector) e;
		assertEquals(expectedColor, chan.color());
		return chan;
	}
	/**
	 * Checks that given AST e has type PostfixExpr, and checks whether or not
	 * ChannelSelectors and PixelSelectors exist
	 *
	 * @param e
	 * @param hasChannelSelector
	 * @param hasPixelSelector
	 * @return
	 */
	PostfixExpr checkPostfixExpr(AST e, boolean hasPixelSelector, boolean
			hasChannelSelector) {
		assertThat("", e, instanceOf(PostfixExpr.class));
		PostfixExpr pfe = (PostfixExpr) e;
		AST channel = pfe.channel();
		assertEquals(hasChannelSelector, channel != null);
		AST pixel = pfe.pixel();
		assertEquals(hasPixelSelector, pixel != null);
		return pfe;
	}
	@Test
	void test0() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
""
""";
			AST ast = getAST(input);
			checkStringLitExpr(ast, "");
		});
	}
	@Test
	void test1() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
56
""";
			AST ast = getAST(input);
			checkNumLitExpr(ast, 56);
		});
	}
	@Test
	void test2() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
abcdefg
""";
			AST ast = getAST(input);
			checkIdentExpr(ast, "abcdefg");
		});
	}
	@Test
	void test3() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
"hello there"
""";
			AST ast = getAST(input);
			checkStringLitExpr(ast, "hello there");
		});
	}
	@Test
	void test4() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
TRUE
""";
			AST ast = getAST(input);
			assertThat("", ast, instanceOf(BooleanLitExpr.class));
		});
	}
	@Test
	void test5() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
FALSE
""";
			AST ast = getAST(input);
			assertThat("", ast, instanceOf(BooleanLitExpr.class));
		});
	}
	@Test
	void test6() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
? a -> b , c
""";
			AST ast = getAST(input);
			checkConditionalExpr(ast);
			Expr v0 = ((ConditionalExpr) ast).getGuardExpr();
			checkIdentExpr(v0, "a");
			Expr v1 = ((ConditionalExpr) ast).getTrueExpr();
			checkIdentExpr(v1, "b");
			Expr v2 = ((ConditionalExpr) ast).getFalseExpr();
			checkIdentExpr(v2, "c");
		});
	}
	@Test
	void test7() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a | b
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.BITOR);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkIdentExpr(v0, "a");
			Expr v1 = ((BinaryExpr) ast).getRightExpr();
			checkIdentExpr(v1, "b");
		});
	}
	@Test
	void test8() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a & b
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.BITAND);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkIdentExpr(v0, "a");
			Expr v1 = ((BinaryExpr) ast).getRightExpr();
			checkIdentExpr(v1, "b");
		});
	}
	@Test
	void test9() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a == b
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.EQ);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkIdentExpr(v0, "a");
			Expr v1 = ((BinaryExpr) ast).getRightExpr();
			checkIdentExpr(v1, "b");
		});
	}
	@Test
	void test10() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a ** b
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.EXP);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkIdentExpr(v0, "a");
			Expr v1 = ((BinaryExpr) ast).getRightExpr();
			checkIdentExpr(v1, "b");
		});
	}
	@Test
	void test11() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a + b
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.PLUS);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkIdentExpr(v0, "a");
			Expr v1 = ((BinaryExpr) ast).getRightExpr();
			checkIdentExpr(v1, "b");
		});
	}
	@Test
	void test12() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a * b
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.TIMES);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkIdentExpr(v0, "a");
			Expr v1 = ((BinaryExpr) ast).getRightExpr();
			checkIdentExpr(v1, "b");
		});
	}
	@Test
	void test13() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(10000000), () -> {
			String input = """
[a, b, c]
""";
			AST ast = getAST(input);
			System.out.println("meow");	
			Expr v0 = ((ExpandedPixelExpr) ast).getRed();
			checkIdentExpr(v0, "a");
			Expr v1 = ((ExpandedPixelExpr) ast).getGreen();
			checkIdentExpr(v1, "b");
			Expr v2 = ((ExpandedPixelExpr) ast).getBlue();
			checkIdentExpr(v2, "c");
		});
	}
	@Test
	void test14() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
(a+b)[2+2, 3+3]:green
""";
			AST ast = getAST(input);
			checkPostfixExpr(ast, true, true);
			Expr v0 = ((PostfixExpr) ast).primary();
			checkBinaryExpr(v0, Kind.PLUS);
			Expr v1 = ((BinaryExpr) v0).getLeftExpr();
			checkIdentExpr(v1, "a");
			Expr v2 = ((BinaryExpr) v0).getRightExpr();
			checkIdentExpr(v2, "b");
			PixelSelector v3 = ((PostfixExpr) ast).pixel();
			Expr v4 = ((PixelSelector) v3).xExpr();
			checkBinaryExpr(v4, Kind.PLUS);
			Expr v5 = ((BinaryExpr) v4).getLeftExpr();
			checkNumLitExpr(v5, 2);
			Expr v6 = ((BinaryExpr) v4).getRightExpr();
			checkNumLitExpr(v6, 2);
			Expr v7 = ((PixelSelector) v3).yExpr();
			checkBinaryExpr(v7, Kind.PLUS);
			Expr v8 = ((BinaryExpr) v7).getLeftExpr();
			checkNumLitExpr(v8, 3);
			Expr v9 = ((BinaryExpr) v7).getRightExpr();
			checkNumLitExpr(v9, 3);
			ChannelSelector v10 = ((PostfixExpr) ast).channel();
			checkChannelSelector(v10, Kind.RES_green);
		});
	}
	@Test
	void test15() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a:green
""";
			AST ast = getAST(input);
			checkPostfixExpr(ast, false, true);
			Expr v0 = ((PostfixExpr) ast).primary();
			checkIdentExpr(v0, "a");
			ChannelSelector v1 = ((PostfixExpr) ast).channel();
			checkChannelSelector(v1, Kind.RES_green);
		});
	}
	@Test
	void test16() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
(? 2+2 -> TRUE , "hello")[a,b]
""";
			AST ast = getAST(input);
			checkPostfixExpr(ast, true, false);
			Expr v0 = ((PostfixExpr) ast).primary();
			checkConditionalExpr(v0);
			Expr v1 = ((ConditionalExpr) v0).getGuardExpr();
			checkBinaryExpr(v1, Kind.PLUS);
			Expr v2 = ((BinaryExpr) v1).getLeftExpr();
			checkNumLitExpr(v2, 2);
			Expr v3 = ((BinaryExpr) v1).getRightExpr();
			checkNumLitExpr(v3, 2);
			Expr v4 = ((ConditionalExpr) v0).getTrueExpr();
			assertThat("", v4, instanceOf(BooleanLitExpr.class));
			Expr v5 = ((ConditionalExpr) v0).getFalseExpr();
			checkStringLitExpr(v5, "hello");
			PixelSelector v6 = ((PostfixExpr) ast).pixel();
			Expr v7 = ((PixelSelector) v6).xExpr();
			checkIdentExpr(v7, "a");
			Expr v8 = ((PixelSelector) v6).yExpr();
			checkIdentExpr(v8, "b");
		});
	}
	@Test
	void test17() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
(? a -> b , c) + (? d -> e , f)
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.PLUS);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkConditionalExpr(v0);
			Expr v1 = ((ConditionalExpr) v0).getGuardExpr();
			checkIdentExpr(v1, "a");
			Expr v2 = ((ConditionalExpr) v0).getTrueExpr();
			checkIdentExpr(v2, "b");
			Expr v3 = ((ConditionalExpr) v0).getFalseExpr();
			checkIdentExpr(v3, "c");
			Expr v4 = ((BinaryExpr) ast).getRightExpr();
			checkConditionalExpr(v4);
			Expr v5 = ((ConditionalExpr) v4).getGuardExpr();
			checkIdentExpr(v5, "d");
			Expr v6 = ((ConditionalExpr) v4).getTrueExpr();
			checkIdentExpr(v6, "e");
			Expr v7 = ((ConditionalExpr) v4).getFalseExpr();
			checkIdentExpr(v7, "f");
		});
	}
	@Test
	void test18() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
5[2+2,"weird expression"]
""";
			AST ast = getAST(input);
			checkPostfixExpr(ast, true, false);
			Expr v0 = ((PostfixExpr) ast).primary();
			checkNumLitExpr(v0, 5);
			PixelSelector v1 = ((PostfixExpr) ast).pixel();
			Expr v2 = ((PixelSelector) v1).xExpr();
			checkBinaryExpr(v2, Kind.PLUS);
			Expr v3 = ((BinaryExpr) v2).getLeftExpr();
			checkNumLitExpr(v3, 2);
			Expr v4 = ((BinaryExpr) v2).getRightExpr();
			checkNumLitExpr(v4, 2);
			Expr v5 = ((PixelSelector) v1).yExpr();
			checkStringLitExpr(v5, "weird expression");
		});
	}
	@Test
	void test19() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(10000000), () -> {
			String input = """
[2, 3, 4]["hello" + 2,6]:green
""";
			AST ast = getAST(input);
			checkPostfixExpr(ast, true, true);
			Expr v0 = ((PostfixExpr) ast).primary();
			Expr v1 = ((ExpandedPixelExpr) v0).getRed();
			checkNumLitExpr(v1, 2);
			Expr v2 = ((ExpandedPixelExpr) v0).getGreen();
			checkNumLitExpr(v2, 3);
			Expr v3 = ((ExpandedPixelExpr) v0).getBlue();
			checkNumLitExpr(v3, 4);
			PixelSelector v4 = ((PostfixExpr) ast).pixel();
			Expr v5 = ((PixelSelector) v4).xExpr();
			checkBinaryExpr(v5, Kind.PLUS);
			Expr v6 = ((BinaryExpr) v5).getLeftExpr();
			checkStringLitExpr(v6, "hello");
			Expr v7 = ((BinaryExpr) v5).getRightExpr();
			checkNumLitExpr(v7, 2);
			Expr v8 = ((PixelSelector) v4).yExpr();
			checkNumLitExpr(v8, 6);
			ChannelSelector v9 = ((PostfixExpr) ast).channel();
			checkChannelSelector(v9, Kind.RES_green);
		});
	}
	@Test
	void test20() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
DARK_GRAY:green
""";
			AST ast = getAST(input);
			checkPostfixExpr(ast, false, true);
			Expr v0 = ((PostfixExpr) ast).primary();
			assertThat("", v0, instanceOf(ConstExpr.class));
			ChannelSelector v1 = ((PostfixExpr) ast).channel();
			checkChannelSelector(v1, Kind.RES_green);
		});
	}
	@Test
	void test21() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
-2
""";
			AST ast = getAST(input);
			checkUnaryExpr(ast, Kind.MINUS);
			Expr v0 = ((UnaryExpr) ast).getExpr();
			checkNumLitExpr(v0, 2);
		});
	}
	@Test
	void test22() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
!-2
""";
			AST ast = getAST(input);
			checkUnaryExpr(ast, Kind.BANG);
			Expr v0 = ((UnaryExpr) ast).getExpr();
			checkUnaryExpr(v0, Kind.MINUS);
			Expr v1 = ((UnaryExpr) v0).getExpr();
			checkNumLitExpr(v1, 2);
		});
	}
	@Test
	void test23() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
!(a+b)[r,2]:green
""";
			AST ast = getAST(input);
			checkUnaryExpr(ast, Kind.BANG);
			Expr v0 = ((UnaryExpr) ast).getExpr();
			checkPostfixExpr(v0, true, true);
			Expr v1 = ((PostfixExpr) v0).primary();
			checkBinaryExpr(v1, Kind.PLUS);
			Expr v2 = ((BinaryExpr) v1).getLeftExpr();
			checkIdentExpr(v2, "a");
			Expr v3 = ((BinaryExpr) v1).getRightExpr();
			checkIdentExpr(v3, "b");
			PixelSelector v4 = ((PostfixExpr) v0).pixel();
			Expr v5 = ((PixelSelector) v4).xExpr();
			checkIdentExpr(v5, "r");
			Expr v6 = ((PixelSelector) v4).yExpr();
			checkNumLitExpr(v6, 2);
			ChannelSelector v7 = ((PostfixExpr) v0).channel();
			checkChannelSelector(v7, Kind.RES_green);
		});
	}
	@Test
	void test24() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
2+2*4-5
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.MINUS);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkBinaryExpr(v0, Kind.PLUS);
			Expr v1 = ((BinaryExpr) v0).getLeftExpr();
			checkNumLitExpr(v1, 2);
			Expr v2 = ((BinaryExpr) v0).getRightExpr();
			checkBinaryExpr(v2, Kind.TIMES);
			Expr v3 = ((BinaryExpr) v2).getLeftExpr();
			checkNumLitExpr(v3, 2);
			Expr v4 = ((BinaryExpr) v2).getRightExpr();
			checkNumLitExpr(v4, 4);
			Expr v5 = ((BinaryExpr) ast).getRightExpr();
			checkNumLitExpr(v5, 5);
		});
	}
	@Test
	void test25() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
2**2**3+4
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.EXP);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkNumLitExpr(v0, 2);
			Expr v1 = ((BinaryExpr) ast).getRightExpr();
			checkBinaryExpr(v1, Kind.EXP);
			Expr v2 = ((BinaryExpr) v1).getLeftExpr();
			checkNumLitExpr(v2, 2);
			Expr v3 = ((BinaryExpr) v1).getRightExpr();
			checkBinaryExpr(v3, Kind.PLUS);
			Expr v4 = ((BinaryExpr) v3).getLeftExpr();
			checkNumLitExpr(v4, 3);
			Expr v5 = ((BinaryExpr) v3).getRightExpr();
			checkNumLitExpr(v5, 4);
		});
	}
	@Test
	void test26() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
(-"hello")
""";
			AST ast = getAST(input);
			checkUnaryExpr(ast, Kind.MINUS);
			Expr v0 = ((UnaryExpr) ast).getExpr();
			checkStringLitExpr(v0, "hello");
		});
	}
	@Test
	void test27() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
2+2>=5-2+b/"hello"&&b+(? a -> b , c) % 2 | b
""";
			AST ast = getAST(input);
			checkBinaryExpr(ast, Kind.BITOR);
			Expr v0 = ((BinaryExpr) ast).getLeftExpr();
			checkBinaryExpr(v0, Kind.AND);
			Expr v1 = ((BinaryExpr) v0).getLeftExpr();
			checkBinaryExpr(v1, Kind.GE);
			Expr v2 = ((BinaryExpr) v1).getLeftExpr();
			checkBinaryExpr(v2, Kind.PLUS);
			Expr v3 = ((BinaryExpr) v2).getLeftExpr();
			checkNumLitExpr(v3, 2);
			Expr v4 = ((BinaryExpr) v2).getRightExpr();
			checkNumLitExpr(v4, 2);
			Expr v5 = ((BinaryExpr) v1).getRightExpr();
			checkBinaryExpr(v5, Kind.PLUS);
			Expr v6 = ((BinaryExpr) v5).getLeftExpr();
			checkBinaryExpr(v6, Kind.MINUS);
			Expr v7 = ((BinaryExpr) v6).getLeftExpr();
			checkNumLitExpr(v7, 5);
			Expr v8 = ((BinaryExpr) v6).getRightExpr();
			checkNumLitExpr(v8, 2);
			Expr v9 = ((BinaryExpr) v5).getRightExpr();
			checkBinaryExpr(v9, Kind.DIV);
			Expr v10 = ((BinaryExpr) v9).getLeftExpr();
			checkIdentExpr(v10, "b");
			Expr v11 = ((BinaryExpr) v9).getRightExpr();
			checkStringLitExpr(v11, "hello");
			Expr v12 = ((BinaryExpr) v0).getRightExpr();
			checkBinaryExpr(v12, Kind.PLUS);
			Expr v13 = ((BinaryExpr) v12).getLeftExpr();
			checkIdentExpr(v13, "b");
			Expr v14 = ((BinaryExpr) v12).getRightExpr();
			checkBinaryExpr(v14, Kind.MOD);
			Expr v15 = ((BinaryExpr) v14).getLeftExpr();
			checkConditionalExpr(v15);
			Expr v16 = ((ConditionalExpr) v15).getGuardExpr();
			checkIdentExpr(v16, "a");
			Expr v17 = ((ConditionalExpr) v15).getTrueExpr();
			checkIdentExpr(v17, "b");
			Expr v18 = ((ConditionalExpr) v15).getFalseExpr();
			checkIdentExpr(v18, "c");
			Expr v19 = ((BinaryExpr) v14).getRightExpr();
			checkNumLitExpr(v19, 2);
			Expr v20 = ((BinaryExpr) ast).getRightExpr();
			checkIdentExpr(v20, "b");
		});
	}
	@Test
	void test28() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
?a -> ? b -> ? c -> 2, 3, 4, 5, 6, 7
""";
			AST ast = getAST(input);
			checkConditionalExpr(ast);
			Expr v0 = ((ConditionalExpr) ast).getGuardExpr();
			checkIdentExpr(v0, "a");
			Expr v1 = ((ConditionalExpr) ast).getTrueExpr();
			checkConditionalExpr(v1);
			Expr v2 = ((ConditionalExpr) v1).getGuardExpr();
			checkIdentExpr(v2, "b");
			Expr v3 = ((ConditionalExpr) v1).getTrueExpr();
			checkConditionalExpr(v3);
			Expr v4 = ((ConditionalExpr) v3).getGuardExpr();
			checkIdentExpr(v4, "c");
			Expr v5 = ((ConditionalExpr) v3).getTrueExpr();
			checkNumLitExpr(v5, 2);
			Expr v6 = ((ConditionalExpr) v3).getFalseExpr();
			checkNumLitExpr(v6, 3);
			Expr v7 = ((ConditionalExpr) v1).getFalseExpr();
			checkNumLitExpr(v7, 4);
			Expr v8 = ((ConditionalExpr) ast).getFalseExpr();
			checkNumLitExpr(v8, 5);
		});
	}
	@Test
	void test29() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
-----(2+2)
""";
			AST ast = getAST(input);
			checkUnaryExpr(ast, Kind.MINUS);
			Expr v0 = ((UnaryExpr) ast).getExpr();
			checkUnaryExpr(v0, Kind.MINUS);
			Expr v1 = ((UnaryExpr) v0).getExpr();
			checkUnaryExpr(v1, Kind.MINUS);
			Expr v2 = ((UnaryExpr) v1).getExpr();
			checkUnaryExpr(v2, Kind.MINUS);
			Expr v3 = ((UnaryExpr) v2).getExpr();
			checkUnaryExpr(v3, Kind.MINUS);
			Expr v4 = ((UnaryExpr) v3).getExpr();
			checkBinaryExpr(v4, Kind.PLUS);
			Expr v5 = ((BinaryExpr) v4).getLeftExpr();
			checkNumLitExpr(v5, 2);
			Expr v6 = ((BinaryExpr) v4).getRightExpr();
			checkNumLitExpr(v6, 2);
		});
	}
	@Test
	void test30() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
width a
""";
			AST ast = getAST(input);
			checkUnaryExpr(ast, Kind.RES_width);
			Expr v0 = ((UnaryExpr) ast).getExpr();
			checkIdentExpr(v0, "a");
		});
	}
	@Test
	void test31() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
""";
			assertThrows(SyntaxException.class, () -> {
				@SuppressWarnings("unused")
				AST ast = getAST(input);
			});
		});
	}
	@Test
	void test32() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a + + + 3
""";
			assertThrows(SyntaxException.class, () -> {
				@SuppressWarnings("unused")
				AST ast = getAST(input);
			});
		});
	}
	@Test
	void test33() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
? -> ,
""";
			assertThrows(SyntaxException.class, () -> {
				@SuppressWarnings("unused")
				AST ast = getAST(input);
			});
		});
	}
	@Test
	void test34() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a :
""";
			assertThrows(SyntaxException.class, () -> {
				@SuppressWarnings("unused")
				AST ast = getAST(input);
			});
		});
	}
	@Test
	void test35() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
a[2+2,
""";
			assertThrows(SyntaxException.class, () -> {
				@SuppressWarnings("unused")
				AST ast = getAST(input);
			});
		});
	}
	@Test
	void test36() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
(a,
""";
			assertThrows(SyntaxException.class, () -> {
				@SuppressWarnings("unused")
				AST ast = getAST(input);
			});
		});
	}
	@Test
	void test37() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
[a,a,]
""";
			assertThrows(SyntaxException.class, () -> {
				@SuppressWarnings("unused")
				AST ast = getAST(input);
			});
		});
	}
	@Test
	void test38() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
5 /
""";
			assertThrows(SyntaxException.class, () -> {
				@SuppressWarnings("unused")
				AST ast = getAST(input);
			});
		});
	}
	@Test
	void test39() throws PLCCompilerException {
		assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
			String input = """
!
""";
			assertThrows(SyntaxException.class, () -> {
				@SuppressWarnings("unused")
				AST ast = getAST(input);
			});
		});
	}
}