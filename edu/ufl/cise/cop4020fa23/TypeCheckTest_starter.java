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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.ASTVisitor;
import edu.ufl.cise.cop4020fa23.ast.AssignmentStatement;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.Block;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.Declaration;
import edu.ufl.cise.cop4020fa23.ast.Dimension;
import edu.ufl.cise.cop4020fa23.ast.DoStatement;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.GuardedBlock;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.LValue;
import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.Program;
import edu.ufl.cise.cop4020fa23.ast.ReturnStatement;
import edu.ufl.cise.cop4020fa23.ast.StatementBlock;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.Type;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.ast.WriteStatement;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

class TypeCheckTest_starter {
	static final int TIMEOUT_MILLIS = 1000;

	AST getDecoratedAST(String input) throws PLCCompilerException {
		AST ast = ComponentFactory.makeParser(input).parse();
		ASTVisitor typeChecker = ComponentFactory.makeTypeChecker();
		ast.visit(typeChecker, null);
		return ast;
	}

	NumLitExpr checkNumLitExpr(AST e, String value) {
		assertThat("", e, instanceOf(NumLitExpr.class));
		NumLitExpr ne = (NumLitExpr) e;
		assertEquals(value, ne.getText());
		assertEquals(Type.INT, ne.getType());
		return ne;
	}

	NumLitExpr checkNumLitExpr(AST e, int value) {
		assertThat("", e, instanceOf(NumLitExpr.class));
		NumLitExpr ne = (NumLitExpr) e;
		assertEquals(Integer.toString(value), ne.getText());
		assertEquals(Type.INT, ne.getType());
		return ne;
	}

	StringLitExpr checkStringLitExpr(AST e, String value) {
		assertThat("", e, instanceOf(StringLitExpr.class));
		StringLitExpr se = (StringLitExpr) e;
		String s = se.getText();
		assertEquals('"', s.charAt(0)); // check that first char is "
		assertEquals('"', s.charAt(s.length() - 1));
		assertEquals(value, s.substring(1, s.length() - 1));
		assertEquals(Type.STRING, se.getType());
		return se;
	}

	BooleanLitExpr checkBooleanLitExpr(AST e, String value) {
		assertThat("", e, instanceOf(BooleanLitExpr.class));
		BooleanLitExpr be = (BooleanLitExpr) e;
		assertEquals(value, be.getText());
		assertEquals(Type.BOOLEAN, be.getType());
		return be;
	}

	private UnaryExpr checkUnaryExpr(AST e, Kind op, Type type) {
		assertThat("", e, instanceOf(UnaryExpr.class));
		UnaryExpr ue = (UnaryExpr) e;
		assertEquals(op, ((UnaryExpr) e).getOp());
		assertEquals(type, ue.getType());
		return (UnaryExpr) e;
	}

	private ConditionalExpr checkConditionalExpr(AST e, Type type) {
		assertThat("", e, instanceOf(ConditionalExpr.class));
		ConditionalExpr ce = (ConditionalExpr) e;
		assertEquals(type, ce.getType());
		return (ConditionalExpr) e;
	}

	BinaryExpr checkBinaryExpr(AST e, Kind expectedOp, Type type) {
		assertThat("", e, instanceOf(BinaryExpr.class));
		BinaryExpr be = (BinaryExpr) e;
		assertEquals(expectedOp, be.getOp().kind());
		assertEquals(type, be.getType());
		return be;
	}

	IdentExpr checkIdentExpr(AST e, String name, Type type) {
		assertThat("", e, instanceOf(IdentExpr.class));
		IdentExpr ident = (IdentExpr) e;
		assertEquals(name, ident.getName());
		assertEquals(type, ident.getType());
		return ident;
	}

	ConstExpr checkConstExpr(AST e, String name, Type type) {
		assertThat("", e, instanceOf(ConstExpr.class));
		ConstExpr ce = (ConstExpr) e;
		assertEquals(name, ce.getName());
		assertEquals(type, ce.getType());
		return ce;
	}

	PostfixExpr checkPostfixExpr(AST e, boolean hasPixelSelector, boolean hasChannelSelector, Type type) {
		assertThat("", e, instanceOf(PostfixExpr.class));
		PostfixExpr pfe = (PostfixExpr) e;
		AST channel = pfe.channel();
		assertEquals(hasChannelSelector, channel != null);
		AST pixel = pfe.pixel();
		assertEquals(hasPixelSelector, pixel != null);
		assertEquals(type, pfe.getType());
		return pfe;
	}

	ChannelSelector checkChannelSelector(AST e, String expectedColor) {
		assertThat("", e, instanceOf(ChannelSelector.class));
		ChannelSelector chan = (ChannelSelector) e;
		assertEquals(expectedColor, getColorString(chan.color()));
		return chan;
	}

	ChannelSelector checkChannelSelector(AST e, Kind expectedColor) {
		assertThat("", e, instanceOf(ChannelSelector.class));
		ChannelSelector chan = (ChannelSelector) e;
		assertEquals(expectedColor, chan.color());
		return chan;
	}

	String getColorString(Kind kind) {
		return switch (kind) {
		case RES_red -> "red";
		case RES_blue -> "blue";
		case RES_green -> "green";
		default -> throw new IllegalArgumentException();
		};
	}

