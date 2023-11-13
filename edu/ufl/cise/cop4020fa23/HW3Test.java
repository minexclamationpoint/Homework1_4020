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
import edu.ufl.cise.cop4020fa23.ast.Type;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.ast.WriteStatement;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

import edu.ufl.cise.cop4020fa23.ast.ASTVisitor;




class HW3Test{
static final int TIMEOUT_MILLIS = 1000;

AST getAST(String input) throws  PLCCompilerException {
	AST ast= ComponentFactory.makeParser(input).parse();
	ASTVisitor typeChecker = ComponentFactory.makeTypeChecker();
	ast.visit(typeChecker, null);
	return ast;
}

NumLitExpr checkNumLitExpr(AST e, String value) {
	assertThat("",e, instanceOf( NumLitExpr.class));
	NumLitExpr ne = (NumLitExpr)e;
	assertEquals(value, ne.getText());
	assertEquals(Type.INT, ne.getType());
	return ne;
}

NumLitExpr checkNumLitExpr(AST e, int value) {
	assertThat("",e, instanceOf( NumLitExpr.class));
	NumLitExpr ne = (NumLitExpr)e;
	assertEquals(Integer.toString(value), ne.getText());
	assertEquals(Type.INT, ne.getType());
	return ne;
}

StringLitExpr checkStringLitExpr(AST e, String value) {
	assertThat("",e, instanceOf( StringLitExpr.class));
	StringLitExpr se = (StringLitExpr)e;
	String s = se.getText();
	assertEquals('"', s.charAt(0));  //check that first char is "
	assertEquals('"', s.charAt(s.length()-1));
	assertEquals(value, s.substring(1, s.length() - 1));
	assertEquals(Type.STRING, se.getType());
	return se;
}



BooleanLitExpr checkBooleanLitExpr(AST e, String value) {
	assertThat("",e, instanceOf( BooleanLitExpr.class));
	BooleanLitExpr be = (BooleanLitExpr)e;
	assertEquals(value, be.getText());
	assertEquals(Type.BOOLEAN, be.getType());
	return be;
}

private UnaryExpr checkUnaryExpr(AST e, Kind op, Type type) {
	assertThat("",e, instanceOf( UnaryExpr.class));
	UnaryExpr ue = (UnaryExpr)e;
	assertEquals(op, ((UnaryExpr)e).getOp());
	assertEquals(type, ue.getType());
	return (UnaryExpr)e;
}


private ConditionalExpr checkConditionalExpr(AST e, Type type) {
	assertThat("",e, instanceOf( ConditionalExpr.class));
	ConditionalExpr ce = (ConditionalExpr)e;
	assertEquals(type, ce.getType());
	return (ConditionalExpr)e;
}


BinaryExpr checkBinaryExpr(AST e, Kind expectedOp, Type type) {
	assertThat("",e, instanceOf(BinaryExpr.class));
	BinaryExpr be = (BinaryExpr)e;
	assertEquals(expectedOp, be.getOp().kind());
	assertEquals(type, be.getType());
	return be;
}


IdentExpr checkIdentExpr(AST e, String name, Type type) {
	assertThat("",e, instanceOf( IdentExpr.class));
	IdentExpr ident = (IdentExpr)e;
	assertEquals(name,ident.getName());
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
	assertThat("",e, instanceOf (PostfixExpr.class));
	PostfixExpr pfe = (PostfixExpr)e;
	AST channel = pfe.channel();
	assertEquals(hasChannelSelector, channel!= null);
	AST pixel = pfe.pixel();
	assertEquals(hasPixelSelector, pixel != null);
	assertEquals(type, pfe.getType());
	return pfe;
}

ChannelSelector checkChannelSelector(AST e, String expectedColor) {
	assertThat("",e, instanceOf(ChannelSelector.class));
	ChannelSelector chan = (ChannelSelector)e;
	assertEquals(expectedColor, getColorString(chan.color()));
	return chan;
}

ChannelSelector checkChannelSelector(AST e, Kind expectedColor) {
	assertThat("",e, instanceOf(ChannelSelector.class));
	ChannelSelector chan = (ChannelSelector)e;
	assertEquals(expectedColor, chan.color());
	return chan;
}

String getColorString(Kind kind) {
	return switch(kind) {
	case RES_red -> "red";
	case RES_blue -> "blue";
	case RES_green -> "green";
	default -> throw new IllegalArgumentException();
	};
}

LValue checkLValueName(AST lValue, String name, Type type) {
	assertThat("",lValue, instanceOf(LValue.class));
	LValue ident = (LValue)lValue;
	assertEquals(name,ident.getName());
	assertEquals(type, ident.getType());
	return ident;
}

NameDef checkNameDef(AST ast, Type type, String name) {
	assertThat("", ast, instanceOf(NameDef.class));
	NameDef nameDef = (NameDef)ast;
	assertEquals(type, nameDef.getType());
	assertEquals(name, nameDef.getName());
	assertNull(nameDef.getDimension());
	return nameDef;
}

NameDef checkNameDefDim(AST ast, Type type, String name) {
	assertThat("", ast, instanceOf(NameDef.class));
	NameDef nameDef = (NameDef)ast;
	assertEquals(type, nameDef.getType());
	assertEquals(name, nameDef.getName());
	assertNotNull(nameDef.getDimension());
	return nameDef;
}

Program checkProgram(AST ast, Type type, String name) {
	assertThat("", ast, instanceOf(Program.class));
	Program program = (Program)ast;
	assertEquals(type, program.getType());
	assertEquals(name, program.getName());
	return program;
}

Declaration checkDec(AST ast) {
	assertThat("",ast, instanceOf(Declaration.class));
	Declaration dec0 = (Declaration)ast;
	return dec0;
}


@Test
void test0() throws PLCCompilerException{
String input = """
void x() <::>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.VOID,"x");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(0,blockElemList3.size());
}

@Test
void test1() throws PLCCompilerException{
String input = """
int f(int xx, image ss, string ii, pixel p)<::>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.INT,"f");
List<NameDef> params1=program0.getParams();
assertEquals(4,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.INT,"xx");
NameDef paramNameDef3 = ((List<NameDef>)params1).get(1);
checkNameDef(paramNameDef3,Type.IMAGE,"ss");
NameDef paramNameDef4 = ((List<NameDef>)params1).get(2);
checkNameDef(paramNameDef4,Type.STRING,"ii");
NameDef paramNameDef5 = ((List<NameDef>)params1).get(3);
checkNameDef(paramNameDef5,Type.PIXEL,"p");
Block programBlock6 = ((Program)ast).getBlock();
List<BlockElem> blockElemList7=programBlock6.getElems();
assertEquals(0,blockElemList7.size());
}

