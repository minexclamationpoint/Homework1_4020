package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.exceptions.*;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;

import static edu.ufl.cise.cop4020fa23.Kind.*;
import static edu.ufl.cise.cop4020fa23.ast.Type.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;

import java.util.ListIterator;

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

    private HashSet<String> importSet = new HashSet<>();
    private HashMap<String, Integer> nameCounter = new HashMap<>();



    // TODO: implement javanames
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg)
            throws PLCCompilerException {
        try {
            // _LValue_ = _Expr_
            StringBuilder sb = new StringBuilder();
            if (assignmentStatement.getlValue() == null) {
                throw new CodeGenException("LValue in assignment statement is null");
            }
            if (assignmentStatement.getlValue().getName() == null || assignmentStatement.getlValue().getName().isEmpty()) {
                throw new CodeGenException("Variable name in LValue is null or empty.");
            }
            if(assignmentStatement.getE() == null){
                throw new CodeGenException("Expression in assignment statement is null");
            }
            sb.append(visitLValue(assignmentStatement.getlValue(), arg)).append(" = ");
            sb.append(determineExpr(assignmentStatement.getE(), arg));
            return sb;
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
        Kind opKind = binaryExpr.getOpKind();

        StringBuilder sb = new StringBuilder();

        if (leftType == Type.STRING && opKind == Kind.EQ) {
            sb.append(leftSb).append(".equals(").append(rightSb).append(")");
        }

        else if (opKind == Kind.EXP) {
            // is this bs? maybeeee
            sb.append("(int)Math.round(Math.pow(").append(leftSb).append(", ").append(rightSb).append("))");
        } else {
            String op = convertOpKind(opKind);
            sb.append("(").append(leftSb).append(" ").append(op).append(" ").append(rightSb).append(")");
        }

        return sb;
        //^^^ unsure if this newline is necessary
    }

    private String convertOpKind(Kind opKind) throws PLCCompilerException {
        return switch (opKind) {
            case PLUS -> "+";
            case MINUS -> "-";
            case TIMES -> "*";
            case DIV -> "/";
            case MOD -> "%";
            case EQ -> "==";
            case LE -> "<=";
            case GE -> ">=";
            case LT -> "<";
            case GT -> ">";
            case AND -> "&&";
            case OR -> "||";
            case BITAND -> "&";
            case BITOR -> "|";
            case BANG -> "!";
            // should be all of them

            default -> throw new CodeGenException("Operation " + opKind + " not supported.");
        };
    }

    @Override
    public StringBuilder visitBlock(Block block, Object arg) throws PLCCompilerException {
        // { _BlockElem*_ }
        // BlockElem ::= Declaration | Statement
        //Extended visitBlock into two subexpressions: visitBlockElem and determineStatement
        StringBuilder blockCode = new StringBuilder("{\n");
        for (Block.BlockElem elem : block.getElems()) {
            try {
                blockCode.append(visitBlockElem(elem, arg));
            } catch (CodeGenException e) {
                throw e;
            }
        }
        blockCode.append("}\n");
        return blockCode;
    }

    private StringBuilder visitBlockElem(BlockElem blockElem, Object arg) throws  PLCCompilerException {
        StringBuilder sb = new StringBuilder();
        if(blockElem instanceof Declaration){
            sb.append(visitDeclaration((Declaration) blockElem, arg));
        } else if (blockElem instanceof Statement){
            sb.append(determineStatement((Statement) blockElem, arg));
        } else {
            throw new CodeGenException("Unsupported BlockElem type");
        }
        sb.append(";\n");
        return sb;
    }

    private StringBuilder determineStatement(Statement statement, Object arg) throws PLCCompilerException {
        StringBuilder sb = new StringBuilder();
        sb.append(switch (statement.getClass().getName()){
            case "edu.ufl.cise.cop4020fa23.ast.AssignmentStatement" -> visitAssignmentStatement((AssignmentStatement) statement, arg);
            case "edu.ufl.cise.cop4020fa23.ast.WriteStatement" -> visitWriteStatement((WriteStatement) statement, arg);
            case "edu.ufl.cise.cop4020fa23.ast.DoStatement", "edu.ufl.cise.cop4020fa23.ast.IfStatement" -> throw new UnsupportedOperationException("Unimplemented method");
            case "edu.ufl.cise.cop4020fa23.ast.ReturnStatement" -> visitReturnStatement((ReturnStatement) statement, arg);
            case "edu.ufl.cise.cop4020fa23.ast.StatementBlock" -> visitBlockStatement((StatementBlock) statement, arg);
            default -> {
                throw new CodeGenException("Unexpected value: " + statement.getClass().getName());
            }
        });
        return sb;
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

        StringBuilder trueExpr = determineExpr(conditionalExpr.getTrueExpr(), arg);

        StringBuilder falseExpr = determineExpr(conditionalExpr.getFalseExpr(), arg);

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
        sb.append(visitNameDef(nameDef, arg));

        if (initializer != null) {
            sb.append(" = ");
            sb.append(determineExpr(initializer, arg));
        }
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
    public StringBuilder visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
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
        return new StringBuilder(identExpr.getNameDef().getJavaName());
    }

    @Override
    public StringBuilder visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        // _IdentExpr_.getNameDef().getJavaName()
        return new StringBuilder(lValue.getNameDef().getJavaName());
    }

    @Override
    public StringBuilder visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {

        /*
         * Dimension is implemented in Assignment 5
         * _Type_ _name_
         * Where _name_ is the Java name of the IDENT
         */
        StringBuilder sb = new StringBuilder();
        String ident = nameDef.getIdentToken().text();
    
        // Generate Name
        int count = nameCounter.getOrDefault(ident, 0);
        String uniqueName;
        if (count > 0) {
            uniqueName = ident + "_" + count;
        } else {
            uniqueName = ident;
        }
        nameCounter.put(ident, count + 1); 
    
        nameDef.setJavaName(uniqueName);
    
        sb.append(determineType(nameDef.getType())).append(" ");
        sb.append(uniqueName);
        
        return sb;
    }

    @Override
    public StringBuilder visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        // _NumLitExpr_.getText
        System.out.println(numLitExpr.getText());
        return new StringBuilder(numLitExpr.getText());
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
    /*How to deal with import statements
    • Traverse the entire tree adding code to a StringBuilder
     • As you traverse the AST, keep track of methods called by generated
            code that would require an import.
            TODO: add a table that keeps track of methods that would require an import
        • After you return back to Program after visiting its children, you will
            have a String containing the body of the class.
        • Construct another String containing the necessary imports.
        • Concatenate the package name, the import statements, and the class
            body to get the complete Java class.*/
    //TODO: implement package name argument

    @Override
    public StringBuilder visitProgram(Program program, Object arg) throws PLCCompilerException {
        StringBuilder imports = new StringBuilder();
        StringBuilder classBody = new StringBuilder("public class ").append(program.getName()).append(" {\n");
        classBody.append("\tpublic static ").append(determineType(program.getType())).append(" apply(\n").append("\t\t");
        ListIterator<NameDef> listIterator = program.getParams().listIterator();
        while(listIterator.hasNext()){
            classBody.append(visitNameDef(listIterator.next(), arg));
            if(listIterator.hasNext()){
                classBody.append(", ");
            } else {
                classBody.append("\n");
            }
        }
        classBody.append("\t) ").append(visitBlock(program.getBlock(), arg)).append("\n");
        classBody.append("}\n");
        //^^^ unsure if the above \n is necessary

        for (String importString : importSet) {
            imports.append("import ").append(importString).append(";\n");
        }
        StringBuilder completeJavaClass = new StringBuilder();
        String packageName = arg != null ? (String) arg : "";

        // check if null or empty
        if (!packageName.isEmpty()) {
            completeJavaClass.append("package ").append(packageName).append(";\n\n");
        }

        if (imports.length() > 0) {
            completeJavaClass.append(imports).append("\n");
        }

        completeJavaClass.append(classBody);

        return completeJavaClass;
    }

    private String determineType(Type type) throws CodeGenException {
        return switch(type) {
            case INT-> "int";
            case STRING -> "String";
            case VOID -> "void";
            case BOOLEAN -> "boolean";
            case IMAGE,PIXEL -> throw new UnsupportedOperationException("Unimplemented method");
        };
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
        subString.append(convertOpKind(unaryExpr.getOp()));
        System.out.println(subString);
        subString.append(determineExpr(unaryExpr.getExpr(), arg)).append(")");
        System.out.println(subString);
        return subString;
    }

    @Override
    public StringBuilder visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        /*
         * ConsoleIO.write( _Expr_ )
         * Note: you will need to import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO
         */
        importSet.add("edu.ufl.cise.cop4020fa23.runtime.ConsoleIO");
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
            case "edu.ufl.cise.cop4020fa23.ast.ConditionalExpr" -> {
                subString.append(visitConditionalExpr((ConditionalExpr) subExpr, arg));
            }
            case "edu.ufl.cise.cop4020fa23.ast.BinaryExpr" -> {
                subString.append(visitBinaryExpr((BinaryExpr) subExpr, arg));
            }
            case "edu.ufl.cise.cop4020fa23.ast.UnaryExpr" -> {
                subString.append(visitUnaryExpr((UnaryExpr) subExpr, arg));
            }
            case "edu.ufl.cise.cop4020fa23.ast.StringLitExpr" -> {
                subString.append(visitStringLitExpr((StringLitExpr) subExpr, arg));
            }
            case "edu.ufl.cise.cop4020fa23.ast.NumLitExpr" -> {
                subString.append(visitNumLitExpr((NumLitExpr) subExpr, arg));
            }
            case "edu.ufl.cise.cop4020fa23.ast.IdentExpr" -> {
                subString.append(visitIdentExpr((IdentExpr) subExpr, arg));
            }
            case "edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr" -> {
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
        return new StringBuilder(switch (booleanLitExpr.getText()) {
            case "FALSE" -> "false";
            case "TRUE" -> "true";
            default -> throw new CodeGenException("Unexpected type in BooleanLitExpr");
        });
    }

    @Override
    public StringBuilder visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        throw new UnsupportedOperationException("Unimplemented method");
    }
}