	LValue checkLValueName(AST lValue, String name, Type type) {
		assertThat("", lValue, instanceOf(LValue.class));
		LValue ident = (LValue) lValue;
		assertEquals(name, ident.getName());
		assertEquals(type, ident.getType());
		return ident;
	}

	NameDef checkNameDef(AST ast, Type type, String name) {
		assertThat("", ast, instanceOf(NameDef.class));
		NameDef nameDef = (NameDef) ast;
		assertEquals(type, nameDef.getType());
		assertEquals(name, nameDef.getName());
		assertNull(nameDef.getDimension());
		return nameDef;
	}

	NameDef checkNameDefDim(AST ast, Type type, String name) {
		assertThat("", ast, instanceOf(NameDef.class));
		NameDef nameDef = (NameDef) ast;
		assertEquals(type, nameDef.getType());
		assertEquals(name, nameDef.getName());
		assertNotNull(nameDef.getDimension());
		return nameDef;
	}

	Program checkProgram(AST ast, Type type, String name) {
		assertThat("", ast, instanceOf(Program.class));
		Program program = (Program) ast;
		assertEquals(type, program.getType());
		assertEquals(name, program.getName());
		return program;
	}

	Declaration checkDec(AST ast) {
		assertThat("", ast, instanceOf(Declaration.class));
		Declaration dec0 = (Declaration) ast;
		return dec0;
	}

	@Test
	void test0() throws PLCCompilerException {
		String input = """
				void prog0() <::>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.VOID, "prog0");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(0, blockElemList3.size());
	}

	@Test
	void test1() throws PLCCompilerException {
		String input = """
				int f(int xx, string ss, image ii, pixel p)<::>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.INT, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(4, params1.size());
		NameDef paramNameDef2 = params1.get(0);
		checkNameDef(paramNameDef2, Type.INT, "xx");
		NameDef paramNameDef3 = params1.get(1);
		checkNameDef(paramNameDef3, Type.STRING, "ss");
		NameDef paramNameDef4 = params1.get(2);
		checkNameDef(paramNameDef4, Type.IMAGE, "ii");
		NameDef paramNameDef5 = params1.get(3);
		checkNameDef(paramNameDef5, Type.PIXEL, "p");
		Block programBlock6 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList7 = programBlock6.getElems();
		assertEquals(0, blockElemList7.size());
	}