@Test
void test2() throws PLCCompilerException{
String input = """
void f0(int a, boolean s, string b, pixel i, image p)<::>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.VOID,"f0");
List<NameDef> params1=program0.getParams();
assertEquals(5,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.INT,"a");
NameDef paramNameDef3 = ((List<NameDef>)params1).get(1);
checkNameDef(paramNameDef3,Type.BOOLEAN,"s");
NameDef paramNameDef4 = ((List<NameDef>)params1).get(2);
checkNameDef(paramNameDef4,Type.STRING,"b");
NameDef paramNameDef5 = ((List<NameDef>)params1).get(3);
checkNameDef(paramNameDef5,Type.PIXEL,"i");
NameDef paramNameDef6 = ((List<NameDef>)params1).get(4);
checkNameDef(paramNameDef6,Type.IMAGE,"p");
Block programBlock7 = ((Program)ast).getBlock();
List<BlockElem> blockElemList8=programBlock7.getElems();
assertEquals(0,blockElemList8.size());
}

@Test
void test3() throws PLCCompilerException{
String input = """
boolean f0() <:
string a;
int s;
image b;
pixel i;
boolean p;
image[1028,256] id;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.BOOLEAN,"f0");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(6,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.STRING,"a");
BlockElem blockElem6 = ((List<BlockElem>)blockElemList3).get(1);
checkDec(blockElem6);
NameDef nameDef7 = ((Declaration)blockElem6).getNameDef();
checkNameDef(nameDef7,Type.INT,"s");
BlockElem blockElem8 = ((List<BlockElem>)blockElemList3).get(2);
checkDec(blockElem8);
NameDef nameDef9 = ((Declaration)blockElem8).getNameDef();
checkNameDef(nameDef9,Type.IMAGE,"b");
BlockElem blockElem10 = ((List<BlockElem>)blockElemList3).get(3);
checkDec(blockElem10);
NameDef nameDef11 = ((Declaration)blockElem10).getNameDef();
checkNameDef(nameDef11,Type.PIXEL,"i");
BlockElem blockElem12 = ((List<BlockElem>)blockElemList3).get(4);
checkDec(blockElem12);
NameDef nameDef13 = ((Declaration)blockElem12).getNameDef();
checkNameDef(nameDef13,Type.BOOLEAN,"p");
BlockElem blockElem14 = ((List<BlockElem>)blockElemList3).get(5);
checkDec(blockElem14);
NameDef nameDef15 = ((Declaration)blockElem14).getNameDef();
checkNameDefDim(nameDef15,Type.IMAGE,"id");
Dimension dimension16 = ((NameDef)nameDef15).getDimension();
assertThat("",dimension16,instanceOf(Dimension.class));
Expr width17 = ((Dimension)dimension16).getWidth();
checkNumLitExpr(width17,1028);
Expr height18 = ((Dimension)dimension16).getHeight();
checkNumLitExpr(height18,256);}

@Test
void test4() throws PLCCompilerException{
String input = """
string s(int s, string f)<:
  int b;
  string ii;
   :>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.STRING,"s");
List<NameDef> params1=program0.getParams();
assertEquals(2,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.INT,"s");
NameDef paramNameDef3 = ((List<NameDef>)params1).get(1);
checkNameDef(paramNameDef3,Type.STRING,"f");
Block programBlock4 = ((Program)ast).getBlock();
List<BlockElem> blockElemList5=programBlock4.getElems();
assertEquals(2,blockElemList5.size());
BlockElem blockElem6 = ((List<BlockElem>)blockElemList5).get(0);
checkDec(blockElem6);
NameDef nameDef7 = ((Declaration)blockElem6).getNameDef();
checkNameDef(nameDef7,Type.INT,"b");
BlockElem blockElem8 = ((List<BlockElem>)blockElemList5).get(1);
checkDec(blockElem8);
NameDef nameDef9 = ((Declaration)blockElem8).getNameDef();
checkNameDef(nameDef9,Type.STRING,"ii");
}

@Test
void test5() throws PLCCompilerException{
String input = """
string p(string p)<::>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.STRING,"p");
List<NameDef> params1=program0.getParams();
assertEquals(1,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.STRING,"p");
Block programBlock3 = ((Program)ast).getBlock();
List<BlockElem> blockElemList4=programBlock3.getElems();
assertEquals(0,blockElemList4.size());
}

