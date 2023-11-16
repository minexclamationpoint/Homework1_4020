package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.ast.Dimension;
import edu.ufl.cise.cop4020fa23.exceptions.*;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;

import static edu.ufl.cise.cop4020fa23.Kind.*;
import static edu.ufl.cise.cop4020fa23.ast.Type.*;

import java.awt.*;
import java.util.*;
import java.lang.Math;

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
    private HashMap<StringBuilder, Integer> javaNameSet = new HashMap<>();

    // TODO: Possibly replace function calls with .visit calls?
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg)
            /*
             * If LValue.varType == image. See explanation
             * below.
             * If LValue.varType == pixel and
             * LValue.ChannelSelector != null
             * PixelOps.setRed(_LValue_ , _Expr_)
             * or setGreen or SetBlue
             * Otherwise
             * _LValue_ = _Expr_
             */
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
         * See pdf for handling Pixel and Image types.
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
            case "edu.ufl.cise.cop4020fa23.ast.DoStatement" -> visitDoStatement((DoStatement) statement, arg);
            case "edu.ufl.cise.cop4020fa23.ast.IfStatement" -> visitIfStatement((IfStatement) statement, arg);
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
        /*
         * See PostfixExpr rule for how to handle this in
         * context of an expression. See LValue for how to
         * handle in context of an LValue
         */
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
         * If NameDef.type is image, instantiate a
         * BufferedImage object using
         * ImageOps.makeImage.The size of the image
         * comes from the Dimension object in the NameDef.
         * If there is no Dimension object, this is an error:
         * throw a CodeGenException.
         * (Note: you may choose a different way to divide
         * the code generation between visitDeclaration and
         * visitNameDef)
         * final BufferedImage NameDef.javaName =
         * ImageOps.makeImage( _Dimension_ )
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
        return new StringBuilder(determineExpr(dimension.getWidth(), arg)).append(",").append(determineExpr(dimension.getHeight(), arg));
    }

    @Override
    public StringBuilder visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        //Compilcated, see instructions pdf
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        StringBuilder sb = new StringBuilder("PixelOps.pack(");
        sb.append(determineExpr(expandedPixelExpr.getRed(), arg)).append(",");
        sb.append(determineExpr(expandedPixelExpr.getGreen(), arg)).append(",");
        return sb.append(determineExpr(expandedPixelExpr.getBlue(), arg)).append(")");
    }

    @Override
    public StringBuilder visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        //Compilcated, see instructions pdf
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public StringBuilder updateJavaName(NameDef nameDef) throws PLCCompilerException {
        StringBuilder javaName = new StringBuilder(nameDef.getJavaName());
        StringBuilder name = new StringBuilder(nameDef.getIdentToken().text());
        if(!(javaName.compareTo(name) == 0)){
            return new StringBuilder(javaName);
        }else if(javaNameSet.containsKey(javaName)){
            int old = javaNameSet.get(javaName);
            javaNameSet.replace(javaName, old, old+1);
            javaName.append("$").append(old+1);
            nameDef.setJavaName(javaName.toString());
            return javaName;
        } else {
            javaNameSet.put(javaName, 1);
            javaName.append("$1");
            nameDef.setJavaName(javaName.toString());
            return javaName;
        }
    }

    @Override
    public StringBuilder visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        // _IdentExpr_.getNameDef().getJavaName()
        return updateJavaName(identExpr.getNameDef());
    }

    @Override
    public StringBuilder visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        //Compilcated, see instructions pdf
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        // _IdentExpr_.getNameDef().getJavaName()
        /*(PixelSelector and ChannelSelector if present, must
        be visited. It may be easier to invoke this methods
        from the parent AssignmentStatement. )*/
        return updateJavaName(lValue.getNameDef());
    }

    @Override
    public StringBuilder visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {

        /*
         * Dimension is implemented in Assignment 5
         * _Type_ _name_
         * Where _name_ is the Java name of the IDENT
         */
        //The dimension will be visited in the parent declaration
        StringBuilder sb = new StringBuilder();
        sb.append(determineType(nameDef.getType())).append(" ");
        sb.append(updateJavaName(nameDef));
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
        return new StringBuilder(determineExpr(pixelSelector.xExpr(), arg)).append(",").append(pixelSelector.yExpr());
    }

    @Override
    public StringBuilder visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        /*If Expr.type is Pixel
        _ChannelSelector_ ( _Expr_ )
        Otherwise it is an image
        If PixelSelector != null && ChannelSelector ==null
        Generate code to get the value of the pixel at the
        indicated location.
        ImageOps.getRGB( _Expr_ , _PixelSelector _ )
        If PixelSelector != null && ChannelSelector != null,
        generate code to get the value of the pixel at the
        indicated location and to invoke PixelOps.red,
        PixelOps.green, or PixelOps.blue. (You may want
        to visit the ChannelSelector, passing info that this is
        in the context of an expression as indicated here, or
        you may want to just get the value from
        visitPostfixExpr)
        _ChannelSelector_ (ImageOps.getRGB( _Expr_ ,
        _PixelSelector_ ))
         */
        //Probably not the cleanest implementation
        //Requires import statement for ImageOps and getRHB I think
        StringBuilder sb = new StringBuilder();
        Expr primary = postfixExpr.primary();
        PixelSelector pixel = postfixExpr.pixel();
        ChannelSelector chan = postfixExpr.channel();
        if(postfixExpr.getType() == PIXEL){
            sb.append(visitChannelSelector(chan, arg)).append("(");
            sb.append(determineExpr(primary, arg));
        } else if(chan == null){
            sb.append("ImageOps.getRGB(").append(determineExpr(primary, arg)).append(",");
            sb.append(visitPixelSelector(pixel, arg));
        } else if(pixel != null){
            sb.append(visitChannelSelector(chan, arg)).append("ImageOps.getRGB(").append(determineExpr(primary, arg)).append(",");
            sb.append(visitPixelSelector(pixel, arg)).append(")");
        } else {
            switch (chan.color()){
                case RES_red -> sb.append("ImageOps.extractRed(");
                case RES_blue -> sb.append("ImageOps.extractBlue(");
                case RES_green -> sb.append("ImageOps.extractGreen(");
                default -> throw new CodeGenException("Invalid channelselector color type");
            }
            sb.append(determineExpr(primary, arg));
        }
        return sb.append(")");
    }

    @Override
    public StringBuilder visitProgram(Program program, Object arg) throws PLCCompilerException {
        StringBuilder imports = new StringBuilder();
        StringBuilder classBody = new StringBuilder("public class ").append(program.getName()).append(" {\n");
        classBody.append("\tpublic static ").append(determineType(program.getType())).append(" apply(\n")
                .append("\t\t");
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

        if (!imports.isEmpty()) {
            completeJavaClass.append(imports).append("\n");
        }

        completeJavaClass.append(classBody);

        return completeJavaClass;
    }

    private String determineType(Type type) throws CodeGenException {
        return switch (type) {
            case INT, PIXEL -> "int";
            case STRING -> "String";
            case VOID -> "void";
            case BOOLEAN -> "boolean";
            case IMAGE -> "BufferedImage";
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
        if (unaryExpr == null || unaryExpr.getOp() == null || unaryExpr.getExpr() == null) {
            throw new CodeGenException("Null reference in unary expression components.");
        }

        // Uhh putting error handling breaks code lmao

        StringBuilder subExprString = new StringBuilder();
        String opString = convertOpKind(unaryExpr.getOp());
        StringBuilder operandString = determineExpr(unaryExpr.getExpr(), arg);



        // this is such a stupid way to do this but it works
        if (operandString.toString().startsWith("-") && opString.equals("-")) {
            subExprString.append(operandString.substring(1)); // Remove the first negation
        }
        switch (opString) {
            case "-" -> subExprString.append(opString).append(operandString);
            case "RES_height" -> subExprString.append("(").append(operandString).append(".getHeight()").append(")");
            case "RES_width" -> subExprString.append("(").append(operandString).append(".getWidth()").append(")");
            default -> subExprString.append("(").append(opString).append(operandString).append(")");
        }

        return subExprString;
    }

    @Override
    public StringBuilder visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        /*
         * ConsoleIO.write( _Expr_ )
         * Note: you will need to import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO
         * The ConsoleIO class includes an overloaded
        method write for each Java type that represents a
        PLC Language type. Thus, you can simply
        generate code to call the write method and let the
        Java compiler determine which overloaded version
        to use. The exception is that int and pixel in PLC
        Language are both represented by a Java int.
        When the type of Expr is pixel, you need to use the
        writePixel method.
         */

        importSet.add("edu.ufl.cise.cop4020fa23.runtime.ConsoleIO");
        Expr subExpr = writeStatement.getExpr();
        StringBuilder subString = new StringBuilder();
        if(subExpr.getType() == PIXEL){
            subString.append("ConsoleIO.writePixel(");
        } else {
            subString.append("ConsoleIO.write(");
        }
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
            case "edu.ufl.cise.cop4020fa23.ast.PostFixExpr" -> {
                subString.append(visitPostfixExpr((PostfixExpr) subExpr, arg));
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
            case "edu.ufl.cise.cop4020fa23.ast.ConstExpr" -> {
                subString.append(visitConstExpr((ConstExpr) subExpr, arg));
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
        /*If ConsExpr.name = Z then 255
        else get hex String literal representing the
        RGB representation of the corresponding
        java.awt.Color.
        Example:
        Let the PLC Lang constant be BLUE.
        This corresponds to the java Color constant
        java.awt.Color.BLUE.
        Get the packed pixel version of the color with
        getRGB()
        Convert to a String with Integer.toHexString
        Prepend “0x” to make it a Java hex literal.
        Putting it all together, you get
        "0x" +
        Integer.toHexString(Color.BLUE.getRGB())
        Which is
        0xff0000ff*/
        String uwu = constExpr.getName();
        if(uwu.equals("Z"))
            return new StringBuilder("255");
        return new StringBuilder("0x").append(switch(uwu){
            case "BLACK" -> Integer.toHexString(Color.BLACK.getRGB());
            case "BLUE" -> Integer.toHexString(Color.BLUE.getRGB());
            case "CYAN" -> Integer.toHexString(Color.CYAN.getRGB());
            case "DARK_GRAY" -> Integer.toHexString(Color.DARK_GRAY.getRGB());
            case "GRAY" -> Integer.toHexString(Color.GRAY.getRGB());
            case "GREEN" -> Integer.toHexString(Color.GREEN.getRGB());
            case "LIGHT_GRAY" -> Integer.toHexString(Color.LIGHT_GRAY.getRGB());
            case "MAGENTA" -> Integer.toHexString(Color.MAGENTA.getRGB());
            case "ORANGE" -> Integer.toHexString(Color.ORANGE.getRGB());
            case "PINK" -> Integer.toHexString(Color.PINK.getRGB());
            case "RED" -> Integer.toHexString(Color.RED.getRGB());
            case "WHITE" -> Integer.toHexString(Color.WHITE.getRGB());
            case "YELLOW" -> Integer.toHexString(Color.YELLOW.getRGB());
            default -> throw new CodeGenException("Unexpected value: " + constExpr.getName());
        });
    }
}
