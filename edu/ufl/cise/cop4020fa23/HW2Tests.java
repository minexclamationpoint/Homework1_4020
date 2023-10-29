package edu.ufl.cise.cop4020fa23;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

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

import edu.ufl.cise.cop4020fa23.Kind;
import edu.ufl.cise.cop4020fa23.ComponentFactory;


class HW2Tests {
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
        assertEquals('"', s.charAt(0));  //check that first char is "
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
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    int prog() <::>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "int", "prog");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(0, blockElemList3.size());
        });
    }

    @Test
    void test1() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void prog()<:image a; pixel s; :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "prog");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(2, blockElemList3.size());
            BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
            checkDec(blockElem4);
            NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
            checkNameDef(nameDef5, "image", "a");
            BlockElem blockElem6 = ((List<BlockElem>) blockElemList3).get(1);
            checkDec(blockElem6);
            NameDef nameDef7 = ((Declaration) blockElem6).getNameDef();
            checkNameDef(nameDef7, "pixel", "s");
        });
    }

    @Test
    void test2() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    boolean p0(image a, pixel s, int b, int i, string p)<::>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "boolean", "p0");
            List<NameDef> params1 = program0.getParams();
            assertEquals(5, params1.size());
            NameDef paramNameDef2 = ((List<NameDef>) params1).get(0);
            checkNameDef(paramNameDef2, "image", "a");
            NameDef paramNameDef3 = ((List<NameDef>) params1).get(1);
            checkNameDef(paramNameDef3, "pixel", "s");
            NameDef paramNameDef4 = ((List<NameDef>) params1).get(2);
            checkNameDef(paramNameDef4, "int", "b");
            NameDef paramNameDef5 = ((List<NameDef>) params1).get(3);
            checkNameDef(paramNameDef5, "int", "i");
            NameDef paramNameDef6 = ((List<NameDef>) params1).get(4);
            checkNameDef(paramNameDef6, "string", "p");
            Block programBlock7 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList8 = programBlock7.getElems();
            assertEquals(0, blockElemList8.size());
        });
    }

    @Test
    void test3() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void p0() <:
                    image a;
                    int s;
                    string b;
                    pixel i;
                    boolean p;
                    image[1,0] d;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "p0");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(6, blockElemList3.size());
            BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
            checkDec(blockElem4);
            NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
            checkNameDef(nameDef5, "image", "a");
            BlockElem blockElem6 = ((List<BlockElem>) blockElemList3).get(1);
            checkDec(blockElem6);
            NameDef nameDef7 = ((Declaration) blockElem6).getNameDef();
            checkNameDef(nameDef7, "int", "s");
            BlockElem blockElem8 = ((List<BlockElem>) blockElemList3).get(2);
            checkDec(blockElem8);
            NameDef nameDef9 = ((Declaration) blockElem8).getNameDef();
            checkNameDef(nameDef9, "string", "b");
            BlockElem blockElem10 = ((List<BlockElem>) blockElemList3).get(3);
            checkDec(blockElem10);
            NameDef nameDef11 = ((Declaration) blockElem10).getNameDef();
            checkNameDef(nameDef11, "pixel", "i");
            BlockElem blockElem12 = ((List<BlockElem>) blockElemList3).get(4);
            checkDec(blockElem12);
            NameDef nameDef13 = ((Declaration) blockElem12).getNameDef();
            checkNameDef(nameDef13, "boolean", "p");
            BlockElem blockElem14 = ((List<BlockElem>) blockElemList3).get(5);
            checkDec(blockElem14);
            NameDef nameDef15 = ((Declaration) blockElem14).getNameDef();
            checkNameDefDim(nameDef15, "image", "d");
            Dimension dimension16 = ((NameDef) nameDef15).getDimension();
            assertThat("", dimension16, instanceOf(Dimension.class));
            Expr width17 = ((Dimension) dimension16).getWidth();
            checkNumLitExpr(width17, 1);
            Expr height18 = ((Dimension) dimension16).getHeight();
            checkNumLitExpr(height18, 0);
        });
    }

    @Test
    void test4() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    string sss()<:
                    write "string"+5;
                    write TRUE;
                    write BLACK;
                    write [a,TRUE,"hello"];
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
            checkStringLitExpr(leftExpr6, "string");
            Expr rightExpr7 = ((BinaryExpr) writeStatementExpr5).getRightExpr();
            checkNumLitExpr(rightExpr7, 5);
            BlockElem blockElem8 = ((List<BlockElem>) blockElemList3).get(1);
            assertThat("", blockElem8, instanceOf(WriteStatement.class));
            Expr writeStatementExpr9 = ((WriteStatement) blockElem8).getExpr();
            checkBooleanLitExpr(writeStatementExpr9, "TRUE");
            BlockElem blockElem10 = ((List<BlockElem>) blockElemList3).get(2);
            assertThat("", blockElem10, instanceOf(WriteStatement.class));
            Expr writeStatementExpr11 = ((WriteStatement) blockElem10).getExpr();
            checkConstExpr(writeStatementExpr11, "BLACK");
            BlockElem blockElem12 = ((List<BlockElem>) blockElemList3).get(3);
            assertThat("", blockElem12, instanceOf(WriteStatement.class));
            Expr writeStatementExpr13 = ((WriteStatement) blockElem12).getExpr();
            Expr red14 = ((ExpandedPixelExpr) writeStatementExpr13).getRed();
            checkIdentExpr(red14, "a");
            Expr green15 = ((ExpandedPixelExpr) writeStatementExpr13).getGreen();
            checkBooleanLitExpr(green15, "TRUE");

            Expr blue16 = ((ExpandedPixelExpr) writeStatementExpr13).getBlue();
            checkStringLitExpr(blue16, "hello");
        });
    }

    @Test
    void test5() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    pixel ppp() <:
                    b = 3;
                    b[x,y] = width 4;
                    b[x,y]:blue = height 5;
                    b:red = -5;
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
            assertEquals("b", name6);
            assertNull(LValue5.getPixelSelector());
            assertNull(((LValue) LValue5).getChannelSelector());
            Expr expr7 = ((AssignmentStatement) blockElem4).getE();
            checkNumLitExpr(expr7, 3);
            BlockElem blockElem8 = ((List<BlockElem>) blockElemList3).get(1);
            assertThat("", blockElem8, instanceOf(AssignmentStatement.class));
            LValue LValue9 = ((AssignmentStatement) blockElem8).getlValue();
            assertThat("", LValue9, instanceOf(LValue.class));
            String name10 = ((LValue) LValue9).getName();
            assertEquals("b", name10);
            PixelSelector pixel11 = ((LValue) LValue9).getPixelSelector();
            Expr x12 = ((PixelSelector) pixel11).xExpr();
            checkIdentExpr(x12, "x");
            Expr y13 = ((PixelSelector) pixel11).yExpr();
            checkIdentExpr(y13, "y");
            assertNull(((LValue) LValue9).getChannelSelector());
            Expr expr14 = ((AssignmentStatement) blockElem8).getE();
            checkUnaryExpr(expr14, Kind.RES_width);
            Expr expr15 = ((UnaryExpr) expr14).getExpr();
            checkNumLitExpr(expr15, 4);
            BlockElem blockElem16 = ((List<BlockElem>) blockElemList3).get(2);
            assertThat("", blockElem16, instanceOf(AssignmentStatement.class));
            LValue LValue17 = ((AssignmentStatement) blockElem16).getlValue();
            assertThat("", LValue17, instanceOf(LValue.class));
            String name18 = ((LValue) LValue17).getName();
            assertEquals("b", name18);
            PixelSelector pixel19 = ((LValue) LValue17).getPixelSelector();
            Expr x20 = ((PixelSelector) pixel19).xExpr();
            checkIdentExpr(x20, "x");
            Expr y21 = ((PixelSelector) pixel19).yExpr();
            checkIdentExpr(y21, "y");
            ChannelSelector channel22 = ((LValue) LValue17).getChannelSelector();
            checkChannelSelector(channel22, Kind.RES_blue);
            Expr expr23 = ((AssignmentStatement) blockElem16).getE();
            checkUnaryExpr(expr23, Kind.RES_height);
            Expr expr24 = ((UnaryExpr) expr23).getExpr();
            checkNumLitExpr(expr24, 5);
            BlockElem blockElem25 = ((List<BlockElem>) blockElemList3).get(3);
            assertThat("", blockElem25, instanceOf(AssignmentStatement.class));
            LValue LValue26 = ((AssignmentStatement) blockElem25).getlValue();
            assertThat("", LValue26, instanceOf(LValue.class));
            String name27 = ((LValue) LValue26).getName();
            assertEquals("b", name27);
            assertNull(LValue26.getPixelSelector());
            ChannelSelector channel28 = ((LValue) LValue26).getChannelSelector();
            checkChannelSelector(channel28, Kind.RES_red);
            Expr expr29 = ((AssignmentStatement) blockElem25).getE();
            checkUnaryExpr(expr29, Kind.MINUS);
            Expr expr30 = ((UnaryExpr) expr29).getExpr();
            checkNumLitExpr(expr30, 5);
        });
    }

    @Test
    void test6() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(999999), () -> {
            String input = """
                    image do_test()<:
                    do TRUE -> <: write 2; :>
                     []  "string" -> <: a = "string"; :>
                    od;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "image", "do_test");
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
            checkBooleanLitExpr(guard7, "TRUE");
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
            checkStringLitExpr(guard13, "string");
            Block block14 = ((GuardedBlock) guardedBlock12).getBlock();
            List<BlockElem> blockElemList15 = block14.getElems();
            assertEquals(1, blockElemList15.size());
            BlockElem blockElem16 = ((List<BlockElem>) blockElemList15).get(0);
            assertThat("", blockElem16, instanceOf(AssignmentStatement.class));
            LValue LValue17 = ((AssignmentStatement) blockElem16).getlValue();
            assertThat("", LValue17, instanceOf(LValue.class));
            String name18 = ((LValue) LValue17).getName();
            assertEquals("a", name18);
            assertNull(LValue17.getPixelSelector());
            assertNull(((LValue) LValue17).getChannelSelector());
            Expr expr19 = ((AssignmentStatement) blockElem16).getE();
            checkStringLitExpr(expr19, "string");
        });
    }

    @Test
    void test7() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    image if_test()<:
                    if FALSE -> <: write CYAN; :>
                    []   LIGHT_GRAY -> <: abc_12 = 6; :>
                    fi;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "image", "if_test");
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
            checkBooleanLitExpr(guard7, "FALSE");
            Block block8 = ((GuardedBlock) guardedBlock6).getBlock();
            List<BlockElem> blockElemList9 = block8.getElems();
            assertEquals(1, blockElemList9.size());
            BlockElem blockElem10 = ((List<BlockElem>) blockElemList9).get(0);
            assertThat("", blockElem10, instanceOf(WriteStatement.class));
            Expr writeStatementExpr11 = ((WriteStatement) blockElem10).getExpr();
            checkConstExpr(writeStatementExpr11, "CYAN");
            GuardedBlock guardedBlock12 = ((List<GuardedBlock>) guardedBlocks5).get(1);
            assertThat("", guardedBlock12, instanceOf(GuardedBlock.class));
            Expr guard13 = ((GuardedBlock) guardedBlock12).getGuard();
            checkConstExpr(guard13, "LIGHT_GRAY");
            Block block14 = ((GuardedBlock) guardedBlock12).getBlock();
            List<BlockElem> blockElemList15 = block14.getElems();
            assertEquals(1, blockElemList15.size());
            BlockElem blockElem16 = ((List<BlockElem>) blockElemList15).get(0);
            assertThat("", blockElem16, instanceOf(AssignmentStatement.class));
            LValue LValue17 = ((AssignmentStatement) blockElem16).getlValue();
            assertThat("", LValue17, instanceOf(LValue.class));
            String name18 = ((LValue) LValue17).getName();
            assertEquals("abc_12", name18);
            assertNull(LValue17.getPixelSelector());
            assertNull(((LValue) LValue17).getChannelSelector());
            Expr expr19 = ((AssignmentStatement) blockElem16).getE();
            checkNumLitExpr(expr19, 6);
        });
    }

    @Test
    void test8() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void p() <:
                       ^(2+3);
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
            checkBinaryExpr(returnValueExpr5, Kind.PLUS);
            Expr leftExpr6 = ((BinaryExpr) returnValueExpr5).getLeftExpr();
            checkNumLitExpr(leftExpr6, 2);
            Expr rightExpr7 = ((BinaryExpr) returnValueExpr5).getRightExpr();
            checkNumLitExpr(rightExpr7, 3);
        });
    }

    @Test
    void test9() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void emtpy_block() <:
                       <::>;
                       :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "emtpy_block");
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
        });
    }

    @Test
    void test10() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void p() <:
                    int r;
                    a=Z;
                    int b;
                    <: a[x,y]:blue = b; :>;
                    ^[last_var, 2, 3];
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
            checkNameDef(nameDef11, "int", "b");
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
            checkChannelSelector(channel21, Kind.RES_blue);
            Expr expr22 = ((AssignmentStatement) blockElem15).getE();
            checkIdentExpr(expr22, "b");
            BlockElem blockElem23 = ((List<BlockElem>) blockElemList3).get(4);
            assertThat("", blockElem23, instanceOf(ReturnStatement.class));
            Expr returnValueExpr24 = ((ReturnStatement) blockElem23).getE();
            Expr red25 = ((ExpandedPixelExpr) returnValueExpr24).getRed();
            checkIdentExpr(red25, "last_var");
            Expr green26 = ((ExpandedPixelExpr) returnValueExpr24).getGreen();
            checkNumLitExpr(green26, 2);
            Expr blue27 = ((ExpandedPixelExpr) returnValueExpr24).getBlue();
            checkNumLitExpr(blue27, 3);
        });
    }

    @Test
    void test11() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    int f()
                    <:
                    boolean b = FALSE;
                    string s = "string";
                    image i = [1,2,3];
                    ^i;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "int", "f");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(4, blockElemList3.size());
            BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
            checkDec(blockElem4);
            NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
            checkNameDef(nameDef5, "boolean", "b");
            Expr expr6 = ((Declaration) blockElem4).getInitializer();
            checkBooleanLitExpr(expr6, "FALSE");
            BlockElem blockElem7 = ((List<BlockElem>) blockElemList3).get(1);
            checkDec(blockElem7);
            NameDef nameDef8 = ((Declaration) blockElem7).getNameDef();
            checkNameDef(nameDef8, "string", "s");
            Expr expr9 = ((Declaration) blockElem7).getInitializer();
            checkStringLitExpr(expr9, "string");
            BlockElem blockElem10 = ((List<BlockElem>) blockElemList3).get(2);
            checkDec(blockElem10);
            NameDef nameDef11 = ((Declaration) blockElem10).getNameDef();
            checkNameDef(nameDef11, "image", "i");
            Expr expr12 = ((Declaration) blockElem10).getInitializer();
            Expr red13 = ((ExpandedPixelExpr) expr12).getRed();
            checkNumLitExpr(red13, 1);
            Expr green14 = ((ExpandedPixelExpr) expr12).getGreen();
            checkNumLitExpr(green14, 2);
            Expr blue15 = ((ExpandedPixelExpr) expr12).getBlue();
            checkNumLitExpr(blue15, 3);
            BlockElem blockElem16 = ((List<BlockElem>) blockElemList3).get(3);
            assertThat("", blockElem16, instanceOf(ReturnStatement.class));
            Expr returnValueExpr17 = ((ReturnStatement) blockElem16).getE();
            checkIdentExpr(returnValueExpr17, "i");
        });
    }

    @Test
    void test12() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void f(int a, int b, int c) <:
                    a = b + c;
                    ^ a + c;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "f");
            List<NameDef> params1 = program0.getParams();
            assertEquals(3, params1.size());
            NameDef paramNameDef2 = ((List<NameDef>) params1).get(0);
            checkNameDef(paramNameDef2, "int", "a");
            NameDef paramNameDef3 = ((List<NameDef>) params1).get(1);
            checkNameDef(paramNameDef3, "int", "b");
            NameDef paramNameDef4 = ((List<NameDef>) params1).get(2);
            checkNameDef(paramNameDef4, "int", "c");
            Block programBlock5 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList6 = programBlock5.getElems();
            assertEquals(2, blockElemList6.size());
            BlockElem blockElem7 = ((List<BlockElem>) blockElemList6).get(0);
            assertThat("", blockElem7, instanceOf(AssignmentStatement.class));
            LValue LValue8 = ((AssignmentStatement) blockElem7).getlValue();
            assertThat("", LValue8, instanceOf(LValue.class));
            String name9 = ((LValue) LValue8).getName();
            assertEquals("a", name9);
            assertNull(LValue8.getPixelSelector());
            assertNull(((LValue) LValue8).getChannelSelector());
            Expr expr10 = ((AssignmentStatement) blockElem7).getE();
            checkBinaryExpr(expr10, Kind.PLUS);
            Expr leftExpr11 = ((BinaryExpr) expr10).getLeftExpr();
            checkIdentExpr(leftExpr11, "b");
            Expr rightExpr12 = ((BinaryExpr) expr10).getRightExpr();
            checkIdentExpr(rightExpr12, "c");
            BlockElem blockElem13 = ((List<BlockElem>) blockElemList6).get(1);
            assertThat("", blockElem13, instanceOf(ReturnStatement.class));
            Expr returnValueExpr14 = ((ReturnStatement) blockElem13).getE();
            checkBinaryExpr(returnValueExpr14, Kind.PLUS);
            Expr leftExpr15 = ((BinaryExpr) returnValueExpr14).getLeftExpr();
            checkIdentExpr(leftExpr15, "a");
            Expr rightExpr16 = ((BinaryExpr) returnValueExpr14).getRightExpr();
            checkIdentExpr(rightExpr16, "c");
        });
    }

    @Test
    void test13() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(9999999), () -> {
            String input = """
                    void single_do(boolean a) <:
                    do a -> <: write a; :> od;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "single_do");
            List<NameDef> params1 = program0.getParams();
            assertEquals(1, params1.size());
            NameDef paramNameDef2 = ((List<NameDef>) params1).get(0);
            checkNameDef(paramNameDef2, "boolean", "a");
            Block programBlock3 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList4 = programBlock3.getElems();
            assertEquals(1, blockElemList4.size());
            BlockElem blockElem5 = ((List<BlockElem>) blockElemList4).get(0);
            assertThat("", blockElem5, instanceOf(DoStatement.class));
            List<GuardedBlock> guardedBlocks6 = ((DoStatement) blockElem5).getGuardedBlocks();
            assertEquals(1, guardedBlocks6.size());
            GuardedBlock guardedBlock7 = ((List<GuardedBlock>) guardedBlocks6).get(0);
            assertThat("", guardedBlock7, instanceOf(GuardedBlock.class));
            Expr guard8 = ((GuardedBlock) guardedBlock7).getGuard();
            checkIdentExpr(guard8, "a");
            Block block9 = ((GuardedBlock) guardedBlock7).getBlock();
            List<BlockElem> blockElemList10 = block9.getElems();
            assertEquals(1, blockElemList10.size());
            BlockElem blockElem11 = ((List<BlockElem>) blockElemList10).get(0);
            assertThat("", blockElem11, instanceOf(WriteStatement.class));
            Expr writeStatementExpr12 = ((WriteStatement) blockElem11).getExpr();
            checkIdentExpr(writeStatementExpr12, "a");
        });
    }

    @Test
    void test14() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void single_if(boolean a) <:
                    if a -> <: write a; :> fi;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "single_if");
            List<NameDef> params1 = program0.getParams();
            assertEquals(1, params1.size());
            NameDef paramNameDef2 = ((List<NameDef>) params1).get(0);
            checkNameDef(paramNameDef2, "boolean", "a");
            Block programBlock3 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList4 = programBlock3.getElems();
            assertEquals(1, blockElemList4.size());
            BlockElem blockElem5 = ((List<BlockElem>) blockElemList4).get(0);
            assertThat("", blockElem5, instanceOf(IfStatement.class));
            List<GuardedBlock> guardedBlocks6 = ((IfStatement) blockElem5).getGuardedBlocks();
            assertEquals(1, guardedBlocks6.size());
            GuardedBlock guardedBlock7 = ((List<GuardedBlock>) guardedBlocks6).get(0);
            assertThat("", guardedBlock7, instanceOf(GuardedBlock.class));
            Expr guard8 = ((GuardedBlock) guardedBlock7).getGuard();
            checkIdentExpr(guard8, "a");
            Block block9 = ((GuardedBlock) guardedBlock7).getBlock();
            List<BlockElem> blockElemList10 = block9.getElems();
            assertEquals(1, blockElemList10.size());
            BlockElem blockElem11 = ((List<BlockElem>) blockElemList10).get(0);
            assertThat("", blockElem11, instanceOf(WriteStatement.class));
            Expr writeStatementExpr12 = ((WriteStatement) blockElem11).getExpr();
            checkIdentExpr(writeStatementExpr12, "a");
        });
    }

    @Test
    void test15() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void a()<::>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "a");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(0, blockElemList3.size());
        });
    }

    @Test
    void test16() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    boolean _() <::>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "boolean", "_");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(0, blockElemList3.size());
        });
    }

    @Test
    void test17() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    string a() <::>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "string", "a");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(0, blockElemList3.size());
        });
    }

    @Test
    void test18() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    pixel a() <::>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "pixel", "a");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(0, blockElemList3.size());
        });
    }

    @Test
    void test19() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    image a() <::>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "image", "a");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(0, blockElemList3.size());
        });
    }

    @Test
    void test20() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void dims(string[2+2-!"string", TRUE] a) <::>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "dims");
            List<NameDef> params1 = program0.getParams();
            assertEquals(1, params1.size());
            NameDef paramNameDef2 = ((List<NameDef>) params1).get(0);
            checkNameDefDim(paramNameDef2, "string", "a");
            Dimension dimension3 = ((NameDef) paramNameDef2).getDimension();
            assertThat("", dimension3, instanceOf(Dimension.class));
            Expr width4 = ((Dimension) dimension3).getWidth();
            checkBinaryExpr(width4, Kind.MINUS);
            Expr leftExpr5 = ((BinaryExpr) width4).getLeftExpr();
            checkBinaryExpr(leftExpr5, Kind.PLUS);
            Expr leftExpr6 = ((BinaryExpr) leftExpr5).getLeftExpr();
            checkNumLitExpr(leftExpr6, 2);
            Expr rightExpr7 = ((BinaryExpr) leftExpr5).getRightExpr();
            checkNumLitExpr(rightExpr7, 2);
            Expr rightExpr8 = ((BinaryExpr) width4).getRightExpr();
            checkUnaryExpr(rightExpr8, Kind.BANG);
            Expr expr9 = ((UnaryExpr) rightExpr8).getExpr();
            checkStringLitExpr(expr9, "string");
            Expr height10 = ((Dimension) dimension3).getHeight();
            checkBooleanLitExpr(height10, "TRUE");
            Block programBlock11 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList12 = programBlock11.getElems();
            assertEquals(0, blockElemList12.size());
        });
    }

    @Test
    void test21() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void complex(int i) <:
                    write 2 + "string" - -5 / width "string" >= [width 5 + 6, "hello", 9] - -!width height a + (a - b);
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "complex");
            List<NameDef> params1 = program0.getParams();
            assertEquals(1, params1.size());
            NameDef paramNameDef2 = ((List<NameDef>) params1).get(0);
            checkNameDef(paramNameDef2, "int", "i");
            Block programBlock3 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList4 = programBlock3.getElems();
            assertEquals(1, blockElemList4.size());
            BlockElem blockElem5 = ((List<BlockElem>) blockElemList4).get(0);
            assertThat("", blockElem5, instanceOf(WriteStatement.class));
            Expr writeStatementExpr6 = ((WriteStatement) blockElem5).getExpr();
            checkBinaryExpr(writeStatementExpr6, Kind.GE);
            Expr leftExpr7 = ((BinaryExpr) writeStatementExpr6).getLeftExpr();
            checkBinaryExpr(leftExpr7, Kind.MINUS);
            Expr leftExpr8 = ((BinaryExpr) leftExpr7).getLeftExpr();
            checkBinaryExpr(leftExpr8, Kind.PLUS);
            Expr leftExpr9 = ((BinaryExpr) leftExpr8).getLeftExpr();
            checkNumLitExpr(leftExpr9, 2);
            Expr rightExpr10 = ((BinaryExpr) leftExpr8).getRightExpr();
            checkStringLitExpr(rightExpr10, "string");
            Expr rightExpr11 = ((BinaryExpr) leftExpr7).getRightExpr();
            checkBinaryExpr(rightExpr11, Kind.DIV);
            Expr leftExpr12 = ((BinaryExpr) rightExpr11).getLeftExpr();
            checkUnaryExpr(leftExpr12, Kind.MINUS);
            Expr expr13 = ((UnaryExpr) leftExpr12).getExpr();
            checkNumLitExpr(expr13, 5);
            Expr rightExpr14 = ((BinaryExpr) rightExpr11).getRightExpr();
            checkUnaryExpr(rightExpr14, Kind.RES_width);
            Expr expr15 = ((UnaryExpr) rightExpr14).getExpr();
            checkStringLitExpr(expr15, "string");
            Expr rightExpr16 = ((BinaryExpr) writeStatementExpr6).getRightExpr();
            checkBinaryExpr(rightExpr16, Kind.PLUS);
            Expr leftExpr17 = ((BinaryExpr) rightExpr16).getLeftExpr();
            checkBinaryExpr(leftExpr17, Kind.MINUS);
            Expr leftExpr18 = ((BinaryExpr) leftExpr17).getLeftExpr();
            Expr red19 = ((ExpandedPixelExpr) leftExpr18).getRed();
            checkBinaryExpr(red19, Kind.PLUS);
            Expr leftExpr20 = ((BinaryExpr) red19).getLeftExpr();
            checkUnaryExpr(leftExpr20, Kind.RES_width);
            Expr expr21 = ((UnaryExpr) leftExpr20).getExpr();
            checkNumLitExpr(expr21, 5);
            Expr rightExpr22 = ((BinaryExpr) red19).getRightExpr();
            checkNumLitExpr(rightExpr22, 6);
            Expr green23 = ((ExpandedPixelExpr) leftExpr18).getGreen();
            checkStringLitExpr(green23, "hello");
            Expr blue24 = ((ExpandedPixelExpr) leftExpr18).getBlue();
            checkNumLitExpr(blue24, 9);
            Expr rightExpr25 = ((BinaryExpr) leftExpr17).getRightExpr();
            checkUnaryExpr(rightExpr25, Kind.MINUS);
            Expr expr26 = ((UnaryExpr) rightExpr25).getExpr();
            checkUnaryExpr(expr26, Kind.BANG);
            Expr expr27 = ((UnaryExpr) expr26).getExpr();
            checkUnaryExpr(expr27, Kind.RES_width);
            Expr expr28 = ((UnaryExpr) expr27).getExpr();
            checkUnaryExpr(expr28, Kind.RES_height);
            Expr expr29 = ((UnaryExpr) expr28).getExpr();
            checkIdentExpr(expr29, "a");
            Expr rightExpr30 = ((BinaryExpr) rightExpr16).getRightExpr();
            checkBinaryExpr(rightExpr30, Kind.MINUS);
            Expr leftExpr31 = ((BinaryExpr) rightExpr30).getLeftExpr();
            checkIdentExpr(leftExpr31, "a");
            Expr rightExpr32 = ((BinaryExpr) rightExpr30).getRightExpr();
            checkIdentExpr(rightExpr32, "b");
        });
    }

    @Test
    void test22() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void test() <:
                    int a = 2[1,1]:green;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "test");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(1, blockElemList3.size());
            BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
            checkDec(blockElem4);
            NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
            checkNameDef(nameDef5, "int", "a");
            Expr expr6 = ((Declaration) blockElem4).getInitializer();
            checkPostfixExpr(expr6, true, true);
            Expr expr7 = ((PostfixExpr) expr6).primary();
            checkNumLitExpr(expr7, 2);
            PixelSelector pixel8 = ((PostfixExpr) expr6).pixel();
            Expr x9 = ((PixelSelector) pixel8).xExpr();
            checkNumLitExpr(x9, 1);
            Expr y10 = ((PixelSelector) pixel8).yExpr();
            checkNumLitExpr(y10, 1);
            ChannelSelector channel11 = ((PostfixExpr) expr6).channel();
            checkChannelSelector(channel11, Kind.RES_green);
        });
    }

    @Test
    void test23() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void a() <:
                    int a = ? TRUE -> b , c;
                    ^a;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "a");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(2, blockElemList3.size());
            BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
            checkDec(blockElem4);
            NameDef nameDef5 = ((Declaration) blockElem4).getNameDef();
            checkNameDef(nameDef5, "int", "a");
            Expr expr6 = ((Declaration) blockElem4).getInitializer();
            checkConditionalExpr(expr6);
            Expr guard7 = ((ConditionalExpr) expr6).getGuardExpr();
            checkBooleanLitExpr(guard7, "TRUE");
            Expr trueCase8 = ((ConditionalExpr) expr6).getTrueExpr();
            checkIdentExpr(trueCase8, "b");
            Expr falseCase9 = ((ConditionalExpr) expr6).getFalseExpr();
            checkIdentExpr(falseCase9, "c");
            BlockElem blockElem10 = ((List<BlockElem>) blockElemList3).get(1);
            assertThat("", blockElem10, instanceOf(ReturnStatement.class));
            Expr returnValueExpr11 = ((ReturnStatement) blockElem10).getE();
            checkIdentExpr(returnValueExpr11, "a");
        });
    }

    @Test
    void test24() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
            String input = """
                    void a() <:
                    write 2 ** 2;
                    :>
                    """;
            AST ast = getAST(input);
            Program program0 = checkProgram(ast, "void", "a");
            List<NameDef> params1 = program0.getParams();
            assertEquals(0, params1.size());
            Block programBlock2 = ((Program) ast).getBlock();
            List<BlockElem> blockElemList3 = programBlock2.getElems();
            assertEquals(1, blockElemList3.size());
            BlockElem blockElem4 = ((List<BlockElem>) blockElemList3).get(0);
            assertThat("", blockElem4, instanceOf(WriteStatement.class));
            Expr writeStatementExpr5 = ((WriteStatement) blockElem4).getExpr();
            checkBinaryExpr(writeStatementExpr5, Kind.EXP);
            Expr leftExpr6 = ((BinaryExpr) writeStatementExpr5).getLeftExpr();
            checkNumLitExpr(leftExpr6, 2);
            Expr rightExpr7 = ((BinaryExpr) writeStatementExpr5).getRightExpr();
            checkNumLitExpr(rightExpr7, 2);
        });
    }

    @Test
    void test25() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
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
        );
    }

    @Test
    void test26() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            boolean prog()<:
                            x = ;
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test27() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
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
        );
    }

    @Test
    void test28() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test29() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s(
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test30() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test31() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s(int a = 2) <: :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test32() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            x =
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test33() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s(int a; int b) <: :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test34() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            write;
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test35() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            do a -><:;
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test36() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            do a -> <: b; :> [] od;
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test37() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            ^;
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test38() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            int a = 2:GRAY;
                            :>
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

    @Test
    void test39() throws PLCCompilerException {
        assertTimeoutPreemptively(Duration.ofMillis(TIMEOUT_MILLIS), () -> {
                    String input = """
                            int s() <:
                            int a = a +
                            """;
                    assertThrows(SyntaxException.class, () -> {
                        @SuppressWarnings("unused")
                        AST ast = getAST(input);
                    });
                }
        );
    }

}