@Test
void test6() throws PLCCompilerException{
String input = """
string s(int s, string f)<:
  int b;
  string ii;
   :>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.STRING,"s");
List<NameDef> params1=program0.getParams();
assertEquals(2,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.INT,"s");
NameDef paramNameDef3 = ((List<NameDef>)params1).get(1);
checkNameDef(paramNameDef3,Type.STRING,"f");
Block programBlock4 = ((Program)ast).getBlock();
List<BlockElem> blockElemList5=programBlock4.getElems();
assertEquals(2,blockElemList5.size());
BlockElem blockElem6 = ((List<BlockElem>)blockElemList5).get(0);
checkDec(blockElem6);
NameDef nameDef7 = ((Declaration)blockElem6).getNameDef();
checkNameDef(nameDef7,Type.INT,"b");
BlockElem blockElem8 = ((List<BlockElem>)blockElemList5).get(1);
checkDec(blockElem8);
NameDef nameDef9 = ((Declaration)blockElem8).getNameDef();
checkNameDef(nameDef9,Type.STRING,"ii");
}

@Test
void test7() throws PLCCompilerException{
String input = """
string ff(int ff, string ss)<:
  int ss;
  string ii;
   :>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.STRING,"ff");
List<NameDef> params1=program0.getParams();
assertEquals(2,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.INT,"ff");
NameDef paramNameDef3 = ((List<NameDef>)params1).get(1);
checkNameDef(paramNameDef3,Type.STRING,"ss");
Block programBlock4 = ((Program)ast).getBlock();
List<BlockElem> blockElemList5=programBlock4.getElems();
assertEquals(2,blockElemList5.size());
BlockElem blockElem6 = ((List<BlockElem>)blockElemList5).get(0);
checkDec(blockElem6);
NameDef nameDef7 = ((Declaration)blockElem6).getNameDef();
checkNameDef(nameDef7,Type.INT,"ss");
BlockElem blockElem8 = ((List<BlockElem>)blockElemList5).get(1);
checkDec(blockElem8);
NameDef nameDef9 = ((Declaration)blockElem8).getNameDef();
checkNameDef(nameDef9,Type.STRING,"ii");
}

@Test
void test8() throws PLCCompilerException{
String input = """
int p()<:
^5;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.INT,"p");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(1,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
assertThat("",blockElem4,instanceOf(ReturnStatement.class));
Expr returnValueExpr5 = ((ReturnStatement)blockElem4).getE();
checkNumLitExpr(returnValueExpr5,5);}

@Test
void test9() throws PLCCompilerException{
String input = """
image s()<:
image p = "url";
image q = p;
int oo = q[3,4]:green;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.IMAGE,"s");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(3,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.IMAGE,"p");
Expr expr6 = ((Declaration)blockElem4).getInitializer();
checkStringLitExpr(expr6,"url");BlockElem blockElem7 = ((List<BlockElem>)blockElemList3).get(1);
checkDec(blockElem7);
NameDef nameDef8 = ((Declaration)blockElem7).getNameDef();
checkNameDef(nameDef8,Type.IMAGE,"q");
Expr expr9 = ((Declaration)blockElem7).getInitializer();
checkIdentExpr(expr9,"p",Type.IMAGE);BlockElem blockElem10 = ((List<BlockElem>)blockElemList3).get(2);
checkDec(blockElem10);
NameDef nameDef11 = ((Declaration)blockElem10).getNameDef();
checkNameDef(nameDef11,Type.INT,"oo");
Expr expr12 = ((Declaration)blockElem10).getInitializer();
checkPostfixExpr(expr12,true,true,Type.INT);
Expr expr13 = ((PostfixExpr)expr12).primary();
checkIdentExpr(expr13,"q",Type.IMAGE);PixelSelector pixel14 = ((PostfixExpr)expr12).pixel();
Expr x15 = ((PixelSelector)pixel14).xExpr();
checkNumLitExpr(x15,3);
Expr y16 = ((PixelSelector)pixel14).yExpr();
checkNumLitExpr(y16,4);ChannelSelector channel17 = ((PostfixExpr)expr12).channel();
checkChannelSelector(channel17,Kind.RES_green);
}

