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
import edu.ufl.cise.cop4020fa23.ast.IfStatement;
import edu.ufl.cise.cop4020fa23.ast.LValue;
import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.Program;
import edu.ufl.cise.cop4020fa23.ast.ReturnStatement;
import edu.ufl.cise.cop4020fa23.ast.StatementBlock;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.ast.WriteStatement;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

class ParserTest_starter {
	static final int TIMEOUT_MILLIS = 1000;

	AST getAST(String input) throws PLCCompilerException {
		return ComponentFactory.makeParser(input).parse();
	}

	NumLitExpr checkNumLitExpr(AST e, String value) {
		assertThat("", e, instanceOf(NumLitExpr.class));
		NumLitExpr ne = (NumLitExpr) e;
		assertEquals(value, ne.getText());
		return ne;
	}

	NumLitExpr checkNumLitExpr(AST e, int value) {
		assertThat("", e, instanceOf(NumLitExpr.class));
		NumLitExpr ne = (NumLitExpr) e;
		assertEquals(Integer.toString(value), ne.getText());
		return ne;
	}

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

	private UnaryExpr checkUnaryExpr(AST e, Kind op) {
		assertThat("", e, instanceOf(UnaryExpr.class));
		assertEquals(op, ((UnaryExpr) e).getOp());
		return (UnaryExpr) e;
	}

	private ConditionalExpr checkConditionalExpr(AST e) {
		assertThat("", e, instanceOf(ConditionalExpr.class));
		return (ConditionalExpr) e;
	}

	BinaryExpr checkBinaryExpr(AST e, Kind expectedOp) {
		assertThat("", e, instanceOf(BinaryExpr.class));
		BinaryExpr be = (BinaryExpr) e;
		assertEquals(expectedOp, be.getOp().kind());
		return be;
	}

	IdentExpr checkIdentExpr(AST e, String name) {
		assertThat("", e, instanceOf(IdentExpr.class));
		IdentExpr ident = (IdentExpr) e;
		assertEquals(name, ident.getName());
		return ident;
	}

	BooleanLitExpr checkBooleanLitExpr(AST e, String value) {
		assertThat("", e, instanceOf(BooleanLitExpr.class));
		BooleanLitExpr be = (BooleanLitExpr) e;
		assertEquals(value, be.getText());
		return be;
	}

	ConstExpr checkConstExpr(AST e, String name) {
		assertThat("", e, instanceOf(ConstExpr.class));
		ConstExpr ce = (ConstExpr) e;
		assertEquals(name, ce.getName());
		return ce;
	}

	PostfixExpr checkPostfixExpr(AST e, boolean hasPixelSelector, boolean hasChannelSelector) {
		assertThat("", e, instanceOf(PostfixExpr.class));
		PostfixExpr pfe = (PostfixExpr) e;
		AST channel = pfe.channel();
		assertEquals(hasChannelSelector, channel != null);
		AST pixel = pfe.pixel();
		assertEquals(hasPixelSelector, pixel != null);
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

	LValue checkLValueName(AST lValue, String name) {
		assertThat("", lValue, instanceOf(LValue.class));
		LValue ident = (LValue) lValue;
		assertEquals(name, ident.getName());
		return ident;
	}

	NameDef checkNameDef(AST ast, String type, String name) {
		assertThat("", ast, instanceOf(NameDef.class));
		NameDef nameDef = (NameDef) ast;
		assertEquals(type, nameDef.getTypeToken().text());
		assertEquals(name, nameDef.getName());
		assertNull(nameDef.getDimension());
		return nameDef;
	}

	NameDef checkNameDefDim(AST ast, String type, String name) {
		assertThat("", ast, instanceOf(NameDef.class));
		NameDef nameDef = (NameDef) ast;
		assertEquals(type, nameDef.getTypeToken().text());
		assertEquals(name, nameDef.getName());
		assertNotNull(nameDef.getDimension());
		return nameDef;
	}

	Program checkProgram(AST ast, String type, String name) {
		assertThat("", ast, instanceOf(Program.class));
		Program program = (Program) ast;
		assertEquals(type, program.getTypeToken().text());
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
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "void", "prog0");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(0, blockElemList3.size());
	}