	@Test
	void test2() throws PLCCompilerException {
		String input = """
				void p0(int a, string s, boolean b, image i, pixel p)<::>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.VOID, "p0");
		List<NameDef> params1 = program0.getParams();
		assertEquals(5, params1.size());
		NameDef paramNameDef2 = params1.get(0);
		checkNameDef(paramNameDef2, Type.INT, "a");
		NameDef paramNameDef3 = params1.get(1);
		checkNameDef(paramNameDef3, Type.STRING, "s");
		NameDef paramNameDef4 = params1.get(2);
		checkNameDef(paramNameDef4, Type.BOOLEAN, "b");
		NameDef paramNameDef5 = params1.get(3);
		checkNameDef(paramNameDef5, Type.IMAGE, "i");
		NameDef paramNameDef6 = params1.get(4);
		checkNameDef(paramNameDef6, Type.PIXEL, "p");
		Block programBlock7 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList8 = programBlock7.getElems();
		assertEquals(0, blockElemList8.size());
	}

	@Test
	void test3() throws PLCCompilerException {
		String input = """
				boolean p0() <:
				int a;
				string s;
				boolean b;
				image i;
				pixel p;
				image[1028,256] d;
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.BOOLEAN, "p0");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(6, blockElemList3.size());
		BlockElem blockElem4 = blockElemList3.get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, Type.INT, "a");
		BlockElem blockElem6 = blockElemList3.get(1);
		checkDec(blockElem6);
		NameDef nameDef7 = ((Declaration) blockElem6).getNameDef();
		checkNameDef(nameDef7, Type.STRING, "s");
		BlockElem blockElem8 = blockElemList3.get(2);
		checkDec(blockElem8);
		NameDef nameDef9 = ((Declaration) blockElem8).getNameDef();
		checkNameDef(nameDef9, Type.BOOLEAN, "b");
		BlockElem blockElem10 = blockElemList3.get(3);
		checkDec(blockElem10);
		NameDef nameDef11 = ((Declaration) blockElem10).getNameDef();
		checkNameDef(nameDef11, Type.IMAGE, "i");
		BlockElem blockElem12 = blockElemList3.get(4);
		checkDec(blockElem12);
		NameDef nameDef13 = ((Declaration) blockElem12).getNameDef();
		checkNameDef(nameDef13, Type.PIXEL, "p");
		BlockElem blockElem14 = blockElemList3.get(5);
		checkDec(blockElem14);
		NameDef nameDef15 = ((Declaration) blockElem14).getNameDef();
		checkNameDefDim(nameDef15, Type.IMAGE, "d");
		Dimension dimension16 = nameDef15.getDimension();
		assertThat("", dimension16, instanceOf(Dimension.class));
		Expr width17 = dimension16.getWidth();
		checkNumLitExpr(width17, 1028);
		Expr height18 = dimension16.getHeight();
		checkNumLitExpr(height18, 256);
	}

	@Test
	void test4() throws PLCCompilerException {
		String input = """
				int f()<:
				^3;
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.INT, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(1, blockElemList3.size());
		BlockElem blockElem4 = blockElemList3.get(0);
		assertThat("", blockElem4, instanceOf(ReturnStatement.class));
		Expr returnValueExpr5 = ((ReturnStatement) blockElem4).getE();
		checkNumLitExpr(returnValueExpr5, 3);
	}

	@Test
	void test5() throws PLCCompilerException {
		String input = """
				image f()<:
				image i = "url";
				image j = i;
				int rr = j[3,4]:red;
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.IMAGE, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(3, blockElemList3.size());
		BlockElem blockElem4 = blockElemList3.get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, Type.IMAGE, "i");
		Expr expr6 = ((Declaration) blockElem4).getInitializer();
		checkStringLitExpr(expr6, "url");
		BlockElem blockElem7 = blockElemList3.get(1);
		checkDec(blockElem7);
		NameDef nameDef8 = ((Declaration) blockElem7).getNameDef();
		checkNameDef(nameDef8, Type.IMAGE, "j");
		Expr expr9 = ((Declaration) blockElem7).getInitializer();
		checkIdentExpr(expr9, "i", Type.IMAGE);
		BlockElem blockElem10 = blockElemList3.get(2);
		checkDec(blockElem10);
		NameDef nameDef11 = ((Declaration) blockElem10).getNameDef();
		checkNameDef(nameDef11, Type.INT, "rr");
		Expr expr12 = ((Declaration) blockElem10).getInitializer();
		checkPostfixExpr(expr12, true, true, Type.INT);
		Expr expr13 = ((PostfixExpr) expr12).primary();
		checkIdentExpr(expr13, "j", Type.IMAGE);
		PixelSelector pixel14 = ((PostfixExpr) expr12).pixel();
		Expr x15 = pixel14.xExpr();
		checkNumLitExpr(x15, 3);
		Expr y16 = pixel14.yExpr();
		checkNumLitExpr(y16, 4);
		ChannelSelector channel17 = ((PostfixExpr) expr12).channel();
		checkChannelSelector(channel17, Kind.RES_red);
	}

	@Test
	void test6() throws PLCCompilerException {
		String input = """
				image f()<:
				image i = "url";
				int x;
				int y;
				i[x,y] = [x,y,0];
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.IMAGE, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(4, blockElemList3.size());
		BlockElem blockElem4 = blockElemList3.get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, Type.IMAGE, "i");
		Expr expr6 = ((Declaration) blockElem4).getInitializer();
		checkStringLitExpr(expr6, "url");
		BlockElem blockElem7 = blockElemList3.get(1);
		checkDec(blockElem7);
		NameDef nameDef8 = ((Declaration) blockElem7).getNameDef();
		checkNameDef(nameDef8, Type.INT, "x");
		BlockElem blockElem9 = blockElemList3.get(2);
		checkDec(blockElem9);
		NameDef nameDef10 = ((Declaration) blockElem9).getNameDef();
		checkNameDef(nameDef10, Type.INT, "y");
		BlockElem blockElem11 = blockElemList3.get(3);
		assertThat("", blockElem11, instanceOf(AssignmentStatement.class));
		LValue LValue12 = ((AssignmentStatement) blockElem11).getlValue();
		assertThat("", LValue12, instanceOf(LValue.class));
		String name13 = LValue12.getName();
		assertEquals("i", name13);
		PixelSelector pixel14 = LValue12.getPixelSelector();
		Expr x15 = pixel14.xExpr();
		checkIdentExpr(x15, "x", Type.INT);
		Expr y16 = pixel14.yExpr();
		checkIdentExpr(y16, "y", Type.INT);
		assertNull(LValue12.getChannelSelector());
		Expr expr17 = ((AssignmentStatement) blockElem11).getE();
		Expr red18 = ((ExpandedPixelExpr) expr17).getRed();
		checkIdentExpr(red18, "x", Type.INT);
		Expr green19 = ((ExpandedPixelExpr) expr17).getGreen();
		checkIdentExpr(green19, "y", Type.INT);
		Expr blue20 = ((ExpandedPixelExpr) expr17).getBlue();
		checkNumLitExpr(blue20, 0);
	}

	@Test
	void test7() throws PLCCompilerException {
		String input = """
				boolean f()<:
				boolean b = TRUE;
				boolean c = FALSE;
				^ TRUE;
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.BOOLEAN, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(3, blockElemList3.size());
		BlockElem blockElem4 = blockElemList3.get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, Type.BOOLEAN, "b");
		Expr expr6 = ((Declaration) blockElem4).getInitializer();
		checkBooleanLitExpr(expr6, "TRUE");
		BlockElem blockElem7 = blockElemList3.get(1);
		checkDec(blockElem7);
		NameDef nameDef8 = ((Declaration) blockElem7).getNameDef();
		checkNameDef(nameDef8, Type.BOOLEAN, "c");
		Expr expr9 = ((Declaration) blockElem7).getInitializer();
		checkBooleanLitExpr(expr9, "FALSE");
		BlockElem blockElem10 = blockElemList3.get(2);
		assertThat("", blockElem10, instanceOf(ReturnStatement.class));
		Expr returnValueExpr11 = ((ReturnStatement) blockElem10).getE();
		checkBooleanLitExpr(returnValueExpr11, "TRUE");
	}

	@Test
	void test8() throws PLCCompilerException {
		String input = """
				string f() <:
				   int x = 1;
				   int y = 2;
				   boolean b = TRUE;
				   do b -> <: int x = 3; int y = 4; write x+y; b = FALSE; :> od;
				   :>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.STRING, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(4, blockElemList3.size());
		BlockElem blockElem4 = blockElemList3.get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, Type.INT, "x");
		Expr expr6 = ((Declaration) blockElem4).getInitializer();
		checkNumLitExpr(expr6, 1);
		BlockElem blockElem7 = blockElemList3.get(1);
		checkDec(blockElem7);
		NameDef nameDef8 = ((Declaration) blockElem7).getNameDef();
		checkNameDef(nameDef8, Type.INT, "y");
		Expr expr9 = ((Declaration) blockElem7).getInitializer();
		checkNumLitExpr(expr9, 2);
		BlockElem blockElem10 = blockElemList3.get(2);
		checkDec(blockElem10);
		NameDef nameDef11 = ((Declaration) blockElem10).getNameDef();
		checkNameDef(nameDef11, Type.BOOLEAN, "b");
		Expr expr12 = ((Declaration) blockElem10).getInitializer();
		checkBooleanLitExpr(expr12, "TRUE");
		BlockElem blockElem13 = blockElemList3.get(3);
		assertThat("", blockElem13, instanceOf(DoStatement.class));
		List<GuardedBlock> guardedBlocks14 = ((DoStatement) blockElem13).getGuardedBlocks();
		assertEquals(1, guardedBlocks14.size());
		GuardedBlock guardedBlock15 = guardedBlocks14.get(0);
		assertThat("", guardedBlock15, instanceOf(GuardedBlock.class));
		Expr guard16 = guardedBlock15.getGuard();
		checkIdentExpr(guard16, "b", Type.BOOLEAN);
		Block block17 = guardedBlock15.getBlock();
		List<BlockElem> blockElemList18 = block17.getElems();
		assertEquals(4, blockElemList18.size());
		BlockElem blockElem19 = blockElemList18.get(0);
		checkDec(blockElem19);
		NameDef nameDef20 = ((Declaration) blockElem19).getNameDef();
		checkNameDef(nameDef20, Type.INT, "x");
		Expr expr21 = ((Declaration) blockElem19).getInitializer();
		checkNumLitExpr(expr21, 3);
		BlockElem blockElem22 = blockElemList18.get(1);
		checkDec(blockElem22);
		NameDef nameDef23 = ((Declaration) blockElem22).getNameDef();
		checkNameDef(nameDef23, Type.INT, "y");
		Expr expr24 = ((Declaration) blockElem22).getInitializer();
		checkNumLitExpr(expr24, 4);
		BlockElem blockElem25 = blockElemList18.get(2);
		assertThat("", blockElem25, instanceOf(WriteStatement.class));
		Expr writeStatementExpr26 = ((WriteStatement) blockElem25).getExpr();
		checkBinaryExpr(writeStatementExpr26, Kind.PLUS, Type.INT);
		Expr leftExpr27 = ((BinaryExpr) writeStatementExpr26).getLeftExpr();
		checkIdentExpr(leftExpr27, "x", Type.INT);
		Expr rightExpr28 = ((BinaryExpr) writeStatementExpr26).getRightExpr();
		checkIdentExpr(rightExpr28, "y", Type.INT);
		BlockElem blockElem29 = blockElemList18.get(3);
		assertThat("", blockElem29, instanceOf(AssignmentStatement.class));
		LValue LValue30 = ((AssignmentStatement) blockElem29).getlValue();
		assertThat("", LValue30, instanceOf(LValue.class));
		String name31 = LValue30.getName();
		assertEquals("b", name31);
		assertNull(LValue30.getPixelSelector());
		assertNull(LValue30.getChannelSelector());
		Expr expr32 = ((AssignmentStatement) blockElem29).getE();
		checkBooleanLitExpr(expr32, "FALSE");
	}

	@Test
	void test9() throws PLCCompilerException {
		String input = """
				image f() <:
				   image x;
				   x[z,y] = [y,z,z];
				   ^x;
				   :>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.IMAGE, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(3, blockElemList3.size());
		BlockElem blockElem4 = blockElemList3.get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, Type.IMAGE, "x");
		BlockElem blockElem6 = blockElemList3.get(1);
		assertThat("", blockElem6, instanceOf(AssignmentStatement.class));
		LValue LValue7 = ((AssignmentStatement) blockElem6).getlValue();
		assertThat("", LValue7, instanceOf(LValue.class));
		String name8 = LValue7.getName();
		assertEquals("x", name8);
		PixelSelector pixel9 = LValue7.getPixelSelector();
		Expr x10 = pixel9.xExpr();
		checkIdentExpr(x10, "z", Type.INT);
		Expr y11 = pixel9.yExpr();
		checkIdentExpr(y11, "y", Type.INT);
		assertNull(LValue7.getChannelSelector());
		Expr expr12 = ((AssignmentStatement) blockElem6).getE();
		Expr red13 = ((ExpandedPixelExpr) expr12).getRed();
		checkIdentExpr(red13, "y", Type.INT);
		Expr green14 = ((ExpandedPixelExpr) expr12).getGreen();
		checkIdentExpr(green14, "z", Type.INT);
		Expr blue15 = ((ExpandedPixelExpr) expr12).getBlue();
		checkIdentExpr(blue15, "z", Type.INT);
		BlockElem blockElem16 = blockElemList3.get(2);
		assertThat("", blockElem16, instanceOf(ReturnStatement.class));
		Expr returnValueExpr17 = ((ReturnStatement) blockElem16).getE();
		checkIdentExpr(returnValueExpr17, "x", Type.IMAGE);
	}

	@Test
	void test10() throws PLCCompilerException {
		String input = """
				image f() <:
				   image x;
				   <:
				   boolean b;
				   :>;
				   x[z,b] = [z,b,z];
				   ^x;
				   :>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.IMAGE, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(4, blockElemList3.size());
		BlockElem blockElem4 = blockElemList3.get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, Type.IMAGE, "x");
		BlockElem blockElem6 = blockElemList3.get(1);
		assertThat("", blockElem6, instanceOf(StatementBlock.class));
		Block block7 = ((StatementBlock) blockElem6).getBlock();
		List<BlockElem> blockElemList8 = block7.getElems();
		assertEquals(1, blockElemList8.size());
		BlockElem blockElem9 = blockElemList8.get(0);
		checkDec(blockElem9);
		NameDef nameDef10 = ((Declaration) blockElem9).getNameDef();
		checkNameDef(nameDef10, Type.BOOLEAN, "b");
		BlockElem blockElem11 = blockElemList3.get(2);
		assertThat("", blockElem11, instanceOf(AssignmentStatement.class));
		LValue LValue12 = ((AssignmentStatement) blockElem11).getlValue();
		assertThat("", LValue12, instanceOf(LValue.class));
		String name13 = LValue12.getName();
		assertEquals("x", name13);
		PixelSelector pixel14 = LValue12.getPixelSelector();
		Expr x15 = pixel14.xExpr();
		checkIdentExpr(x15, "z", Type.INT);
		Expr y16 = pixel14.yExpr();
		checkIdentExpr(y16, "b", Type.INT);
		assertNull(LValue12.getChannelSelector());
		Expr expr17 = ((AssignmentStatement) blockElem11).getE();
		Expr red18 = ((ExpandedPixelExpr) expr17).getRed();
		checkIdentExpr(red18, "z", Type.INT);
		Expr green19 = ((ExpandedPixelExpr) expr17).getGreen();
		checkIdentExpr(green19, "b", Type.INT);
		Expr blue20 = ((ExpandedPixelExpr) expr17).getBlue();
		checkIdentExpr(blue20, "z", Type.INT);
		BlockElem blockElem21 = blockElemList3.get(3);
		assertThat("", blockElem21, instanceOf(ReturnStatement.class));
		Expr returnValueExpr22 = ((ReturnStatement) blockElem21).getE();
		checkIdentExpr(returnValueExpr22, "x", Type.IMAGE);
	}

	@Test
	void test11() throws PLCCompilerException {
		String input = """
				string sss()<:
				write 3+5;
				write Z;
				write [1,2,3];
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.STRING, "sss");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(3, blockElemList3.size());
		BlockElem blockElem4 = blockElemList3.get(0);
		assertThat("", blockElem4, instanceOf(WriteStatement.class));
		Expr writeStatementExpr5 = ((WriteStatement) blockElem4).getExpr();
		checkBinaryExpr(writeStatementExpr5, Kind.PLUS, Type.INT);
		Expr leftExpr6 = ((BinaryExpr) writeStatementExpr5).getLeftExpr();
		checkNumLitExpr(leftExpr6, 3);
		Expr rightExpr7 = ((BinaryExpr) writeStatementExpr5).getRightExpr();
		checkNumLitExpr(rightExpr7, 5);
		BlockElem blockElem8 = blockElemList3.get(1);
		assertThat("", blockElem8, instanceOf(WriteStatement.class));
		Expr writeStatementExpr9 = ((WriteStatement) blockElem8).getExpr();
		checkConstExpr(writeStatementExpr9, "Z", Type.INT);
		BlockElem blockElem10 = blockElemList3.get(2);
		assertThat("", blockElem10, instanceOf(WriteStatement.class));
		Expr writeStatementExpr11 = ((WriteStatement) blockElem10).getExpr();
		Expr red12 = ((ExpandedPixelExpr) writeStatementExpr11).getRed();
		checkNumLitExpr(red12, 1);
		Expr green13 = ((ExpandedPixelExpr) writeStatementExpr11).getGreen();
		checkNumLitExpr(green13, 2);
		Expr blue14 = ((ExpandedPixelExpr) writeStatementExpr11).getBlue();
		checkNumLitExpr(blue14, 3);
	}

	@Test
	void test12() throws PLCCompilerException {
		String input = """
				string s(string s0, string s1, boolean ok)<:
				string s2 = ? ok -> s0 , s0 + s1 ;
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.STRING, "s");
		List<NameDef> params1 = program0.getParams();
		assertEquals(3, params1.size());
		NameDef paramNameDef2 = params1.get(0);
		checkNameDef(paramNameDef2, Type.STRING, "s0");
		NameDef paramNameDef3 = params1.get(1);
		checkNameDef(paramNameDef3, Type.STRING, "s1");
		NameDef paramNameDef4 = params1.get(2);
		checkNameDef(paramNameDef4, Type.BOOLEAN, "ok");
		Block programBlock5 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList6 = programBlock5.getElems();
		assertEquals(1, blockElemList6.size());
		BlockElem blockElem7 = blockElemList6.get(0);
		checkDec(blockElem7);
		NameDef nameDef8 = ((Declaration) blockElem7).getNameDef();
		checkNameDef(nameDef8, Type.STRING, "s2");
		Expr expr9 = ((Declaration) blockElem7).getInitializer();
		checkConditionalExpr(expr9, Type.STRING);
		Expr guard10 = ((ConditionalExpr) expr9).getGuardExpr();
		checkIdentExpr(guard10, "ok", Type.BOOLEAN);
		Expr trueCase11 = ((ConditionalExpr) expr9).getTrueExpr();
		checkIdentExpr(trueCase11, "s0", Type.STRING);
		Expr falseCase12 = ((ConditionalExpr) expr9).getFalseExpr();
		checkBinaryExpr(falseCase12, Kind.PLUS, Type.STRING);
		Expr leftExpr13 = ((BinaryExpr) falseCase12).getLeftExpr();
		checkIdentExpr(leftExpr13, "s0", Type.STRING);
		Expr rightExpr14 = ((BinaryExpr) falseCase12).getRightExpr();
		checkIdentExpr(rightExpr14, "s1", Type.STRING);
	}

	@Test
	void test13() throws PLCCompilerException {
		String input = """
				int f(int xx)<:
				^ Z/2 + xx;
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.INT, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(1, params1.size());
		NameDef paramNameDef2 = params1.get(0);
		checkNameDef(paramNameDef2, Type.INT, "xx");
		Block programBlock3 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList4 = programBlock3.getElems();
		assertEquals(1, blockElemList4.size());
		BlockElem blockElem5 = blockElemList4.get(0);
		assertThat("", blockElem5, instanceOf(ReturnStatement.class));
		Expr returnValueExpr6 = ((ReturnStatement) blockElem5).getE();
		checkBinaryExpr(returnValueExpr6, Kind.PLUS, Type.INT);
		Expr leftExpr7 = ((BinaryExpr) returnValueExpr6).getLeftExpr();
		checkBinaryExpr(leftExpr7, Kind.DIV, Type.INT);
		Expr leftExpr8 = ((BinaryExpr) leftExpr7).getLeftExpr();
		checkConstExpr(leftExpr8, "Z", Type.INT);
		Expr rightExpr9 = ((BinaryExpr) leftExpr7).getRightExpr();
		checkNumLitExpr(rightExpr9, 2);
		Expr rightExpr10 = ((BinaryExpr) returnValueExpr6).getRightExpr();
		checkIdentExpr(rightExpr10, "xx", Type.INT);
	}

	@Test
	void test14() throws PLCCompilerException {
		String input = """
				int f(int xx)<:
				int i = 3;
				do  i > 0 -> <:
				   write xx;
				   i = i -1;
				:>
				od;
				^i;
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.INT, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(1, params1.size());
		NameDef paramNameDef2 = params1.get(0);
		checkNameDef(paramNameDef2, Type.INT, "xx");
		Block programBlock3 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList4 = programBlock3.getElems();
		assertEquals(3, blockElemList4.size());
		BlockElem blockElem5 = blockElemList4.get(0);
		checkDec(blockElem5);
		NameDef nameDef6 = ((Declaration) blockElem5).getNameDef();
		checkNameDef(nameDef6, Type.INT, "i");
		Expr expr7 = ((Declaration) blockElem5).getInitializer();
		checkNumLitExpr(expr7, 3);
		BlockElem blockElem8 = blockElemList4.get(1);
		assertThat("", blockElem8, instanceOf(DoStatement.class));
		List<GuardedBlock> guardedBlocks9 = ((DoStatement) blockElem8).getGuardedBlocks();
		assertEquals(1, guardedBlocks9.size());
		GuardedBlock guardedBlock10 = guardedBlocks9.get(0);
		assertThat("", guardedBlock10, instanceOf(GuardedBlock.class));
		Expr guard11 = guardedBlock10.getGuard();
		checkBinaryExpr(guard11, Kind.GT, Type.BOOLEAN);
		Expr leftExpr12 = ((BinaryExpr) guard11).getLeftExpr();
		checkIdentExpr(leftExpr12, "i", Type.INT);
		Expr rightExpr13 = ((BinaryExpr) guard11).getRightExpr();
		checkNumLitExpr(rightExpr13, 0);
		Block block14 = guardedBlock10.getBlock();
		List<BlockElem> blockElemList15 = block14.getElems();
		assertEquals(2, blockElemList15.size());
		BlockElem blockElem16 = blockElemList15.get(0);
		assertThat("", blockElem16, instanceOf(WriteStatement.class));
		Expr writeStatementExpr17 = ((WriteStatement) blockElem16).getExpr();
		checkIdentExpr(writeStatementExpr17, "xx", Type.INT);
		BlockElem blockElem18 = blockElemList15.get(1);
		assertThat("", blockElem18, instanceOf(AssignmentStatement.class));
		LValue LValue19 = ((AssignmentStatement) blockElem18).getlValue();
		assertThat("", LValue19, instanceOf(LValue.class));
		String name20 = LValue19.getName();
		assertEquals("i", name20);
		assertNull(LValue19.getPixelSelector());
		assertNull(LValue19.getChannelSelector());
		Expr expr21 = ((AssignmentStatement) blockElem18).getE();
		checkBinaryExpr(expr21, Kind.MINUS, Type.INT);
		Expr leftExpr22 = ((BinaryExpr) expr21).getLeftExpr();
		checkIdentExpr(leftExpr22, "i", Type.INT);
		Expr rightExpr23 = ((BinaryExpr) expr21).getRightExpr();
		checkNumLitExpr(rightExpr23, 1);
		BlockElem blockElem24 = blockElemList4.get(2);
		assertThat("", blockElem24, instanceOf(ReturnStatement.class));
		Expr returnValueExpr25 = ((ReturnStatement) blockElem24).getE();
		checkIdentExpr(returnValueExpr25, "i", Type.INT);
	}

	@Test
	void test15() throws PLCCompilerException {
		String input = """
				int f(int xx)<:
				int i = 3;
				do i > 0 -> <:
				   string xx = "hello";
				   write xx;
				   i = i -1;
				:>
				od;
				i = 3;
				do i > 0 -> <:
				   image xx = "url";
				   write xx;
				   i = i -1;
				:>
				od;
				^i;
				:>
				""";
		AST ast = getDecoratedAST(input);
		Program program0 = checkProgram(ast, Type.INT, "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(1, params1.size());
		NameDef paramNameDef2 = params1.get(0);
		checkNameDef(paramNameDef2, Type.INT, "xx");
		Block programBlock3 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList4 = programBlock3.getElems();
		assertEquals(5, blockElemList4.size());
		BlockElem blockElem5 = blockElemList4.get(0);
		checkDec(blockElem5);
		NameDef nameDef6 = ((Declaration) blockElem5).getNameDef();
		checkNameDef(nameDef6, Type.INT, "i");
		Expr expr7 = ((Declaration) blockElem5).getInitializer();
		checkNumLitExpr(expr7, 3);
		BlockElem blockElem8 = blockElemList4.get(1);
		assertThat("", blockElem8, instanceOf(DoStatement.class));
		List<GuardedBlock> guardedBlocks9 = ((DoStatement) blockElem8).getGuardedBlocks();
		assertEquals(1, guardedBlocks9.size());
		GuardedBlock guardedBlock10 = guardedBlocks9.get(0);
		assertThat("", guardedBlock10, instanceOf(GuardedBlock.class));
		Expr guard11 = guardedBlock10.getGuard();
		checkBinaryExpr(guard11, Kind.GT, Type.BOOLEAN);
		Expr leftExpr12 = ((BinaryExpr) guard11).getLeftExpr();
		checkIdentExpr(leftExpr12, "i", Type.INT);
		Expr rightExpr13 = ((BinaryExpr) guard11).getRightExpr();
		checkNumLitExpr(rightExpr13, 0);
		Block block14 = guardedBlock10.getBlock();
		List<BlockElem> blockElemList15 = block14.getElems();
		assertEquals(3, blockElemList15.size());
		BlockElem blockElem16 = blockElemList15.get(0);
		checkDec(blockElem16);
		NameDef nameDef17 = ((Declaration) blockElem16).getNameDef();
		checkNameDef(nameDef17, Type.STRING, "xx");
		Expr expr18 = ((Declaration) blockElem16).getInitializer();
		checkStringLitExpr(expr18, "hello");
		BlockElem blockElem19 = blockElemList15.get(1);
		assertThat("", blockElem19, instanceOf(WriteStatement.class));
		Expr writeStatementExpr20 = ((WriteStatement) blockElem19).getExpr();
		checkIdentExpr(writeStatementExpr20, "xx", Type.STRING);
		BlockElem blockElem21 = blockElemList15.get(2);
		assertThat("", blockElem21, instanceOf(AssignmentStatement.class));
		LValue LValue22 = ((AssignmentStatement) blockElem21).getlValue();
		assertThat("", LValue22, instanceOf(LValue.class));
		String name23 = LValue22.getName();
		assertEquals("i", name23);
		assertNull(LValue22.getPixelSelector());
		assertNull(LValue22.getChannelSelector());
		Expr expr24 = ((AssignmentStatement) blockElem21).getE();
		checkBinaryExpr(expr24, Kind.MINUS, Type.INT);
		Expr leftExpr25 = ((BinaryExpr) expr24).getLeftExpr();
		checkIdentExpr(leftExpr25, "i", Type.INT);
		Expr rightExpr26 = ((BinaryExpr) expr24).getRightExpr();
		checkNumLitExpr(rightExpr26, 1);
		BlockElem blockElem27 = blockElemList4.get(2);
		assertThat("", blockElem27, instanceOf(AssignmentStatement.class));
		LValue LValue28 = ((AssignmentStatement) blockElem27).getlValue();
		assertThat("", LValue28, instanceOf(LValue.class));
		String name29 = LValue28.getName();
		assertEquals("i", name29);
		assertNull(LValue28.getPixelSelector());
		assertNull(LValue28.getChannelSelector());
		Expr expr30 = ((AssignmentStatement) blockElem27).getE();
		checkNumLitExpr(expr30, 3);
		BlockElem blockElem31 = blockElemList4.get(3);
		assertThat("", blockElem31, instanceOf(DoStatement.class));
		List<GuardedBlock> guardedBlocks32 = ((DoStatement) blockElem31).getGuardedBlocks();
		assertEquals(1, guardedBlocks32.size());
		GuardedBlock guardedBlock33 = guardedBlocks32.get(0);
		assertThat("", guardedBlock33, instanceOf(GuardedBlock.class));
		Expr guard34 = guardedBlock33.getGuard();
		checkBinaryExpr(guard34, Kind.GT, Type.BOOLEAN);
		Expr leftExpr35 = ((BinaryExpr) guard34).getLeftExpr();
		checkIdentExpr(leftExpr35, "i", Type.INT);
		Expr rightExpr36 = ((BinaryExpr) guard34).getRightExpr();
		checkNumLitExpr(rightExpr36, 0);
		Block block37 = guardedBlock33.getBlock();
		List<BlockElem> blockElemList38 = block37.getElems();
		assertEquals(3, blockElemList38.size());
		BlockElem blockElem39 = blockElemList38.get(0);
		checkDec(blockElem39);
		NameDef nameDef40 = ((Declaration) blockElem39).getNameDef();
		checkNameDef(nameDef40, Type.IMAGE, "xx");
		Expr expr41 = ((Declaration) blockElem39).getInitializer();
		checkStringLitExpr(expr41, "url");
		BlockElem blockElem42 = blockElemList38.get(1);
		assertThat("", blockElem42, instanceOf(WriteStatement.class));
		Expr writeStatementExpr43 = ((WriteStatement) blockElem42).getExpr();
		checkIdentExpr(writeStatementExpr43, "xx", Type.IMAGE);
		BlockElem blockElem44 = blockElemList38.get(2);
		assertThat("", blockElem44, instanceOf(AssignmentStatement.class));
		LValue LValue45 = ((AssignmentStatement) blockElem44).getlValue();
		assertThat("", LValue45, instanceOf(LValue.class));
		String name46 = LValue45.getName();
		assertEquals("i", name46);
		assertNull(LValue45.getPixelSelector());
		assertNull(LValue45.getChannelSelector());
		Expr expr47 = ((AssignmentStatement) blockElem44).getE();
		checkBinaryExpr(expr47, Kind.MINUS, Type.INT);
		Expr leftExpr48 = ((BinaryExpr) expr47).getLeftExpr();
		checkIdentExpr(leftExpr48, "i", Type.INT);
		Expr rightExpr49 = ((BinaryExpr) expr47).getRightExpr();
		checkNumLitExpr(rightExpr49, 1);
		BlockElem blockElem50 = blockElemList4.get(4);
		assertThat("", blockElem50, instanceOf(ReturnStatement.class));
		Expr returnValueExpr51 = ((ReturnStatement) blockElem50).getE();
		checkIdentExpr(returnValueExpr51, "i", Type.INT);
	}

	@Test
	void test16() throws PLCCompilerException {
		String input = """
				void f(void xx)<::>
				""";
		assertThrows(TypeCheckException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getDecoratedAST(input);
		});
	}

	@Test
	void test17() throws PLCCompilerException {
		String input = """
				void f()<:
				  int xx = 2;
				  string ss = "hello";
				  image[100,100] ii = "url";
				  image[200,200] ii1 = ii;
				  ^xx;
				  :>
				""";
		assertThrows(TypeCheckException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getDecoratedAST(input);
		});
	}

	@Test
	void test18() throws PLCCompilerException {
		String input = """
				void f()<:
				  int xx = 2+xx;
				  :>
				""";
		assertThrows(TypeCheckException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getDecoratedAST(input);
		});
	}

	@Test
	void test19() throws PLCCompilerException {
		String input = """
				image f()<:
				image i = "url";
				string x;
				string y;
				i[x,y] = [x,y,0]; #x and y must be int
				:>
				""";
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getDecoratedAST(input);
		});
	}

	@Test
	void test20() throws PLCCompilerException {
		String input = """
				string s(string s0, string s1, boolean ok)<:
				^ ? ok -> ok+1 , s0 + s1 ;
				:>
				""";
		assertThrows(TypeCheckException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getDecoratedAST(input);
		});
	}

	@Test
	void test21() throws PLCCompilerException {
		String input = """
				int f()<:
				int i = 3;
				do i > 0 -> <:
				   string xx = "hello";
				   write xx;
				   i = i -1;
				:>
				od;
				i = 3;
				do i > 0 -> <:
				   write xx;  #error xx not visible here
				   i = i -1;
				:>
				od;
				^ i;
				:>
				""";
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getDecoratedAST(input);
		});
	}

	@Test
	void test22() throws PLCCompilerException {
		String input = """
				int f() <:
				   image x;
				   x[x,y] = [y,x,x];
				   ^y;
				   :>
				""";
		assertThrows(TypeCheckException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getDecoratedAST(input);
		});
	}

	@Test
	void test23() throws PLCCompilerException {
		String input = """
				image f() <:
				   image x;
				   <:
				   boolean b;
				   :>;
				   x[z,y] = [y,b,z];
				   ^x;
				   :>
				""";
		assertThrows(TypeCheckException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getDecoratedAST(input);
		});
	}

	@Test
	void test24() throws PLCCompilerException {
		String input = """
				int f() <:
				   image x;
				   x[z,y] = [y,z,z];
				   ^y; #y not visible here
				   :>
				""";
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getDecoratedAST(input);
		});
	}

}