@Test
void test10() throws PLCCompilerException{
String input = """
image i()<:
image f = "url";
int p;
int q;
f[p,q] = [p,q,0];
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.IMAGE,"i");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(4,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.IMAGE,"f");
Expr expr6 = ((Declaration)blockElem4).getInitializer();
checkStringLitExpr(expr6,"url");BlockElem blockElem7 = ((List<BlockElem>)blockElemList3).get(1);
checkDec(blockElem7);
NameDef nameDef8 = ((Declaration)blockElem7).getNameDef();
checkNameDef(nameDef8,Type.INT,"p");
BlockElem blockElem9 = ((List<BlockElem>)blockElemList3).get(2);
checkDec(blockElem9);
NameDef nameDef10 = ((Declaration)blockElem9).getNameDef();
checkNameDef(nameDef10,Type.INT,"q");
BlockElem blockElem11 = ((List<BlockElem>)blockElemList3).get(3);
assertThat("",blockElem11,instanceOf(AssignmentStatement.class));
LValue LValue12 = ((AssignmentStatement)blockElem11).getlValue();
assertThat("",LValue12,instanceOf(LValue.class));
String name13 = ((LValue)LValue12).getName();
assertEquals("f",name13);
PixelSelector pixel14 = ((LValue)LValue12).getPixelSelector();
Expr x15 = ((PixelSelector)pixel14).xExpr();
checkIdentExpr(x15,"p",Type.INT);
Expr y16 = ((PixelSelector)pixel14).yExpr();
checkIdentExpr(y16,"q",Type.INT);assertNull(((LValue)LValue12).getChannelSelector());
Expr expr17 = ((AssignmentStatement)blockElem11).getE();
Expr red18 = ((ExpandedPixelExpr)expr17).getRed();
checkIdentExpr(red18,"p",Type.INT);
Expr green19 = ((ExpandedPixelExpr)expr17).getGreen();
checkIdentExpr(green19,"q",Type.INT);
Expr blue20 = ((ExpandedPixelExpr)expr17).getBlue();
checkNumLitExpr(blue20,0);}

@Test
void test11() throws PLCCompilerException{
String input = """
string f(string a, string b, boolean c)<:
string d = ? c -> a , a + b ;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.STRING,"f");
List<NameDef> params1=program0.getParams();
assertEquals(3,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.STRING,"a");
NameDef paramNameDef3 = ((List<NameDef>)params1).get(1);
checkNameDef(paramNameDef3,Type.STRING,"b");
NameDef paramNameDef4 = ((List<NameDef>)params1).get(2);
checkNameDef(paramNameDef4,Type.BOOLEAN,"c");
Block programBlock5 = ((Program)ast).getBlock();
List<BlockElem> blockElemList6=programBlock5.getElems();
assertEquals(1,blockElemList6.size());
BlockElem blockElem7 = ((List<BlockElem>)blockElemList6).get(0);
checkDec(blockElem7);
NameDef nameDef8 = ((Declaration)blockElem7).getNameDef();
checkNameDef(nameDef8,Type.STRING,"d");
Expr expr9 = ((Declaration)blockElem7).getInitializer();
checkConditionalExpr(expr9,Type.STRING);
Expr guard10 = ((ConditionalExpr)expr9).getGuardExpr();
checkIdentExpr(guard10,"c",Type.BOOLEAN);Expr trueCase11 = ((ConditionalExpr)expr9).getTrueExpr();
checkIdentExpr(trueCase11,"a",Type.STRING);Expr falseCase12 = ((ConditionalExpr)expr9).getFalseExpr();
checkBinaryExpr(falseCase12,Kind.PLUS,Type.STRING);
Expr leftExpr13 = ((BinaryExpr)falseCase12).getLeftExpr();
checkIdentExpr(leftExpr13,"a",Type.STRING);Expr rightExpr14 = ((BinaryExpr)falseCase12).getRightExpr();
checkIdentExpr(rightExpr14,"b",Type.STRING);}

