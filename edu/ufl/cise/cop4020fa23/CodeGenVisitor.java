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

/*
    Pixels are conceptually a 3-tuple with the red, green, and blue color components. Internally,
    they are stored in a packed form where the three values are packed into a single integer.
    Because each color component must fit in 8 bits, the values are in [0, 255]. When the color
    components are provided in the form of an ExpandedPixelExpr, invoke PixelOps.pack to pack
    them into an int. To extract individual color components from an int, use methods red, green,
    and blue in PixelOps. To update individual colors in a pixel, use PixelOps methods setRed,
    setGreen, and setBlue.

    The image type in PLC Language will be represented using a java.awt.image.BufferedImage.
    An image object is instantiated when the declaration is elaborated. An image always has a
    size—either it obtains the size from a Dimension in the NameDef in its declaration, or its
    declaration has an initializer, and the size is determined from the initializer. If it does not have
    either a Dimension or an initializer, throw a CodeGenException. Once instantiated, the size of
    the image does not change.
    Make sure to add import statements to your generated code for all of the
    edu.ufl.cise.cop4020fa23.runtime classes that are used in your generated Java code.
 */
public class CodeGenVisitor implements ASTVisitor {

    // Types image and pixel are implemented in Assignment 5
    private final HashSet<String> importSet = new HashSet<>();
    //TODO: vvv below expressions & variables are probably not necessary
    public static final int SELECT_ALPHA = 0xff000000;
    public static final int SELECT_RED = 0x00ff0000;
    public static final int SELECT_GREEN = 0x0000ff00;
    public static final int SELECT_BLUE = 0x000000ff;
    public static final int SHIFT_ALPHA = 24;
    public static final int SHIFT_RED = 16;
    public static final int SHIFT_GREEN = 8;
    public static final int SHIFT_BLUE = 0;
    public static int truncate(int val){
        if(0>val){
            return 0;
        }
        return Math.min(val, 255);
    }
    public static int pack(int redVal, int grnVal, int bluVal) {
        int pixel = ((0xFF << SHIFT_ALPHA) | (truncate(redVal) << SHIFT_RED) |
                (truncate(grnVal) << SHIFT_GREEN)
                | (truncate(bluVal) << SHIFT_BLUE));
        return pixel;
    }
    public static int red(int pixel) {
        return (pixel & SELECT_RED) >> SHIFT_RED;
    }
    public static int green(int pixel) {
        return (pixel & SELECT_GREEN) >> SHIFT_GREEN;
    }
    public static int blue(int pixel) {
        return (pixel & SELECT_BLUE) >> SHIFT_BLUE;
    }
    public static int setRed(int pixel, int val) {
        return pack(val, green(pixel), blue(pixel));
    }
    public static int setGreen(int pixel, int val) {
        return pack(red(pixel), val, blue(pixel));
    }
    public static int setBlue(int pixel, int val) {
        return pack(red(pixel), green(pixel), val);
    }
    //TODO: ^^^ above expressions & variables are probably not necessary




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
    //TODO: If LValue has a pixelSelector or channelselector, visit them here
            throws PLCCompilerException {
        try {
            StringBuilder sb = new StringBuilder();
            if(assignmentStatement.getlValue().getType() == IMAGE){
                throw new UnsupportedOperationException("To be implemented: IMAGE assignment statement");
            }
            if(assignmentStatement.getlValue().getType() == PIXEL && assignmentStatement.getlValue().getChannelSelector() != null){
                sb.append(switch(assignmentStatement.getlValue().getChannelSelector().color()){
                    case RES_red -> "PixelOps.setRed(";
                    case RES_blue -> "PixelOps.setBlue(";
                    case RES_green -> "PixelOps.setGreen(";
                    default -> throw new CodeGenException("Unexpected color kind in channelSelector");
                });
                sb.append(assignmentStatement.getlValue().visit(this, arg)).append(" , ").append(assignmentStatement.getE().visit(this, arg));
                return sb.append(")");
            }
            // _LValue_ = _Expr_
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
            sb.append(assignmentStatement.getE().visit(this, arg));
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

        StringBuilder leftSb = (StringBuilder) binaryExpr.getLeftExpr().visit(this, arg);
        StringBuilder rightSb = (StringBuilder) binaryExpr.getRightExpr().visit(this, arg);

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
    //TODO: ^^^^ above statement may not be needed, with revising of .visit commands

    @Override
    public StringBuilder visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        // _Block_
        return (StringBuilder) statementBlock.getBlock().visit(this, arg);
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

        StringBuilder guardExpr = (StringBuilder) conditionalExpr.getGuardExpr().visit(this, arg);

        StringBuilder trueExpr = (StringBuilder) conditionalExpr.getTrueExpr().visit(this, arg);

        StringBuilder falseExpr = (StringBuilder) conditionalExpr.getFalseExpr().visit(this, arg);

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
        //TODO: if nameDef has a dimension, visit it here
        boolean isImage = (nameDef.getType() == IMAGE);

        if (nameDef.getType() == null || nameDef.getIdentToken() == null) {
            throw new CodeGenException("Invalid type or identifier in declaration.");
        }
        if(isImage){
            if(initializer != null){
                //TODO: isImage && initializer case vvvv
            /*
                If NameDef.type is an image, there are several
                options for the type of the Expr.
                TODO: type image
                If Expr,type is an image, then the value is determined by
                whether or not a dimension has been declared.
                    TODO: type image, no dimension
                    If Expr.type is an image and the NameDef does not
                    have a Dimension, then the image being declared
                    gets its size from the image on the right side. Use
                    ImageOps.cloneImage.
                    TODO: type image, has dimension
                    If Expr.Type is an image and the NameDef does
                    have a Dimension, then the image being declared
                    is initialized to a resized version of the image in the
                    Expr. Use ImageOps.copyAndResize.
                TODO: type string
                If Expr.type is string, then the value should be the
                URL of an image which is used to initialize the
                declared variable.
                    TODO: type string, has size
                    If the NameDef has a size, then
                    the image is resized to the given size.
                    TODO: type string, no size
                    Otherwise, it takes the size of the loaded image.
                    Use edu.ufl.cise.cop4020fa23.runtime.FileURLIO
                    readImage (with or without length and width
                    parameters as appropriate)
             */
            } else {
                sb.append("final BufferedImage ").append(nameDef.getJavaName());
                sb.append(" = ImageOps.makeImage(").append(nameDef.getDimension().visit(this, arg));
            }


            return sb.append(")");

        } else {
            sb.append(nameDef.visit(this, arg));

            if (initializer != null) {
                sb.append(" = ");
                sb.append(initializer.visit(this, arg));
            }
        }
        return sb;
    }

    @Override
    public StringBuilder visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        return new StringBuilder((StringBuilder) dimension.getWidth().visit(this, arg)).append(",").append(dimension.getHeight().visit(this, arg));
    }

