package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.ast.Dimension;
import edu.ufl.cise.cop4020fa23.exceptions.*;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import org.junit.jupiter.api.Test;

import static edu.ufl.cise.cop4020fa23.Kind.*;
import static edu.ufl.cise.cop4020fa23.ast.Type.*;

import java.awt.*;
import java.util.*;
import java.util.List;
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
    private int numOfDoStatements = 0;

    // TODO: Possibly replace function calls with .visit calls?
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg)
            throws PLCCompilerException {

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
        // TODO: If LValue has a pixelSelector or channelselector, visit them here
        PixelSelector pix = assignmentStatement.getlValue().getPixelSelector();
        ChannelSelector chan = assignmentStatement.getlValue().getChannelSelector();
        LValue val = assignmentStatement.getlValue();
        StringBuilder LVString = new StringBuilder((StringBuilder) val.visit(this, arg));
        Expr ex = assignmentStatement.getE();
        StringBuilder sb = new StringBuilder();




        if (assignmentStatement.getlValue().getVarType() == IMAGE) {

            // • PixelSelector == null and ChannelSelector == null (i.e. something like im0
            // = expr; )
            if (pix == null && chan == null) {
                /*
                 * • If Expr.type = IMAGE, use ImageOps.copyInto to copy the Expr image into the
                 * LValue image
                 * • If Expr.type = PIXEL, use ImageOps.setAllPixels to update each pixel of the
                 * LValue
                 * image with the given pixel value
                 * • If Expr.type = STRING, load the image from the URL in the string, and
                 * resizing to the
                 * size of the LValue image. Then copy the pixels of the loaded image into the
                 * LValue
                 * image. Use FileURLIO.readImage and ImageOps.copyInto
                 * 
                 */
                importSet.add("edu.ufl.cise.cop4020fa23.runtime.ImageOps");
                switch (ex.getType()) {
                    case IMAGE -> {
                        sb.append("ImageOps.copyInto(");
                        sb.append(ex.visit(this, arg)).append(",").append(val.visit(this, arg));
                    }
                    case PIXEL -> {
                        sb.append("ImageOps.setAllPixels(");
                        sb.append(val.visit(this, arg)).append(",").append(ex.visit(this, arg));
                    }
                    case STRING -> {
                        // TODO: figure out how to resize images properly
                        importSet.add("edu.ufl.cise.cop4020fa23.FileURLIO");
                        sb.append("ImageOps.copyInto(FileURLIO.readImage(").append(ex.visit(this, arg)).append("),");
                        sb.append(val.visit(this, arg));
                    }
                    default -> throw new CodeGenException("illegal expression type: " + ex.getType());
                }
                ;
                return sb.append(");");
            } else {
                if (pix == null) {
                    throw new UnsupportedOperationException("Not to be implemented");
                }
                StringBuilder pixString = (StringBuilder) pix.visit(this, arg);
                StringBuilder JNX = (StringBuilder) pix.xExpr().visit(this, arg);
                StringBuilder JNY = (StringBuilder) pix.yExpr().visit(this, arg);
                StringBuilder Brackets = new StringBuilder();
                if (((IdentExpr) pix.xExpr()).getNameDef() instanceof SyntheticNameDef) {
                    System.out.println("hello");
                    sb.append("for (int ").append(JNX).append(" = 0; ");
                    sb.append(JNX).append("<").append(LVString).append(".getWidth();");
                    sb.append(JNX).append("++){\n");
                    Brackets.append("}");
                }
                if (((IdentExpr) pix.yExpr()).getNameDef() instanceof SyntheticNameDef) {
                    sb.append("for (int ").append(JNY).append(" = 0; ");
                    sb.append(JNY).append("<").append(LVString).append(".getHeight();");
                    sb.append(JNY).append("++){\n");
                    Brackets.append("}");
                }
                sb.append("ImageOps.setRGB(").append(LVString).append(",").append(JNX).append(",");
                sb.append(JNY).append(",").append(ex.visit(this, arg)).append(");\n");
                sb.append(Brackets);
                return sb;
            }
        }
        if (assignmentStatement.getlValue().getVarType() == PIXEL){
            importSet.add("edu.ufl.cise.cop4020fa23.runtime.PixelOps");
                 if(assignmentStatement.getlValue().getChannelSelector() != null) {
                     sb.append(LVString).append(" = ");
                     sb.append(switch (assignmentStatement.getlValue().getChannelSelector().color()) {
                         case RES_red -> "PixelOps.setRed(";
                         case RES_blue -> "PixelOps.setBlue(";
                         case RES_green -> "PixelOps.setGreen(";
                         default -> throw new CodeGenException("Unexpected color kind in channelSelector");
                     });
                     sb.append(LVString).append(" , ")
                             .append(assignmentStatement.getE().visit(this, arg));
                     return sb.append(")");
                 } else if (ex.getType() == INT) {
                     sb.append(val.visit(this, arg))
                             .append(" = PixelOps.pack(")
                             .append(ex.visit(this, arg)).append(", ")
                             .append(ex.visit(this, arg)).append(", ")
                             .append(ex.visit(this, arg)).append(");");
                     return sb;
                 }

        }
        // _LValue_ = _Expr_
        if (assignmentStatement.getlValue() == null) {
            throw new CodeGenException("LValue in assignment statement is null");
        }
        if (assignmentStatement.getlValue().getName() == null
                || assignmentStatement.getlValue().getName().isEmpty()) {
            throw new CodeGenException("Variable name in LValue is null or empty.");
        }
        if (assignmentStatement.getE() == null) {
            throw new CodeGenException("Expression in assignment statement is null");
        }
        System.out.println("hello!");
        sb.append(val.visit(this, arg)).append(" = ");
        sb.append(assignmentStatement.getE().visit(this, arg));

        
        return sb;
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

        /*
         * Binary operations on these types are generally carried out component-wise.
         * For example, for images im0 and im1, the expression (e0+e1) yields a new
         * image with the
         * individual pixels added together componentwise. Pixels in turn are also added
         * componentwise.
         * For pixels p0, and p1, the expression (p0 + p1) yields a pixel with
         * individual color channel
         * values added together. Any time the value of a color channel goes out of the
         * range [0,255], it is
         * truncated to the maximum or minimum value.
         * TODO: When a binary operation has one operand with a “larger” type than the
         * other, the value of the
         * TODO: smaller type is replicated. So for example, im0/2 would divide each
         * pixel in im0 by 2. For each
         * TODO: pixel, each individual color channel value is divided by 2.
         * Several routines in edu.ufl.cise.cop4020fa23.ImageOps have been provided to
         * implement
         * binary expressions with images and pixels.
         * See binaryImageIMageOp (combine two images),
         * TODO: image + image
         * binaryImagePixelOp (combine an image with a pixel),
         * TODO: image + pixel
         * binaryImageIntOp (combine an image with an int),
         * TODO: image + int
         * binaryPackedPixelPixelOP (combine two pixels),
         * TODO: pixel + pixel
         * binaryPackedPixelIntOp (combine a pixel with an int) and
         * TODO: pixel + int
         * binaryPackedPixelBooleanOP (compare two pixels Boolean operator).
         * TODO: pixel boolean pixel
         */

        StringBuilder leftSb = (StringBuilder) binaryExpr.getLeftExpr().visit(this, arg);
        StringBuilder rightSb = (StringBuilder) binaryExpr.getRightExpr().visit(this, arg);

        Type leftType = binaryExpr.getLeftExpr().getType();
        Type rightType = binaryExpr.getRightExpr().getType();
        Kind opKind = binaryExpr.getOpKind();

        StringBuilder sb = new StringBuilder();

        if (leftType == IMAGE) {
            importSet.add("edu.ufl.cise.cop4020fa23.runtime.ImageOps");
            sb.append("ImageOps.").append(switch (rightType) {
                case IMAGE -> "binaryImageImageOp(";
                case PIXEL -> "binaryImagePixelOp(";
                case INT -> "binaryImageScalarOp(";
                default -> throw new CodeGenException("invalid right expression type for left type IMAGE");
            });
            sb.append(opKindToImageOp(opKind)).append(",");
            sb.append(leftSb).append(",");
            return sb.append(rightSb).append(")");
        } else if (leftType == PIXEL) {
            if (opKind == EQ) {
                importSet.add("edu.ufl.cise.cop4020fa23.runtime.ImageOps");
                sb.append("ImageOps.binaryPackedPixelBooleanOp(").append(opKindToImageOp(opKind)).append(",");
                sb.append(leftSb).append(",").append(rightSb).append(")");
                return sb;
            } else {
                // Truncation logic
                sb.append("PixelOps.pack(");
                sb.append("PixelOps.red(").append(leftSb).append(") ")
                        .append(convertOpKind(opKind)).append(" PixelOps.red(").append(rightSb).append("), ");
                sb.append("PixelOps.green(").append(leftSb).append(") ")
                        .append(convertOpKind(opKind)).append(" PixelOps.green(").append(rightSb).append("), ");
                sb.append("PixelOps.blue(").append(leftSb).append(") ")
                        .append(convertOpKind(opKind)).append(" PixelOps.blue(").append(rightSb).append(")");
                sb.append(")");
            }
        }

        else if (leftType == Type.STRING && opKind == Kind.EQ) {
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

    private String opKindToImageOp(Kind opKind) throws PLCCompilerException {
        return switch (opKind) {
            case PLUS -> "ImageOps.OP.PLUS";
            case MINUS -> "ImageOps.OP.MINUS";
            case TIMES -> "ImageOps.OP.TIMES";
            case DIV -> "ImageOps.OP.DIV";
            case MOD -> "ImageOps.OP.MOD";
            case EQ -> "ImageOps.BoolOP.EQUALS";
            default -> throw new CodeGenException("Operation " + opKind + " not supported.");
        };
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
            case RES_height -> "RES_height";
            case RES_width -> "RES_width";
            // should be all of them

            default -> throw new CodeGenException("Operation " + opKind + " not supported.");
        };
    }

    @Override
    public StringBuilder visitBlock(Block block, Object arg) throws PLCCompilerException {
        // { _BlockElem*_ }
        // BlockElem ::= Declaration | Statement
        // Extended visitBlock into two subexpressions: visitBlockElem and
        // determineStatement
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

    private StringBuilder visitBlockElem(BlockElem blockElem, Object arg) throws PLCCompilerException {
        StringBuilder sb = new StringBuilder();
        if (blockElem instanceof Declaration) {
            sb.append(visitDeclaration((Declaration) blockElem, arg));
        } else if (blockElem instanceof Statement) {
            sb.append(determineStatement((Statement) blockElem, arg));
        } else {
            throw new CodeGenException("Unsupported BlockElem type");
        }
        sb.append(";\n");
        return sb;
    }

    private StringBuilder determineStatement(Statement statement, Object arg) throws PLCCompilerException {
        StringBuilder sb = new StringBuilder();
        sb.append(switch (statement.getClass().getName()) {
            case "edu.ufl.cise.cop4020fa23.ast.AssignmentStatement" ->
                visitAssignmentStatement((AssignmentStatement) statement, arg);
            case "edu.ufl.cise.cop4020fa23.ast.WriteStatement" -> visitWriteStatement((WriteStatement) statement, arg);
            case "edu.ufl.cise.cop4020fa23.ast.DoStatement" -> visitDoStatement((DoStatement) statement, arg);
            case "edu.ufl.cise.cop4020fa23.ast.IfStatement" -> visitIfStatement((IfStatement) statement, arg);
            case "edu.ufl.cise.cop4020fa23.ast.ReturnStatement" ->
                visitReturnStatement((ReturnStatement) statement, arg);
            case "edu.ufl.cise.cop4020fa23.ast.StatementBlock" -> visitBlockStatement((StatementBlock) statement, arg);
            default -> {
                throw new CodeGenException("Unexpected value: " + statement.getClass().getName());
            }
        });
        return sb;
    }
    // TODO: ^^^^ above statement may not be needed, with revising of .visit
    // commands

    @Override
    public StringBuilder visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        // _Block_
        return (StringBuilder) statementBlock.getBlock().visit(this, arg);
    }

    @Override
    public StringBuilder visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        StringBuilder sb = new StringBuilder();
        importSet.add("edu.ufl.cise.cop4020fa23.runtime.PixelOps");
        switch (channelSelector.color()) {
            case RES_red:
                sb.append("PixelOps.red(");
                break;
            case RES_green:
                sb.append("PixelOps.green(");
                break;
            case RES_blue:
                sb.append("PixelOps.blue(");
                break;
            default:
                throw new PLCCompilerException("Invalid color channel");
        }
        return sb;
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
        /*
         * If NameDef.type is an image, there are several
         * options for the type of the Expr.
         * TODO: type image
         * If Expr,type is an image, then the value is determined by
         * whether or not a dimension has been declared.
         * TODO: type image, no dimension
         * If Expr.type is an image and the NameDef does not
         * have a Dimension, then the image being declared
         * gets its size from the image on the right side. Use
         * ImageOps.cloneImage.
         * TODO: type image, has dimension
         * If Expr.Type is an image and the NameDef does
         * have a Dimension, then the image being declared
         * is initialized to a resized version of the image in the
         * Expr. Use ImageOps.copyAndResize.
         * TODO: type string
         * If Expr.type is string, then the value should be the
         * URL of an image which is used to initialize the
         * declared variable.
         * TODO: type string, has size
         * If the NameDef has a size, then
         * the image is resized to the given size.
         * TODO: type string, no size
         * Otherwise, it takes the size of the loaded image.
         * Use edu.ufl.cise.cop4020fa23.runtime.FileURLIO
         * readImage (with or without length and width
         * parameters as appropriate)
         */
        StringBuilder sb = new StringBuilder();
        NameDef nameDef = declaration.getNameDef();
        Expr initializer = declaration.getInitializer();
        boolean isImageType = nameDef.getType() == IMAGE;

        if (nameDef.getType() == null || nameDef.getIdentToken() == null) {
            throw new CodeGenException("Invalid type or identifier in declaration.");
        }

        if (isImageType) {
            importSet.add("java.awt.image.BufferedImage");
            sb.append("final BufferedImage ").append(nameDef.getJavaName());
            importSet.add("edu.ufl.cise.cop4020fa23.runtime.ImageOps");
            if (initializer != null) {
                // Check the type of the initializer
                // Tbh idk if this is the best way to do this
                Type initializerType = initializer.getType();

                if (initializerType == IMAGE) {
                    // Handle case when initializer is an image
                    if (nameDef.getDimension() != null) {
                        // Image with dimension - resize the image
                        sb.append(" = ImageOps.copyAndResize(")
                                .append(initializer.visit(this, arg))
                                .append(", ")
                                .append(nameDef.getDimension().visit(this, arg))
                                .append(")");
                    } else {
                        // Image without dimension - clone the image
                        sb.append(" = ImageOps.cloneImage(").append(initializer.visit(this, arg)).append(")");
                    }
                } else if (initializerType == STRING) {
                    importSet.add("edu.ufl.cise.cop4020fa23.runtime.FileURLIO");
                    // Handle case when initializer is a string (URL)
                    if (nameDef.getDimension() != null) {
                        // Image with dimension - load and resize from URL
                        sb.append(" = FileURLIO.readImage(")
                                .append(initializer.visit(this, arg))
                                .append(", ")
                                .append(nameDef.getDimension().visit(this, arg))
                                .append(")");
                    } else {
                        // Image without dimension - load from URL
                        sb.append(" = FileURLIO.readImage(").append(initializer.visit(this, arg)).append(")");
                    }
                }
            } else {
                // If there is no initializer
                if (nameDef.getDimension() != null) {
                    sb.append(" = ImageOps.makeImage(").append(nameDef.getDimension().visit(this, arg)).append(")");
                } else {
                    throw new CodeGenException("Image declaration without dimension or initializer.");
                }
            }
        } else {
            // For non-image types
            sb.append(nameDef.visit(this, arg));
            if (initializer != null) {
                sb.append(" = ").append(initializer.visit(this, arg));
            }
        }
        return sb;
    }

    @Override
    public StringBuilder visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        return new StringBuilder((StringBuilder) dimension.getWidth().visit(this, arg)).append(",")
                .append(dimension.getHeight().visit(this, arg));
    }

    @Override
    public StringBuilder visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        // Compilcated, see instructions pdf
        /*
         * You will need to figure out the details yourself.
         * The semantics are like Dijkstra’s guarded command do-od statement except our
         * version is not
         * non-deterministic (i.e. is deterministic). In each iteration, the guarded
         * blocks are evaluated
         * starting from the top. (Note that this semantic choice was made for ease of
         * implementation in a
         * class project. There are alternatives that would probably be more useful in
         * practice.) The loop
         * terminates when none of the guards are true.
         * In other words, if the guards are G0, G1, .. Gn, and the corresponding blocks
         * are B0, B1,..,Bn,
         * Guards are evaluated in turn, starting with G0. When a guard, say Gi is true,
         * execute the
         * corresponding Block Bi. That is an iteration. Repeat, starting at the top
         * with G0 again, for each
         * iteration. The statement terminates when none of the guards are true.
         */

        StringBuilder sb = new StringBuilder();
        List<GuardedBlock> blocks = doStatement.getGuardedBlocks();
        numOfDoStatements++;
        StringBuilder guardName = new StringBuilder("anyGuardTrue").append("$").append(numOfDoStatements);
        sb.append("boolean ").append(guardName).append(" = false;\n");
        sb.append("do {\n");
        sb.append(guardName).append(" = false;\n");
        for (GuardedBlock guardedBlock : blocks) {
            Expr guardExpr = guardedBlock.getGuard();
            StringBuilder guardExprCode = new StringBuilder();

            // TODO: vvvv this is probably not the best way to do this
            guardExprCode.append(guardExpr.visit(this, arg));

            sb.append("if (").append(guardExprCode).append(") {\n");
            sb.append(guardName).append(" = true;\n");

            Block guardedBlockCode = guardedBlock.getBlock();
            StringBuilder blockCode = visitBlock(guardedBlockCode, arg);
            sb.append(blockCode);

            sb.append("}\n");
        }

        sb.append("} while(").append(guardName).append(");\n");
        return sb;

    }

    @Override
    public StringBuilder visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg)
            throws PLCCompilerException {
        importSet.add("edu.ufl.cise.cop4020fa23.runtime.PixelOps");
        StringBuilder sb = new StringBuilder("PixelOps.pack(");
        sb.append(expandedPixelExpr.getRed().visit(this, arg)).append(",");
        sb.append(expandedPixelExpr.getGreen().visit(this, arg)).append(",");
        return sb.append(expandedPixelExpr.getBlue().visit(this, arg)).append(")");
    }

    @Override
    public StringBuilder visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        // Handling depends on context, so we probably need to pass down an arg to
        // differentiate between do and if statements

        // Create a StringBuilder to hold the generated code
        StringBuilder sb = new StringBuilder();

        // Generate code for the guard expression
        Expr guard = guardedBlock.getGuard();
        StringBuilder guardCode = (StringBuilder) guard.visit(this, arg);
        sb.append("if (").append(guardCode).append(") {\n");

        // Generate code for the block to be executed if the guard is true
        Block block = guardedBlock.getBlock();
        StringBuilder blockCode = (StringBuilder) block.visit(this, arg);
        sb.append(blockCode);

        // Close the if block
        sb.append("}\n");

        return sb;
    }

    @Override
    public StringBuilder visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        // _IdentExpr_.getNameDef().getJavaName()
        return new StringBuilder(identExpr.getNameDef().getJavaName());
    }

    @Override
    public StringBuilder visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        // Compilcated, see instructions pdf
        /*
         * You will need to figure out the details yourself.
         * The semantics are similar to Dijkstra’s guarded command if statement except
         * our version is not
         * non-deterministic (i.e. is deterministic). The guarded blocks are evaluated
         * starting from the
         * top. One other difference is that Dijkstra’s version requires that at least
         * one guard be true. In
         * our version, if none of the guards are true, nothing will happen.
         * In other words, if the guards are G0, G1, .. Gn, and the corresponding blocks
         * are B0, B1,..,Bn,
         * Guards are evaluated in turn, starting with G0. When a guard, say Gi, is
         * true, execute the
         * corresponding Block Bi. That is the end of the if statement. The Java code
         * would look
         * something like “if (G0) {B0;} else if (G1) {B1;}… else if (Gn) {Bn;}”
         */
        StringBuilder sb = new StringBuilder();
        List<GuardedBlock> guardedBlocks = ifStatement.getGuardedBlocks();

        for (int i = 0; i < guardedBlocks.size(); i++) {
            GuardedBlock guardedBlock = guardedBlocks.get(i);
            StringBuilder guardExprCode = generateGuardExpressionCode(guardedBlock.getGuard(), arg);

            sb.append(i == 0 ? "if (" : " else if (");
            sb.append(guardExprCode);
            sb.append(") {\n");
            sb.append(visitBlock(guardedBlock.getBlock(), arg));
            sb.append("\n}");
        }
        return sb;
    }

    private StringBuilder generateGuardExpressionCode(Expr guardExpr, Object arg) throws PLCCompilerException {
        StringBuilder guardExprCode = new StringBuilder();
        if (guardExpr instanceof BinaryExpr) {
            guardExprCode = visitBinaryExpr((BinaryExpr) guardExpr, arg);
        } else if (guardExpr instanceof UnaryExpr) {
            guardExprCode = visitUnaryExpr((UnaryExpr) guardExpr, arg);
        } else if (guardExpr instanceof ConditionalExpr) {
            guardExprCode = visitConditionalExpr((ConditionalExpr) guardExpr, arg);
        } else if (guardExpr instanceof IdentExpr) {
            guardExprCode.append(visitIdentExpr((IdentExpr) guardExpr, arg));
        } else if (guardExpr instanceof NumLitExpr) {
            guardExprCode.append(visitNumLitExpr((NumLitExpr) guardExpr, arg));
        } else if (guardExpr instanceof BooleanLitExpr) {
            guardExprCode.append(visitBooleanLitExpr((BooleanLitExpr) guardExpr, arg));
        } else if (guardExpr instanceof StringLitExpr) {
            guardExprCode.append(visitStringLitExpr((StringLitExpr) guardExpr, arg));
        } else if (guardExpr instanceof ExpandedPixelExpr) {
            guardExprCode.append(visitExpandedPixelExpr((ExpandedPixelExpr) guardExpr, arg));
        } else if (guardExpr instanceof ConstExpr) {
            guardExprCode.append(visitConstExpr((ConstExpr) guardExpr, arg));
        }
        return guardExprCode;
    }

    @Override
    public StringBuilder visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        StringBuilder sb = new StringBuilder();
        String varName = lValue.getNameDef().getJavaName();
        // removed visiting of pixelselector and channelselector,
        // they will be visited in the parent assignment statement
        return sb.append(varName);
    }

    @Override
    public StringBuilder visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {

        /*
         * Dimension is implemented in Assignment 5
         * _Type_ _name_
         * Where _name_ is the Java name of the IDENT
         */
        // The dimension will be visited in the parent declaration
        StringBuilder sb = new StringBuilder();
        sb.append(determineType(nameDef.getType())).append(" ");
        sb.append(nameDef.getJavaName());
        return sb;
    }

    @Override
    public StringBuilder visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        // _NumLitExpr_.getText
        return new StringBuilder(numLitExpr.getText());
    }

    @Override
    public StringBuilder visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        return new StringBuilder((StringBuilder) pixelSelector.xExpr().visit(this, arg)).append(",")
                .append(pixelSelector.yExpr().visit(this, arg));
    }

    @Override
    public StringBuilder visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        // Implemented in Assignment 5
        /*
         * If Expr.type is Pixel
         * _ChannelSelector_ ( _Expr_ )
         * Otherwise it is an image
         * If PixelSelector != null && ChannelSelector ==null
         * Generate code to get the value of the pixel at the
         * indicated location.
         * ImageOps.getRGB( _Expr_ , _PixelSelector _ )
         * If PixelSelector != null && ChannelSelector != null,
         * generate code to get the value of the pixel at the
         * indicated location and to invoke PixelOps.red,
         * PixelOps.green, or PixelOps.blue. (You may want
         * to visit the ChannelSelector, passing info that this is
         * in the context of an expression as indicated here, or
         * you may want to just get the value from
         * visitPostfixExpr)
         * _ChannelSelector_ (ImageOps.getRGB( _Expr_ ,
         * _PixelSelector_ ))
         */
        // Probably not the cleanest implementation
        StringBuilder sb = new StringBuilder();
        Expr primary = postfixExpr.primary();
        PixelSelector pixel = postfixExpr.pixel();
        ChannelSelector chan = postfixExpr.channel();
        importSet.add("edu.ufl.cise.cop4020fa23.runtime.ImageOps");
        if (postfixExpr.primary().getType() == PIXEL) {
            // if type is pixel
            System.out.println("hello");
            sb.append(chan.visit(this, arg));
            sb.append(primary.visit(this, arg));
        } else {
            // otherwise is an image
            if (chan == null) {
                sb.append("ImageOps.getRGB(").append(primary.visit(this, arg)).append(",");
                sb.append(pixel.visit(this, arg));
            } else if (pixel != null) {
                sb.append(chan.visit(this, arg)).append("ImageOps.getRGB(").append(primary.visit(this, arg))
                        .append(",");
                sb.append(pixel.visit(this, arg)).append(")");
            } else {
                importSet.add("edu.ufl.cise.cop4020fa23.runtime.ImageOps");
                switch (chan.color()) {
                    case RES_red -> sb.append("ImageOps.extractRed(");
                    case RES_blue -> sb.append("ImageOps.extractBlu(");
                    case RES_green -> sb.append("ImageOps.extractGrn(");
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
        while (listIterator.hasNext()) {
            classBody.append(listIterator.next().visit(this, arg));
            if (listIterator.hasNext()) {
                classBody.append(", ");
            } else {
                classBody.append("\n");
            }
        }
        classBody.append("\t) ").append(program.getBlock().visit(this, arg)).append("\n");
        classBody.append("}\n");
        // ^^^ unsure if the above \n is necessary

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
        if (type == IMAGE) {
            importSet.add("java.awt.image.BufferedImage");
        }
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
                case "RES_height" -> {
                    importSet.add("java.awt.image.BufferedImage");
                    subExprString.append("(").append(operandString).append(".getHeight()").append(")");
                }
                case "RES_width" -> {
                    importSet.add("java.awt.image.BufferedImage");
                    subExprString.append("(").append(operandString).append(".getWidth()").append(")");
                }
                default -> subExprString.append("(").append(opString).append(operandString).append(")");
            }
            // TODO: may need to invoke bufferedimage
            // Idk how bufferedimage works really, oh well
        }

        return subExprString;
    }

    @Override
    public StringBuilder visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        /*
         * ConsoleIO.write( _Expr_ )
         * Note: you will need to import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO
         * The ConsoleIO class includes an overloaded
         * method write for each Java type that represents a
         * PLC Language type. Thus, you can simply
         * generate code to call the write method and let the
         * Java compiler determine which overloaded version
         * to use. The exception is that int and pixel in PLC
         * Language are both represented by a Java int.
         * When the type of Expr is pixel, you need to use the
         * writePixel method.
         */

        importSet.add("edu.ufl.cise.cop4020fa23.runtime.ConsoleIO");
        Expr subExpr = writeStatement.getExpr();
        StringBuilder subString = new StringBuilder();
        if (subExpr.getType() == PIXEL) {
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
        /*
         * If ConsExpr.name = Z then 255
         * else get hex String literal representing the
         * RGB representation of the corresponding
         * java.awt.Color.
         * Example:
         * Let the PLC Lang constant be BLUE.
         * This corresponds to the java Color constant
         * java.awt.Color.BLUE.
         * Get the packed pixel version of the color with
         * getRGB()
         * Convert to a String with Integer.toHexString
         * Prepend “0x” to make it a Java hex literal.
         * Putting it all together, you get
         * "0x" +
         * Integer.toHexString(Color.BLUE.getRGB())
         * Which is
         * 0xff0000ff
         */
        String uwu = constExpr.getName();
        if (uwu.equals("Z"))
            return new StringBuilder("255");
        return new StringBuilder("0x").append(switch (uwu) {
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