@Test
void test12() throws PLCCompilerException{
String input = """
int f(int x)<:
^ Z/2 + x;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.INT,"f");
List<NameDef> params1=program0.getParams();
assertEquals(1,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.INT,"x");
Block programBlock3 = ((Program)ast).getBlock();
List<BlockElem> blockElemList4=programBlock3.getElems();
assertEquals(1,blockElemList4.size());
BlockElem blockElem5 = ((List<BlockElem>)blockElemList4).get(0);
assertThat("",blockElem5,instanceOf(ReturnStatement.class));
Expr returnValueExpr6 = ((ReturnStatement)blockElem5).getE();
checkBinaryExpr(returnValueExpr6,Kind.PLUS,Type.INT);
Expr leftExpr7 = ((BinaryExpr)returnValueExpr6).getLeftExpr();
checkBinaryExpr(leftExpr7,Kind.DIV,Type.INT);
Expr leftExpr8 = ((BinaryExpr)leftExpr7).getLeftExpr();
checkConstExpr(leftExpr8,"Z",Type.INT);
Expr rightExpr9 = ((BinaryExpr)leftExpr7).getRightExpr();
checkNumLitExpr(rightExpr9,2);Expr rightExpr10 = ((BinaryExpr)returnValueExpr6).getRightExpr();
checkIdentExpr(rightExpr10,"x",Type.INT);}

@Test
void test13() throws PLCCompilerException{
String input = """
int g(int y)<:
int j = 3;
do j > 0 -> <:
   write y;
   j = j - 1;
:>
od;
^j;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.INT,"g");
List<NameDef> params1=program0.getParams();
assertEquals(1,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.INT,"y");
Block programBlock3 = ((Program)ast).getBlock();
List<BlockElem> blockElemList4=programBlock3.getElems();
assertEquals(3,blockElemList4.size());
BlockElem blockElem5 = ((List<BlockElem>)blockElemList4).get(0);
checkDec(blockElem5);
NameDef nameDef6 = ((Declaration)blockElem5).getNameDef();
checkNameDef(nameDef6,Type.INT,"j");
Expr expr7 = ((Declaration)blockElem5).getInitializer();
checkNumLitExpr(expr7,3);BlockElem blockElem8 = ((List<BlockElem>)blockElemList4).get(1);
assertThat("",blockElem8,instanceOf(DoStatement.class));
List<GuardedBlock> guardedBlocks9=((DoStatement)blockElem8).getGuardedBlocks();
assertEquals(1,guardedBlocks9.size());
GuardedBlock guardedBlock10 = ((List<GuardedBlock>)guardedBlocks9).get(0);
assertThat("",guardedBlock10,instanceOf(GuardedBlock.class));
Expr guard11 = ((GuardedBlock)guardedBlock10).getGuard();
checkBinaryExpr(guard11,Kind.GT,Type.BOOLEAN);
Expr leftExpr12 = ((BinaryExpr)guard11).getLeftExpr();
checkIdentExpr(leftExpr12,"j",Type.INT);Expr rightExpr13 = ((BinaryExpr)guard11).getRightExpr();
checkNumLitExpr(rightExpr13,0);Block block14 = ((GuardedBlock)guardedBlock10).getBlock();
List<BlockElem> blockElemList15=block14.getElems();
assertEquals(2,blockElemList15.size());
BlockElem blockElem16 = ((List<BlockElem>)blockElemList15).get(0);
assertThat("",blockElem16,instanceOf(WriteStatement.class));
Expr writeStatementExpr17 = ((WriteStatement)blockElem16).getExpr();
checkIdentExpr(writeStatementExpr17,"y",Type.INT);BlockElem blockElem18 = ((List<BlockElem>)blockElemList15).get(1);
assertThat("",blockElem18,instanceOf(AssignmentStatement.class));
LValue LValue19 = ((AssignmentStatement)blockElem18).getlValue();
assertThat("",LValue19,instanceOf(LValue.class));
String name20 = ((LValue)LValue19).getName();
assertEquals("j",name20);
assertNull(LValue19.getPixelSelector());
assertNull(((LValue)LValue19).getChannelSelector());
Expr expr21 = ((AssignmentStatement)blockElem18).getE();
checkBinaryExpr(expr21,Kind.MINUS,Type.INT);
Expr leftExpr22 = ((BinaryExpr)expr21).getLeftExpr();
checkIdentExpr(leftExpr22,"j",Type.INT);Expr rightExpr23 = ((BinaryExpr)expr21).getRightExpr();
checkNumLitExpr(rightExpr23,1);BlockElem blockElem24 = ((List<BlockElem>)blockElemList4).get(2);
assertThat("",blockElem24,instanceOf(ReturnStatement.class));
Expr returnValueExpr25 = ((ReturnStatement)blockElem24).getE();
checkIdentExpr(returnValueExpr25,"j",Type.INT);}

@Test
void test14() throws PLCCompilerException{
String input = """
boolean myFunction() <:
boolean flag1 = TRUE;
boolean flag2 = FALSE;
^ TRUE;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.BOOLEAN,"myFunction");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(3,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.BOOLEAN,"flag1");
Expr expr6 = ((Declaration)blockElem4).getInitializer();
checkBooleanLitExpr(expr6,"TRUE");
BlockElem blockElem7 = ((List<BlockElem>)blockElemList3).get(1);
checkDec(blockElem7);
NameDef nameDef8 = ((Declaration)blockElem7).getNameDef();
checkNameDef(nameDef8,Type.BOOLEAN,"flag2");
Expr expr9 = ((Declaration)blockElem7).getInitializer();
checkBooleanLitExpr(expr9,"FALSE");
BlockElem blockElem10 = ((List<BlockElem>)blockElemList3).get(2);
assertThat("",blockElem10,instanceOf(ReturnStatement.class));
Expr returnValueExpr11 = ((ReturnStatement)blockElem10).getE();
checkBooleanLitExpr(returnValueExpr11,"TRUE");
}

@Test
void test15() throws PLCCompilerException{
String input = """
string myFunction() <:
   int var1 = 1;
   int var2 = 2;
   boolean condition = TRUE;
   do condition -> <: int innerVar1 = 3; int innerVar2 = 4; write innerVar1 + innerVar2; condition = FALSE; :> od;
   :>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.STRING,"myFunction");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(4,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.INT,"var1");
