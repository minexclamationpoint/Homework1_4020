package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;

import static edu.ufl.cise.cop4020fa23.Kind.*;
import static edu.ufl.cise.cop4020fa23.ast.Type.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.logging.Logger;

/*To work around a mismatch between the scoping rules of our language and Java’s scoping rules,
implement a way to rename variables in the generated Java code so that every variable has a
unique name. It is up to you to determine when and how to do this. (See the lecture on 10/31)
 */
/*Implement an ASTVisitor (CodeGenVisitor) to generate Java code. The visitProgram method of
this visitor should accept a package name as an argument and return a String containing a Java
program implementing the semantics of the language. The package name may be null or an
empty String. In either such case, the generated program should be in the default package (i.e.
has no package declaration).
 */
public class CodeGenVisitor implements ASTVisitor {

    //Types image and pixel are implemented in Assignment 5
    private Type currentType;
    private SymbolTable st = new SymbolTable();
    private StringBuilder sb;
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        //_LValue_ = _Expr_
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        /*
        If ExprleftExpr.type is string and op is EQ
        _ ExprleftExpr_ .equals( _ ExprrigthExpr _)
        If op is EXP
        ((int)Math.round(Math.pow( _ ExprleftExpr _ , _ExprrigthExpr_ ))
        Otherwise
        (_ ExprleftExpr _ _op_ _ ExprrigthExpr _)
        */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        //{ _BlockElem*_ }
        //BlockElem ::= Declaration | Statement
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        //_Block_
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        //Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        /*
        ( _ ExprGuardExpr_ ? _ ExprTrueExpr _
        : _ ExprFalseExpr _ )
         */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        /*
        either _NameDef_ or _NameDef_ = _Expr_
         */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        //Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        //Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        //Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        //_IdentExpr_.getNameDef().getJavaName()
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        //Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        //_IdentExpr_.getNameDef().getJavaName()
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {

        /*
        Dimension is implemented in Assignment 5
        _Type_ _name_
        Where _name_ is the Java name of the IDENT
        */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        //_NumLitExpr_.getText
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        //Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        //Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        /*public class _IDENT_ {
            public static _Type_ apply(
                _NameDef*_
            ) _Block
            }
        Note: parameters from _NameDef*_ are separated by commas
         */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        //return _Expr_
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        //_StringLitExpr_.getText
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        /*
        ( _op_ _Expr_ )
        Note: you do not need to handle width and height
        in this assignment
         */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        /*
        ConsoleIO.write( _Expr_ )
        Note: you will need to import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO
         */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        //true or false
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        //Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }
}