    @Override
    public StringBuilder visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        //Compilcated, see instructions pdf
        /*
            You will need to figure out the details yourself.
            The semantics are like Dijkstra’s guarded command do-od statement except our version is not
            non-deterministic (i.e. is deterministic). In each iteration, the guarded blocks are evaluated
            starting from the top. (Note that this semantic choice was made for ease of implementation in a
            class project. There are alternatives that would probably be more useful in practice.) The loop
            terminates when none of the guards are true.
            In other words, if the guards are G0, G1, .. Gn, and the corresponding blocks are B0, B1,..,Bn,
            Guards are evaluated in turn, starting with G0. When a guard, say Gi is true, execute the
            corresponding Block Bi. That is an iteration. Repeat, starting at the top with G0 again, for each
            iteration. The statement terminates when none of the guards are true.
         */
        /*
        TODO vvv sample implementation of getting a single block for a do statement iteration
        StringBuilder sb = new StringBuilder();
        java.util.List<GuardedBlock> blocks = doStatement.getGuardedBlocks();
        if(determineExpr(blocks.get(0).getGuard(), arg).equals("true")){
            sb.append(visitBlock(blocks.get(0).getBlock(), arg));
        }
        return sb;*/
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        StringBuilder sb = new StringBuilder("PixelOps.pack(");
        sb.append(expandedPixelExpr.getRed().visit(this, arg)).append(",");
        sb.append(expandedPixelExpr.getGreen().visit(this, arg)).append(",");
        return sb.append(expandedPixelExpr.getBlue().visit(this, arg)).append(")");
    }

    @Override
    public StringBuilder visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        //Compilcated, see instructions pdf
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
        //Compilcated, see instructions pdf
        /*
            You will need to figure out the details yourself.
            The semantics are similar to Dijkstra’s guarded command if statement except our version is not
            non-deterministic (i.e. is deterministic). The guarded blocks are evaluated starting from the
            top. One other difference is that Dijkstra’s version requires that at least one guard be true. In
            our version, if none of the guards are true, nothing will happen.
            In other words, if the guards are G0, G1, .. Gn, and the corresponding blocks are B0, B1,..,Bn,
            Guards are evaluated in turn, starting with G0. When a guard, say Gi, is true, execute the
            corresponding Block Bi. That is the end of the if statement. The Java code would look
            something like “if (G0) {B0;} else if (G1) {B1;}… else if (Gn) {Bn;}”
         */
        throw new UnsupportedOperationException("Unimplemented method");
    }

    @Override
    public StringBuilder visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        // _IdentExpr_.getNameDef().getJavaName()
        /*(PixelSelector and ChannelSelector if present, must
        be visited. It may be easier to invoke this methods
        from the parent AssignmentStatement. )*/
        return new StringBuilder(lValue.getNameDef().getJavaName());
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
        sb.append(nameDef.getJavaName());
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
        return new StringBuilder((StringBuilder) pixelSelector.xExpr().visit(this, arg)).append(",").append(pixelSelector.yExpr().visit(this, arg));
        //TODO: small mistake in the last append statement?
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
        if (postfixExpr.getType() == PIXEL) {
            sb.append(chan.visit(this, arg)).append("(");
            sb.append(primary.visit(this, arg));
        } else {
            if (chan == null) {
                sb.append("ImageOps.getRGB(").append(primary.visit(this, arg)).append(",");
                sb.append(pixel.visit(this, arg));
            } else if (pixel != null) {
                sb.append(chan.visit(this, arg)).append("ImageOps.getRGB(").append(primary.visit(this, arg)).append(",");
                sb.append(pixel.visit(this, arg)).append(")");
            } else {
                switch (chan.color()) {
                    case RES_red -> sb.append("ImageOps.extractRed(");
                    case RES_blue -> sb.append("ImageOps.extractBlue(");
                    case RES_green -> sb.append("ImageOps.extractGreen(");
                    default -> throw new CodeGenException("Invalid channelselector color type");
                }
            sb.append(primary.visit(this, arg));
        }
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
            classBody.append(listIterator.next().visit(this, arg));
            if(listIterator.hasNext()){
                classBody.append(", ");
            } else {
                classBody.append("\n");
            }
        }
        classBody.append("\t) ").append(program.getBlock().visit(this, arg)).append("\n");
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
        subString.append(returnStatement.getE().visit(this, arg));
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
        StringBuilder operandString = (StringBuilder) unaryExpr.getExpr().visit(this, arg);



        // this is such a stupid way to do this but it works
        if (operandString.toString().startsWith("-") && opString.equals("-")) {
            subExprString.append(operandString.substring(1)); // Remove the first negation
        } else {
            switch (opString) {
                case "-" -> subExprString.append(opString).append(operandString);
                case "RES_height" -> subExprString.append("(").append(operandString).append(".getHeight()").append(")");
                case "RES_width" -> subExprString.append("(").append(operandString).append(".getWidth()").append(")");
                default -> subExprString.append("(").append(opString).append(operandString).append(")");
            }
            //TODO: may need to invoke bufferedimage
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
        subString.append(subExpr.visit(this, arg));
        subString.append(")");
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