Expr expr6 = ((Declaration)blockElem4).getInitializer();
checkNumLitExpr(expr6,1);BlockElem blockElem7 = ((List<BlockElem>)blockElemList3).get(1);
checkDec(blockElem7);
NameDef nameDef8 = ((Declaration)blockElem7).getNameDef();
checkNameDef(nameDef8,Type.INT,"var2");
Expr expr9 = ((Declaration)blockElem7).getInitializer();
checkNumLitExpr(expr9,2);BlockElem blockElem10 = ((List<BlockElem>)blockElemList3).get(2);
checkDec(blockElem10);
NameDef nameDef11 = ((Declaration)blockElem10).getNameDef();
checkNameDef(nameDef11,Type.BOOLEAN,"condition");
Expr expr12 = ((Declaration)blockElem10).getInitializer();
checkBooleanLitExpr(expr12,"TRUE");
BlockElem blockElem13 = ((List<BlockElem>)blockElemList3).get(3);
assertThat("",blockElem13,instanceOf(DoStatement.class));
List<GuardedBlock> guardedBlocks14=((DoStatement)blockElem13).getGuardedBlocks();
assertEquals(1,guardedBlocks14.size());
GuardedBlock guardedBlock15 = ((List<GuardedBlock>)guardedBlocks14).get(0);
assertThat("",guardedBlock15,instanceOf(GuardedBlock.class));
Expr guard16 = ((GuardedBlock)guardedBlock15).getGuard();
checkIdentExpr(guard16,"condition",Type.BOOLEAN);Block block17 = ((GuardedBlock)guardedBlock15).getBlock();
List<BlockElem> blockElemList18=block17.getElems();
assertEquals(4,blockElemList18.size());
BlockElem blockElem19 = ((List<BlockElem>)blockElemList18).get(0);
checkDec(blockElem19);
NameDef nameDef20 = ((Declaration)blockElem19).getNameDef();
checkNameDef(nameDef20,Type.INT,"innerVar1");
Expr expr21 = ((Declaration)blockElem19).getInitializer();
checkNumLitExpr(expr21,3);BlockElem blockElem22 = ((List<BlockElem>)blockElemList18).get(1);
checkDec(blockElem22);
NameDef nameDef23 = ((Declaration)blockElem22).getNameDef();
checkNameDef(nameDef23,Type.INT,"innerVar2");
Expr expr24 = ((Declaration)blockElem22).getInitializer();
checkNumLitExpr(expr24,4);BlockElem blockElem25 = ((List<BlockElem>)blockElemList18).get(2);
assertThat("",blockElem25,instanceOf(WriteStatement.class));
Expr writeStatementExpr26 = ((WriteStatement)blockElem25).getExpr();
checkBinaryExpr(writeStatementExpr26,Kind.PLUS,Type.INT);
Expr leftExpr27 = ((BinaryExpr)writeStatementExpr26).getLeftExpr();
checkIdentExpr(leftExpr27,"innerVar1",Type.INT);Expr rightExpr28 = ((BinaryExpr)writeStatementExpr26).getRightExpr();
checkIdentExpr(rightExpr28,"innerVar2",Type.INT);BlockElem blockElem29 = ((List<BlockElem>)blockElemList18).get(3);
assertThat("",blockElem29,instanceOf(AssignmentStatement.class));
LValue LValue30 = ((AssignmentStatement)blockElem29).getlValue();
assertThat("",LValue30,instanceOf(LValue.class));
String name31 = ((LValue)LValue30).getName();
assertEquals("condition",name31);
assertNull(LValue30.getPixelSelector());
assertNull(((LValue)LValue30).getChannelSelector());
Expr expr32 = ((AssignmentStatement)blockElem29).getE();
checkBooleanLitExpr(expr32,"FALSE");
}

@Test
void test16() throws PLCCompilerException{
String input = """
image processImage() <:
   image img;
   img[coordX, coordY] = [coordY, coordX, coordX];
   ^img;
   :>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.IMAGE,"processImage");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(3,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.IMAGE,"img");
BlockElem blockElem6 = ((List<BlockElem>)blockElemList3).get(1);
assertThat("",blockElem6,instanceOf(AssignmentStatement.class));
LValue LValue7 = ((AssignmentStatement)blockElem6).getlValue();
assertThat("",LValue7,instanceOf(LValue.class));
String name8 = ((LValue)LValue7).getName();
assertEquals("img",name8);
PixelSelector pixel9 = ((LValue)LValue7).getPixelSelector();
Expr x10 = ((PixelSelector)pixel9).xExpr();
checkIdentExpr(x10,"coordX",Type.INT);
Expr y11 = ((PixelSelector)pixel9).yExpr();
checkIdentExpr(y11,"coordY",Type.INT);assertNull(((LValue)LValue7).getChannelSelector());
Expr expr12 = ((AssignmentStatement)blockElem6).getE();
Expr red13 = ((ExpandedPixelExpr)expr12).getRed();
checkIdentExpr(red13,"coordY",Type.INT);
Expr green14 = ((ExpandedPixelExpr)expr12).getGreen();
checkIdentExpr(green14,"coordX",Type.INT);
Expr blue15 = ((ExpandedPixelExpr)expr12).getBlue();
checkIdentExpr(blue15,"coordX",Type.INT);BlockElem blockElem16 = ((List<BlockElem>)blockElemList3).get(2);
assertThat("",blockElem16,instanceOf(ReturnStatement.class));
Expr returnValueExpr17 = ((ReturnStatement)blockElem16).getE();
checkIdentExpr(returnValueExpr17,"img",Type.IMAGE);}

@Test
void test17() throws PLCCompilerException{
String input = """
image createImage() <:
   image img;
   <:
   boolean flag;
   :>;
   img[u,flag] = [u,flag,u];
   ^img;
   :>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.IMAGE,"createImage");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(4,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.IMAGE,"img");
