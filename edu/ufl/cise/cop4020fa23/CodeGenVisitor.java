package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.exceptions.*;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;

import static edu.ufl.cise.cop4020fa23.Kind.*;
import static edu.ufl.cise.cop4020fa23.ast.Type.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.ListIterator;
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

    // Types image and pixel are implemented in Assignment 5
    private Type currentType;
    private SymbolTable st = new SymbolTable();
    private StringBuilder sb;

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg)
            throws PLCCompilerException {
        // I have to wrap in try catch
        try {
            // _LValue_ = _Expr_
            StringBuilder assignmentStringBuilder = new StringBuilder();

            LValue lvalue = assignmentStatement.getlValue();
            if (lvalue == null) {
                throw new CodeGenException("LValue in assignment statement is null");
            }
            String variableName = lvalue.getName();
            if (variableName == null || variableName.isEmpty()) {
                throw new CodeGenException("Variable name in LValue is null or empty.");
            }
            assignmentStringBuilder.append(variableName);

            assignmentStringBuilder.append(" = ");

            Expr expr = assignmentStatement.getE();
            if (expr == null) {
                throw new CodeGenException("Expression in assignment statement is null");
            }
            StringBuilder exprCode = determineExpr(expr, arg);
            assignmentStringBuilder.append(exprCode);

            assignmentStringBuilder.append(";\n");

            return assignmentStringBuilder;
        } catch (Exception e) {
            throw new CodeGenException("Well then we shouldn't be here" + e.getMessage());
        }
    }

    @Override
    public StringBuilder visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        /*
         * If ExprleftExpr.type is string and op is EQ
         * _ ExprleftExpr_ .equals( _ ExprrigthExpr _)
         * If op is EXP
         * ((int)Math.round(Math.pow( _ ExprleftExpr _ , _ExprrigthExpr_ ))
         * Otherwise
         * (_ ExprleftExpr _ _op_ _ ExprrigthExpr _)
         */

        StringBuilder leftSb = determineExpr(binaryExpr.getLeftExpr(), arg);
        StringBuilder rightSb = determineExpr(binaryExpr.getRightExpr(), arg);

        Type leftType = binaryExpr.getLeftExpr().getType();
        Type rightType = binaryExpr.getRightExpr().getType();
        Kind opKind = binaryExpr.getOpKind();

        StringBuilder sb = new StringBuilder();

        if (leftType == Type.STRING && rightType == Type.STRING && opKind == Kind.EQ) {
            sb.append(leftSb).append(".equals(").append(rightSb).append(")");
        } else if (leftType == Type.INT && rightType == Type.INT && opKind == Kind.EXP) {
            sb.append("(int)Math.round(Math.pow(").append(leftSb).append(", ").append(rightSb).append("))");
        } else if (leftType == Type.INT && rightType == Type.INT) {
            // Arithmetic operations are only supported between integers in this example
            String op = convertOpKind(opKind);
            sb.append("(").append(leftSb).append(" ").append(op).append(" ").append(rightSb).append(")");
        } else {
            throw new CodeGenException("Unsupported operation or type mismatch for operation " + opKind);
        }

        return sb;
    }

    private String convertOpKind(Kind opKind) throws CodeGenException {
        switch (opKind) {
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case TIMES:
                return "*";
            case DIV:
                return "/";
            case MOD:
                return "%";
            case EQ:
                return "==";
            case LE:
                return "<=";
            case GE:
                return ">=";
            case LT:
                return "<";
            case GT:
                return ">";
            case AND:
                return "&&";
            case OR:
                return "||";
            case BITAND:
                return "&";
            case BITOR:
                return "|";
            case BANG:
                return "!";
            // should be all of them

            default:
                throw new CodeGenException("Unsupported operation or type mismatch for operation " + opKind);// WHY WPNT
                                                                                                             // THIS
                                                                                                             // QWOERK
        }
    }

    @Override
    public StringBuilder visitBlock(Block block, Object arg) throws PLCCompilerException {
        StringBuilder blockCode = new StringBuilder("{\n");
        for (Block.BlockElem elem : block.getElems()) {
            try {
                if (elem instanceof Declaration) {
                    blockCode.append(visitDeclaration((Declaration) elem, arg));
                } else if (elem instanceof Statement) {
                    blockCode.append(visitBlockStatement((StatementBlock) elem, arg));
                } else {
                    // Here, we throw a CodeGenException for an unsupported BlockElem type.
                    throw new CodeGenException(elem.firstToken() + "Unsupported BlockElem type");
                }
                blockCode.append(";\n");
            } catch (CodeGenException e) {
                throw e;
            }
        }
        blockCode.append("}\n");
        return blockCode;
    }

    @Override
    public StringBuilder visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        // _Block_
        return visitBlock(statementBlock.getBlock(), arg);
    }

    @Override
    public StringBuilder visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        /*
         * ( _ ExprGuardExpr_ ? _ ExprTrueExpr _
         * : _ ExprFalseExpr _ )
         */
        StringBuilder sb = new StringBuilder();

        StringBuilder guardExpr = determineExpr(conditionalExpr.getGuardExpr(), arg);
        if (guardExpr == null) {
            throw new CodeGenException("Guard expression is null in conditional expression");
        }

        StringBuilder trueExpr = determineExpr(conditionalExpr.getTrueExpr(), arg);
        if (trueExpr == null) {
            throw new CodeGenException("True expression is null in conditional expression");
        }

        StringBuilder falseExpr = determineExpr(conditionalExpr.getFalseExpr(), arg);
        if (falseExpr == null) {
            throw new CodeGenException("False expression is null in conditional expression");
        }
        // probably right?
        sb.append("(")
                .append(guardExpr)
                .append(" ? ")
                .append(trueExpr)
                .append(" : ")
                .append(falseExpr)
                .append(")");

        return sb;
    }

    @Override
    public StringBuilder visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        /*
         * either _NameDef_ or _NameDef_ = _Expr_
         */
        StringBuilder sb = new StringBuilder();
        NameDef nameDef = declaration.getNameDef();
        Expr initializer = declaration.getInitializer();

        if (nameDef.getType() == null || nameDef.getIdentToken() == null) {
            throw new CodeGenException("Invalid type or identifier in declaration.");
        }

        sb.append(nameDef.getType().toString()).append(" ").append(nameDef.getIdentToken());

        if (initializer != null) {
            Object result = initializer.visit(this, arg); 

            StringBuilder initCode = (StringBuilder) result;
            if (initCode.length() == 0) {
                throw new CodeGenException("Failed to generate code for the initializer expression.");
            }
            sb.append(" = ").append(initCode);
        }

        sb.append(";");
        return sb;
    }

    @Override
    public StringBuilder visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg)
            throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        // _IdentExpr_.getNameDef().getJavaName()
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        // _IdentExpr_.getNameDef().getJavaName()
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {

        /*
         * Dimension is implemented in Assignment 5
         * _Type_ _name_
         * Where _name_ is the Java name of the IDENT
         */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        // _NumLitExpr_.getText
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitProgram(Program program, Object arg) throws PLCCompilerException {
        /*
         * Should accept a package name as an argument and return a String containing a
         * java program implementing the semantics of the language. The package name may
         * be null or an empty string.
         * If so, the generated program should be in the default package.
         * public class _IDENT_ {
         * public static _Type_ apply(
         * _NameDef*_
         * ) _Block
         * }
         * Note: parameters from _NameDef*_ are separated by commas
         */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        // return _Expr_
        StringBuilder subString = new StringBuilder("return ");
        subString.append(determineExpr(returnStatement.getE(), arg));
        return subString;
    }

    @Override
    public StringBuilder visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        // _StringLitExpr_.getText
        return new StringBuilder(stringLitExpr.getText());
    }

    @Override
    public StringBuilder visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        /*
         * ( _op_ _Expr_ )
         * Note: you do not need to handle width and height
         * in this assignment
         */
        StringBuilder subString = new StringBuilder("(");
        subString.append(unaryExpr.getOp().toString()).append(" ");
        subString.append(unaryExpr.getExpr()).append(")");
        return subString;
    }

    @Override
    public StringBuilder visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        /*
         * ConsoleIO.write( _Expr_ )
         * Note: you will need to import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO
         */
        Expr subExpr = writeStatement.getExpr();
        StringBuilder subString = new StringBuilder("ConsoleIO.write(");
        subString.append(determineExpr(subExpr, arg));
        subString.append(")");
        return subString;
    }

    // Helper method for the various visitors that have to go through the different
    // versions of Expr
    private StringBuilder determineExpr(Expr subExpr, Object arg) throws PLCCompilerException {
        StringBuilder subString = new StringBuilder();
        switch (subExpr.getClass().getName()) {
            case "ConditionalExpr" -> {
                subString.append(visitConditionalExpr((ConditionalExpr) subExpr, arg));
            }
            case "BinaryExpr" -> {
                subString.append(visitBinaryExpr((BinaryExpr) subExpr, arg));
            }
            case "UnaryExpr" -> {
                subString.append(visitUnaryExpr((UnaryExpr) subExpr, arg));
            }
            case "StringLitExpr" -> {
                subString.append(visitStringLitExpr((StringLitExpr) subExpr, arg));
            }
            case "NumLitExpr" -> {
                subString.append(visitNumLitExpr((NumLitExpr) subExpr, arg));
            }
            case "IdentExpr" -> {
                subString.append(visitIdentExpr((IdentExpr) subExpr, arg));
            }
            case "BooleanLitExpr" -> {
                subString.append(visitBooleanLitExpr((BooleanLitExpr) subExpr, arg));
            }
            default -> throw new CodeGenException("Unexpected subexpression type");
        }
        ;
        return subString;
    }

    @Override
    public StringBuilder visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        // true or false
        System.out.println(booleanLitExpr.getText());
        StringBuilder subString = new StringBuilder();
        return switch (booleanLitExpr.getText()) {
            case "FALSE" -> subString.append("false");
            case "TRUE" -> subString.append("true");
            default -> throw new CodeGenException("Unexpected type in BooleanLitExpr");
        };
    }

    @Override
    public StringBuilder visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }
}