	@Test
	void test1() throws PLCCompilerException {
		String input = """
				int prog()<:int a; string s; :>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "int", "prog");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(2, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, "int", "a");
		BlockElem blockElem6 = ((List<BlockElem>) blockElemList3).get(1);
		checkDec(blockElem6);
		NameDef nameDef7 = ((Declaration) blockElem6).getNameDef();
		checkNameDef(nameDef7, "string", "s");
	}

	@Test
	void test2() throws PLCCompilerException {
		String input = """
				void p0(int a, string s, boolean b, image i, pixel p)<::>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "void", "p0");
		List<NameDef> params1 = program0.getParams();
		assertEquals(5, params1.size());
		NameDef paramNameDef2 = ((List<NameDef>) params1).get(0);
		checkNameDef(paramNameDef2, "int", "a");
		NameDef paramNameDef3 = ((List<NameDef>) params1).get(1);
		checkNameDef(paramNameDef3, "string", "s");
		NameDef paramNameDef4 = ((List<NameDef>) params1).get(2);
		checkNameDef(paramNameDef4, "boolean", "b");
		NameDef paramNameDef5 = ((List<NameDef>) params1).get(3);
		checkNameDef(paramNameDef5, "image", "i");
		NameDef paramNameDef6 = ((List<NameDef>) params1).get(4);
		checkNameDef(paramNameDef6, "pixel", "p");
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
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "boolean", "p0");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(6, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, "int", "a");
		BlockElem blockElem6 = ((List<BlockElem>) blockElemList3).get(1);
		checkDec(blockElem6);
		NameDef nameDef7 = ((Declaration) blockElem6).getNameDef();
		checkNameDef(nameDef7, "string", "s");
		BlockElem blockElem8 = ((List<BlockElem>) blockElemList3).get(2);
		checkDec(blockElem8);
		NameDef nameDef9 = ((Declaration) blockElem8).getNameDef();
		checkNameDef(nameDef9, "boolean", "b");
		BlockElem blockElem10 = ((List<BlockElem>) blockElemList3).get(3);
		checkDec(blockElem10);
		NameDef nameDef11 = ((Declaration) blockElem10).getNameDef();
		checkNameDef(nameDef11, "image", "i");
		BlockElem blockElem12 = ((List<BlockElem>) blockElemList3).get(4);
		checkDec(blockElem12);
		NameDef nameDef13 = ((Declaration) blockElem12).getNameDef();
		checkNameDef(nameDef13, "pixel", "p");
		BlockElem blockElem14 = ((List<BlockElem>) blockElemList3).get(5);
		checkDec(blockElem14);
		NameDef nameDef15 = ((Declaration) blockElem14).getNameDef();
		checkNameDefDim(nameDef15, "image", "d");
		Dimension dimension16 = ((NameDef) nameDef15).getDimension();
		assertThat("", dimension16, instanceOf(Dimension.class));
		Expr width17 = ((Dimension) dimension16).getWidth();
		checkNumLitExpr(width17, 1028);
		Expr height18 = ((Dimension) dimension16).getHeight();
		checkNumLitExpr(height18, 256);
	}

	@Test
	void test4() throws PLCCompilerException {
		String input = """
				string sss()<:
				write 3+5;
				write x;
				write Z;
				write [1,2,3];
				:>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "string", "sss");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(4, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		assertThat("", blockElem4, instanceOf(WriteStatement.class));
		Expr writeStatementExpr5 = ((WriteStatement) blockElem4).getExpr();
		checkBinaryExpr(writeStatementExpr5, Kind.PLUS);
		Expr leftExpr6 = ((BinaryExpr) writeStatementExpr5).getLeftExpr();
		checkNumLitExpr(leftExpr6, 3);
		Expr rightExpr7 = ((BinaryExpr) writeStatementExpr5).getRightExpr();
		checkNumLitExpr(rightExpr7, 5);
		BlockElem blockElem8 = ((List<BlockElem>) blockElemList3).get(1);
		assertThat("", blockElem8, instanceOf(WriteStatement.class));
		Expr writeStatementExpr9 = ((WriteStatement) blockElem8).getExpr();
		checkIdentExpr(writeStatementExpr9, "x");
		BlockElem blockElem10 = ((List<BlockElem>) blockElemList3).get(2);
		assertThat("", blockElem10, instanceOf(WriteStatement.class));
		Expr writeStatementExpr11 = ((WriteStatement) blockElem10).getExpr();
		checkConstExpr(writeStatementExpr11, "Z");
		BlockElem blockElem12 = ((List<BlockElem>) blockElemList3).get(3);
		assertThat("", blockElem12, instanceOf(WriteStatement.class));
		Expr writeStatementExpr13 = ((WriteStatement) blockElem12).getExpr();
		Expr red14 = ((ExpandedPixelExpr) writeStatementExpr13).getRed();
		checkNumLitExpr(red14, 1);
		Expr green15 = ((ExpandedPixelExpr) writeStatementExpr13).getGreen();
		checkNumLitExpr(green15, 2);
		Expr blue16 = ((ExpandedPixelExpr) writeStatementExpr13).getBlue();
		checkNumLitExpr(blue16, 3);
	}

	@Test
	void test5() throws PLCCompilerException {
		String input = """
				pixel ppp() <:
				a = 3;
				a[x,y] = 4;
				a[x,y]:red = 5;
				a:green = 5;
				:>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "pixel", "ppp");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(4, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		assertThat("", blockElem4, instanceOf(AssignmentStatement.class));
		LValue LValue5 = ((AssignmentStatement) blockElem4).getlValue();
		assertThat("", LValue5, instanceOf(LValue.class));
		String name6 = ((LValue) LValue5).getName();
		assertEquals("a", name6);
		assertNull(LValue5.getPixelSelector());
		assertNull(((LValue) LValue5).getChannelSelector());
		Expr expr7 = ((AssignmentStatement) blockElem4).getE();
		checkNumLitExpr(expr7, 3);
		BlockElem blockElem8 = ((List<BlockElem>) blockElemList3).get(1);
		assertThat("", blockElem8, instanceOf(AssignmentStatement.class));
		LValue LValue9 = ((AssignmentStatement) blockElem8).getlValue();
		assertThat("", LValue9, instanceOf(LValue.class));
		String name10 = ((LValue) LValue9).getName();
		assertEquals("a", name10);
		PixelSelector pixel11 = ((LValue) LValue9).getPixelSelector();
		Expr x12 = ((PixelSelector) pixel11).xExpr();
		checkIdentExpr(x12, "x");
		Expr y13 = ((PixelSelector) pixel11).yExpr();
		checkIdentExpr(y13, "y");
		assertNull(((LValue) LValue9).getChannelSelector());
		Expr expr14 = ((AssignmentStatement) blockElem8).getE();
		checkNumLitExpr(expr14, 4);
		BlockElem blockElem15 = ((List<BlockElem>) blockElemList3).get(2);
		assertThat("", blockElem15, instanceOf(AssignmentStatement.class));
		LValue LValue16 = ((AssignmentStatement) blockElem15).getlValue();
		assertThat("", LValue16, instanceOf(LValue.class));
		String name17 = ((LValue) LValue16).getName();
		assertEquals("a", name17);
		PixelSelector pixel18 = ((LValue) LValue16).getPixelSelector();
		Expr x19 = ((PixelSelector) pixel18).xExpr();
		checkIdentExpr(x19, "x");
		Expr y20 = ((PixelSelector) pixel18).yExpr();
		checkIdentExpr(y20, "y");
		ChannelSelector channel21 = ((LValue) LValue16).getChannelSelector();
		checkChannelSelector(channel21, Kind.RES_red);
		Expr expr22 = ((AssignmentStatement) blockElem15).getE();
		checkNumLitExpr(expr22, 5);
		BlockElem blockElem23 = ((List<BlockElem>) blockElemList3).get(3);
		assertThat("", blockElem23, instanceOf(AssignmentStatement.class));
		LValue LValue24 = ((AssignmentStatement) blockElem23).getlValue();
		assertThat("", LValue24, instanceOf(LValue.class));
		String name25 = ((LValue) LValue24).getName();
		assertEquals("a", name25);
		assertNull(LValue24.getPixelSelector());
		ChannelSelector channel26 = ((LValue) LValue24).getChannelSelector();
		checkChannelSelector(channel26, Kind.RES_green);
		Expr expr27 = ((AssignmentStatement) blockElem23).getE();
		checkNumLitExpr(expr27, 5);
	}

	@Test
	void test6() throws PLCCompilerException {
		String input = """
				image sss()<:
				do 1 -> <: write 2; :>
				 []  a -> <: b = d; :>
				od;
				:>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "image", "sss");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(1, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		assertThat("", blockElem4, instanceOf(DoStatement.class));
		List<GuardedBlock> guardedBlocks5 = ((DoStatement) blockElem4).getGuardedBlocks();
		assertEquals(2, guardedBlocks5.size());
		GuardedBlock guardedBlock6 = ((List<GuardedBlock>) guardedBlocks5).get(0);
		assertThat("", guardedBlock6, instanceOf(GuardedBlock.class));
		Expr guard7 = ((GuardedBlock) guardedBlock6).getGuard();
		checkNumLitExpr(guard7, 1);
		Block block8 = ((GuardedBlock) guardedBlock6).getBlock();
		List<BlockElem> blockElemList9 = block8.getElems();
		assertEquals(1, blockElemList9.size());
		BlockElem blockElem10 = ((List<BlockElem>) blockElemList9).get(0);
		assertThat("", blockElem10, instanceOf(WriteStatement.class));
		Expr writeStatementExpr11 = ((WriteStatement) blockElem10).getExpr();
		checkNumLitExpr(writeStatementExpr11, 2);
		GuardedBlock guardedBlock12 = ((List<GuardedBlock>) guardedBlocks5).get(1);
		assertThat("", guardedBlock12, instanceOf(GuardedBlock.class));
		Expr guard13 = ((GuardedBlock) guardedBlock12).getGuard();
		checkIdentExpr(guard13, "a");
		Block block14 = ((GuardedBlock) guardedBlock12).getBlock();
		List<BlockElem> blockElemList15 = block14.getElems();
		assertEquals(1, blockElemList15.size());
		BlockElem blockElem16 = ((List<BlockElem>) blockElemList15).get(0);
		assertThat("", blockElem16, instanceOf(AssignmentStatement.class));
		LValue LValue17 = ((AssignmentStatement) blockElem16).getlValue();
		assertThat("", LValue17, instanceOf(LValue.class));
		String name18 = ((LValue) LValue17).getName();
		assertEquals("b", name18);
		assertNull(LValue17.getPixelSelector());
		assertNull(((LValue) LValue17).getChannelSelector());
		Expr expr19 = ((AssignmentStatement) blockElem16).getE();
		checkIdentExpr(expr19, "d");
	}

	@Test
	void test7() throws PLCCompilerException {
		String input = """
				image sss()<:
				if 1 -> <: write 2; :>
				[]   a -> <: b = d; :>
				fi;
				:>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "image", "sss");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(1, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		assertThat("", blockElem4, instanceOf(IfStatement.class));
		List<GuardedBlock> guardedBlocks5 = ((IfStatement) blockElem4).getGuardedBlocks();
		assertEquals(2, guardedBlocks5.size());
		GuardedBlock guardedBlock6 = ((List<GuardedBlock>) guardedBlocks5).get(0);
		assertThat("", guardedBlock6, instanceOf(GuardedBlock.class));
		Expr guard7 = ((GuardedBlock) guardedBlock6).getGuard();
		checkNumLitExpr(guard7, 1);
		Block block8 = ((GuardedBlock) guardedBlock6).getBlock();
		List<BlockElem> blockElemList9 = block8.getElems();
		assertEquals(1, blockElemList9.size());
		BlockElem blockElem10 = ((List<BlockElem>) blockElemList9).get(0);
		assertThat("", blockElem10, instanceOf(WriteStatement.class));
		Expr writeStatementExpr11 = ((WriteStatement) blockElem10).getExpr();
		checkNumLitExpr(writeStatementExpr11, 2);
		GuardedBlock guardedBlock12 = ((List<GuardedBlock>) guardedBlocks5).get(1);
		assertThat("", guardedBlock12, instanceOf(GuardedBlock.class));
		Expr guard13 = ((GuardedBlock) guardedBlock12).getGuard();
		checkIdentExpr(guard13, "a");
		Block block14 = ((GuardedBlock) guardedBlock12).getBlock();
		List<BlockElem> blockElemList15 = block14.getElems();
		assertEquals(1, blockElemList15.size());
		BlockElem blockElem16 = ((List<BlockElem>) blockElemList15).get(0);
		assertThat("", blockElem16, instanceOf(AssignmentStatement.class));
		LValue LValue17 = ((AssignmentStatement) blockElem16).getlValue();
		assertThat("", LValue17, instanceOf(LValue.class));
		String name18 = ((LValue) LValue17).getName();
		assertEquals("b", name18);
		assertNull(LValue17.getPixelSelector());
		assertNull(((LValue) LValue17).getChannelSelector());
		Expr expr19 = ((AssignmentStatement) blockElem16).getE();
		checkIdentExpr(expr19, "d");
	}

	@Test
	void test8() throws PLCCompilerException {
		String input = """
				void p() <:
				   ^3;
				   :>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "void", "p");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(1, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		assertThat("", blockElem4, instanceOf(ReturnStatement.class));
		Expr returnValueExpr5 = ((ReturnStatement) blockElem4).getE();
		checkNumLitExpr(returnValueExpr5, 3);
	}

	@Test
	void test9() throws PLCCompilerException {
		String input = """
				void p() <:
				   <::>;
				   :>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "void", "p");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(1, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		assertThat("", blockElem4, instanceOf(StatementBlock.class));
		Block block5 = ((StatementBlock) blockElem4).getBlock();
		List<BlockElem> blockElemList6 = block5.getElems();
		assertEquals(0, blockElemList6.size());
	}

	@Test
	void test10() throws PLCCompilerException {
		String input = """
				void p() <:
				int r;
				a=Z;
				boolean b;
				<: a[x,y]:red = b; :>;
				c=2;
				:>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "void", "p");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(5, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, "int", "r");
		BlockElem blockElem6 = ((List<BlockElem>) blockElemList3).get(1);
		assertThat("", blockElem6, instanceOf(AssignmentStatement.class));
		LValue LValue7 = ((AssignmentStatement) blockElem6).getlValue();
		assertThat("", LValue7, instanceOf(LValue.class));
		String name8 = ((LValue) LValue7).getName();
		assertEquals("a", name8);
		assertNull(LValue7.getPixelSelector());
		assertNull(((LValue) LValue7).getChannelSelector());
		Expr expr9 = ((AssignmentStatement) blockElem6).getE();
		checkConstExpr(expr9, "Z");
		BlockElem blockElem10 = ((List<BlockElem>) blockElemList3).get(2);
		checkDec(blockElem10);
		NameDef nameDef11 = ((Declaration) blockElem10).getNameDef();
		checkNameDef(nameDef11, "boolean", "b");
		BlockElem blockElem12 = ((List<BlockElem>) blockElemList3).get(3);
		assertThat("", blockElem12, instanceOf(StatementBlock.class));
		Block block13 = ((StatementBlock) blockElem12).getBlock();
		List<BlockElem> blockElemList14 = block13.getElems();
		assertEquals(1, blockElemList14.size());
		BlockElem blockElem15 = ((List<BlockElem>) blockElemList14).get(0);
		assertThat("", blockElem15, instanceOf(AssignmentStatement.class));
		LValue LValue16 = ((AssignmentStatement) blockElem15).getlValue();
		assertThat("", LValue16, instanceOf(LValue.class));
		String name17 = ((LValue) LValue16).getName();
		assertEquals("a", name17);
		PixelSelector pixel18 = ((LValue) LValue16).getPixelSelector();
		Expr x19 = ((PixelSelector) pixel18).xExpr();
		checkIdentExpr(x19, "x");
		Expr y20 = ((PixelSelector) pixel18).yExpr();
		checkIdentExpr(y20, "y");
		ChannelSelector channel21 = ((LValue) LValue16).getChannelSelector();
		checkChannelSelector(channel21, Kind.RES_red);
		Expr expr22 = ((AssignmentStatement) blockElem15).getE();
		checkIdentExpr(expr22, "b");
		BlockElem blockElem23 = ((List<BlockElem>) blockElemList3).get(4);
		assertThat("", blockElem23, instanceOf(AssignmentStatement.class));
		LValue LValue24 = ((AssignmentStatement) blockElem23).getlValue();
		assertThat("", LValue24, instanceOf(LValue.class));
		String name25 = ((LValue) LValue24).getName();
		assertEquals("c", name25);
		assertNull(LValue24.getPixelSelector());
		assertNull(((LValue) LValue24).getChannelSelector());
		Expr expr26 = ((AssignmentStatement) blockElem23).getE();
		checkNumLitExpr(expr26, 2);
	}

	@Test
	void test11() throws PLCCompilerException {
		String input = """
				int f()
				<:
				int a = TRUE;
				string b = 3;
				pixel p = "hello";
				:>
				""";
		AST ast = getAST(input);
		Program program0 = checkProgram(ast, "int", "f");
		List<NameDef> params1 = program0.getParams();
		assertEquals(0, params1.size());
		Block programBlock2 = ((Program) ast).getBlock();
		List<BlockElem> blockElemList3 = programBlock2.getElems();
		assertEquals(3, blockElemList3.size());
		BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
		checkDec(blockElem4);
		NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
		checkNameDef(nameDef5, "int", "a");
		Expr expr6 = ((Declaration) blockElem4).getInitializer();
		checkBooleanLitExpr(expr6, "TRUE");
		BlockElem blockElem7 = ((List<BlockElem>) blockElemList3).get(1);
		checkDec(blockElem7);
		NameDef nameDef8 = ((Declaration) blockElem7).getNameDef();
		checkNameDef(nameDef8, "string", "b");
		Expr expr9 = ((Declaration) blockElem7).getInitializer();
		checkNumLitExpr(expr9, 3);
		BlockElem blockElem10 = ((List<BlockElem>) blockElemList3).get(2);
		checkDec(blockElem10);
		NameDef nameDef11 = ((Declaration) blockElem10).getNameDef();
		checkNameDef(nameDef11, "pixel", "p");
		Expr expr12 = ((Declaration) blockElem10).getInitializer();
		checkStringLitExpr(expr12, "hello");
	}

	@Test
	void test12() throws PLCCompilerException {
		String input = """
				int s()<:
				xx = 22
				:>
				""";
		assertThrows(SyntaxException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getAST(input);
		});
	}

	@Test
	void test13() throws PLCCompilerException {
		String input = """
				boolean prog()<:
				x = @;
				:>
				""";
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getAST(input);
		});
	}

	@Test
	void test14() throws PLCCompilerException {
		String input = """
				pixel ppp() <:
				a = 3;
				a[x,y] = 4;
				a[x,y]:red = 5;
				a:green = 5;
				:>
				trailing_stuff
				""";
		assertThrows(SyntaxException.class, () -> {
			@SuppressWarnings("unused")
			AST ast = getAST(input);
		});
	}

}