BlockElem blockElem6 = ((List<BlockElem>)blockElemList3).get(1);
assertThat("",blockElem6,instanceOf(StatementBlock.class));
Block block7 = ((StatementBlock)blockElem6).getBlock();
List<BlockElem> blockElemList8=block7.getElems();
assertEquals(1,blockElemList8.size());
BlockElem blockElem9 = ((List<BlockElem>)blockElemList8).get(0);
checkDec(blockElem9);
NameDef nameDef10 = ((Declaration)blockElem9).getNameDef();
checkNameDef(nameDef10,Type.BOOLEAN,"flag");
BlockElem blockElem11 = ((List<BlockElem>)blockElemList3).get(2);
assertThat("",blockElem11,instanceOf(AssignmentStatement.class));
LValue LValue12 = ((AssignmentStatement)blockElem11).getlValue();
assertThat("",LValue12,instanceOf(LValue.class));
String name13 = ((LValue)LValue12).getName();
assertEquals("img",name13);
PixelSelector pixel14 = ((LValue)LValue12).getPixelSelector();
Expr x15 = ((PixelSelector)pixel14).xExpr();
checkIdentExpr(x15,"u",Type.INT);
Expr y16 = ((PixelSelector)pixel14).yExpr();
checkIdentExpr(y16,"flag",Type.INT);assertNull(((LValue)LValue12).getChannelSelector());
Expr expr17 = ((AssignmentStatement)blockElem11).getE();
Expr red18 = ((ExpandedPixelExpr)expr17).getRed();
checkIdentExpr(red18,"u",Type.INT);
Expr green19 = ((ExpandedPixelExpr)expr17).getGreen();
checkIdentExpr(green19,"flag",Type.INT);
Expr blue20 = ((ExpandedPixelExpr)expr17).getBlue();
checkIdentExpr(blue20,"u",Type.INT);BlockElem blockElem21 = ((List<BlockElem>)blockElemList3).get(3);
assertThat("",blockElem21,instanceOf(ReturnStatement.class));
Expr returnValueExpr22 = ((ReturnStatement)blockElem21).getE();
checkIdentExpr(returnValueExpr22,"img",Type.IMAGE);}

@Test
void test18() throws PLCCompilerException{
String input = """
int f(int x) <:
  ^x;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.INT,"f");
List<NameDef> params1=program0.getParams();
assertEquals(1,params1.size());
NameDef paramNameDef2 = ((List<NameDef>)params1).get(0);
checkNameDef(paramNameDef2,Type.INT,"x");
Block programBlock3 = ((Program)ast).getBlock();
List<BlockElem> blockElemList4=programBlock3.getElems();
assertEquals(1,blockElemList4.size());
BlockElem blockElem5 = ((List<BlockElem>)blockElemList4).get(0);
assertThat("",blockElem5,instanceOf(ReturnStatement.class));
Expr returnValueExpr6 = ((ReturnStatement)blockElem5).getE();
checkIdentExpr(returnValueExpr6,"x",Type.INT);}

@Test
void test19() throws PLCCompilerException{
String input = """
void f() <:
  int x = 5;
  if (x > 3) -> <:
    x = x + 1;
  :> fi;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.VOID,"f");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(2,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.INT,"x");
Expr expr6 = ((Declaration)blockElem4).getInitializer();
checkNumLitExpr(expr6,5);BlockElem blockElem7 = ((List<BlockElem>)blockElemList3).get(1);
assertThat("",blockElem7,instanceOf(IfStatement.class));
List<GuardedBlock> guardedBlocks8=((IfStatement)blockElem7).getGuardedBlocks();
assertEquals(1,guardedBlocks8.size());
GuardedBlock guardedBlock9 = ((List<GuardedBlock>)guardedBlocks8).get(0);
assertThat("",guardedBlock9,instanceOf(GuardedBlock.class));
Expr guard10 = ((GuardedBlock)guardedBlock9).getGuard();
checkBinaryExpr(guard10,Kind.GT,Type.BOOLEAN);
Expr leftExpr11 = ((BinaryExpr)guard10).getLeftExpr();
checkIdentExpr(leftExpr11,"x",Type.INT);Expr rightExpr12 = ((BinaryExpr)guard10).getRightExpr();
checkNumLitExpr(rightExpr12,3);Block block13 = ((GuardedBlock)guardedBlock9).getBlock();
List<BlockElem> blockElemList14=block13.getElems();
assertEquals(1,blockElemList14.size());
BlockElem blockElem15 = ((List<BlockElem>)blockElemList14).get(0);
assertThat("",blockElem15,instanceOf(AssignmentStatement.class));
LValue LValue16 = ((AssignmentStatement)blockElem15).getlValue();
assertThat("",LValue16,instanceOf(LValue.class));
String name17 = ((LValue)LValue16).getName();
assertEquals("x",name17);
assertNull(LValue16.getPixelSelector());
assertNull(((LValue)LValue16).getChannelSelector());
Expr expr18 = ((AssignmentStatement)blockElem15).getE();
checkBinaryExpr(expr18,Kind.PLUS,Type.INT);
Expr leftExpr19 = ((BinaryExpr)expr18).getLeftExpr();
checkIdentExpr(leftExpr19,"x",Type.INT);Expr rightExpr20 = ((BinaryExpr)expr18).getRightExpr();
checkNumLitExpr(rightExpr20,1);}

@Test
void test20() throws PLCCompilerException{
String input = """
void main() <:
  int x = 5;
  string y = "hello";
  boolean z = TRUE;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.VOID,"main");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(3,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.INT,"x");
Expr expr6 = ((Declaration)blockElem4).getInitializer();
checkNumLitExpr(expr6,5);BlockElem blockElem7 = ((List<BlockElem>)blockElemList3).get(1);
checkDec(blockElem7);
NameDef nameDef8 = ((Declaration)blockElem7).getNameDef();
checkNameDef(nameDef8,Type.STRING,"y");
Expr expr9 = ((Declaration)blockElem7).getInitializer();
checkStringLitExpr(expr9,"hello");BlockElem blockElem10 = ((List<BlockElem>)blockElemList3).get(2);
checkDec(blockElem10);
NameDef nameDef11 = ((Declaration)blockElem10).getNameDef();
checkNameDef(nameDef11,Type.BOOLEAN,"z");
Expr expr12 = ((Declaration)blockElem10).getInitializer();
checkBooleanLitExpr(expr12,"TRUE");
}

@Test
void test21() throws PLCCompilerException{
String input = """
boolean f() <:
  boolean b = !TRUE;
  ^b;
:>
""";
	AST ast = getAST(input);
Program program0=checkProgram(ast,Type.BOOLEAN,"f");
List<NameDef> params1=program0.getParams();
assertEquals(0,params1.size());
Block programBlock2 = ((Program)ast).getBlock();
List<BlockElem> blockElemList3=programBlock2.getElems();
assertEquals(2,blockElemList3.size());
BlockElem blockElem4 = ((List<BlockElem>)blockElemList3).get(0);
checkDec(blockElem4);
NameDef nameDef5 = ((Declaration)blockElem4).getNameDef();
checkNameDef(nameDef5,Type.BOOLEAN,"b");
Expr expr6 = ((Declaration)blockElem4).getInitializer();
checkUnaryExpr(expr6,Kind.BANG,Type.BOOLEAN);
Expr expr7 = ((UnaryExpr)expr6).getExpr();
checkBooleanLitExpr(expr7,"TRUE");
BlockElem blockElem8 = ((List<BlockElem>)blockElemList3).get(1);
assertThat("",blockElem8,instanceOf(ReturnStatement.class));
Expr returnValueExpr9 = ((ReturnStatement)blockElem8).getE();
checkIdentExpr(returnValueExpr9,"b",Type.BOOLEAN);}

@Test
void test22() throws PLCCompilerException{
String input = """
string x(string f,int f)<::>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test23() throws PLCCompilerException{
String input = """
string x(string f,string f)<::>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test24() throws PLCCompilerException{
String input = """
void f() <:
  int x = y + 5;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test25() throws PLCCompilerException{
String input = """
void s(void ff)<::>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test26() throws PLCCompilerException{
String input = """
void f()<:
  int aa = 2;
  string zz = "hello";
  image[100,100] xx = "url";
  image[200,200] xxx = xx;
  ^ii;
  :>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test27() throws PLCCompilerException{
String input = """
void x()<:
  int ss = 2+ss;
  :>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test28() throws PLCCompilerException{
String input = """
string f(string a, string b, boolean c)<:
^ ? c -> c+1 , a + b ;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test29() throws PLCCompilerException{
String input = """
int f() <:
int j = 3;
do j > 0 -> <:
   string str = "World";
   write str;
   j = j - 1;
:>
od;
j = 3;
do j > 0 -> <:
   write missingVar;
   j = j - 1;
:>
od;
^ j;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test30() throws PLCCompilerException{
String input = """
int getImageValue() <:
   image img;
   img[coordX, coordY] = [coordY, coordX, coordX];
   ^coordY;
   :>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test31() throws PLCCompilerException{
String input = """
int calculateValue() <:
   image img;
   img[imgCoord, yCoord] = [yCoord, imgCoord, imgCoord];
   ^yCoord;
   :>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test32() throws PLCCompilerException{
String input = """
image generateImage() <:
   image img;
   <:
   boolean flag;
   :>;
   img[p,q] = [q,flag,p];
   ^img;
   :>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test33() throws PLCCompilerException{
String input = """
void myFunction() <:
  int alpha = 1;
  int alpha = 2;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test34() throws PLCCompilerException{
String input = """
int myFunction() <:
  ^y;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test35() throws PLCCompilerException{
String input = """
void f() <:
  int y = "hello world";
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test36() throws PLCCompilerException{
String input = """
void f() <:
  int a = 1;
  string b = "Basic Code";
  int c = a + b;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test37() throws PLCCompilerException{
String input = """
void f() <:
  <:
    int x = 10;
  :>;
  int y = x;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test38() throws PLCCompilerException{
String input = """
void main() <:
  int y = x;
  int x = 10;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test39() throws PLCCompilerException{
String input = """
void f() <:
  int x = "string";
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test40() throws PLCCompilerException{
String input = """
int f() <:
  ^"string";
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test41() throws PLCCompilerException{
String input = """
void f() <:
  int x = 5;
  if (x + "string") -> <:
    x = x + 1;
  :> fi;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


@Test
void test42() throws PLCCompilerException{
String input = """
boolean f() <:
  int x = !5;
  ^x;
:>
""";
assertThrows(TypeCheckException.class, () -> {
    @SuppressWarnings("unused")
	AST ast = getAST(input);
});
}


}
