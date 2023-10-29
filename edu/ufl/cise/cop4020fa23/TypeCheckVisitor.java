package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;

import static edu.ufl.cise.cop4020fa23.Kind.*;
import static edu.ufl.cise.cop4020fa23.ast.Type.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.logging.Logger;

/*
Your parser returns an AST
• In this assignment, we will use the visitor pattern to traverse this AST
and decorate it with type information
• Today: quick review of Visitor pattern
• also see AstractSyntaxTrees.pptx
• You will also need to implement a symbol table class.
• A Leblanc-Cook symbol table will work
• See NamesScopesBindings3.pptx
Attributes
• Attribute “type” added to
• Program
• NameDef
• Declaration
• Expr (and thus all of its
subclasses)
• Attribute “nameDef” added to
• LValue
• IdentExpr
• Your visitor will visit all the
nodes in the AST
• Nodes with attributes will
determine and set the value of
the attribute.
• There are constraints on some
attributes which must be
satisfied by a correct program.

public Object visitX(X node, Object arg);
• The visitX methods typically involve calling the visit method of it’s
children.
• The returned Object passes info from child to parent
• The Object arg parameter passes info from parent to child
• Returned value and arg can be null if nothing to pass.
LValue and IdentExpr have an attribute "nameDef" which has type NameDef
and is implemened using the enum Type in the ast packages
Program, NameDef, Declaration and all other Expr nodes have an attribute "type".
 */
public class TypeCheckVisitor implements ASTVisitor {

    private static final Logger LOGGER = Logger.getLogger(TypeCheckVisitor.class.getName());


    //vvv implemented with the symbol table class
    private SymbolTable st;

    @Override
public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
    LOGGER.info("Entering visit AssignmentStatement");
    
    try {
        LValue lValue = assignmentStatement.getlValue();
        Expr expr = assignmentStatement.getE();
    

        // Visit LValue and Expr to populate their types
        Type lValueType = (Type) lValue.visit(this, arg);
        Type exprType = (Type) expr.visit(this, arg);
        LOGGER.info("Type of LValue: " + lValueType);
        LOGGER.info("Type of Expr: " + exprType);
        
        // Type check
        if (lValueType != exprType) {
            throw new TypeCheckException("Type mismatch in assignment statement");
        }

        // Everything is fine
        LOGGER.info("Successfully processed visit AssignmentStatement");
        return lValueType;
        
    } catch (TypeCheckException e) {
        LOGGER.severe("TypeCheckException in visit AssignmentStatement: " + e.getMessage());
        throw e;
    } finally {
        LOGGER.info("Leaving visit AssignmentStatement");
    }
}

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitBinaryExpr");
    
        try {
            Type leftExprType = (Type) binaryExpr.getLeftExpr().visit(this, arg);
            Type rightExprType = (Type) binaryExpr.getRightExpr().visit(this, arg);

            Kind opKind = binaryExpr.getOpKind();
    
            Type resultType = inferBinaryType(leftExprType, opKind, rightExprType);
    
            if (resultType == null) {
                throw new TypeCheckException("Invalid combination of types and operator in binary expression");
            }

            binaryExpr.setType(resultType);
    
            LOGGER.info("Successfully processed visitBinaryExpr");
            return resultType;
    
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitBinaryExpr: " + e.getMessage());
            throw e;
        } finally {
            LOGGER.info("Leaving visitBinaryExpr");
        }
    }
    private Type inferBinaryType(Type leftType, Kind op, Type rightType) throws TypeCheckException {
        // PIXEL BITAND, BITOR PIXEL PIXEL
        if (leftType == Type.PIXEL && (op == Kind.BITAND || op == Kind.BITOR) && rightType == Type.PIXEL) {
            return Type.PIXEL;
        }
        // BOOLEAN AND, OR BOOLEAN BOOLEAN
        else if (leftType == Type.BOOLEAN && (op == Kind.AND || op == Kind.OR) && rightType == Type.BOOLEAN) {
            return Type.BOOLEAN;
        }
        // INT LT, GT, LE, GE INT BOOLEAN
        else if (leftType == Type.INT && (op == Kind.LT || op == Kind.GT || op == Kind.LE || op == Kind.GE) && rightType == Type.INT) {
            return Type.BOOLEAN;
        }
        // any EQ ExprleftExpr.type BOOLEAN
        else if (op == Kind.EQ && leftType == rightType) {
            return Type.BOOLEAN;
        }
        // INT EXP INT INT
        else if (leftType == Type.INT && op == Kind.EXP && rightType == Type.INT) {
            return Type.INT;
        }
        // PIXEL EXP INT PIXEL
        else if (leftType == Type.PIXEL && op == Kind.EXP && rightType == Type.INT) {
            return Type.PIXEL;
        }
        // PIXEL,IMAGE TIMES, DIV, MOD INT ExprleftExpr.type
        else if ((leftType == Type.PIXEL || leftType == Type.IMAGE) && (op == Kind.TIMES || op == Kind.DIV || op == Kind.MOD) && rightType == Type.INT) {
            return leftType;
        }
        // Any PLUS ExprleftExpr.type ExprleftExpr.type
        else if (op == Kind.PLUS && leftType == rightType) {
            return leftType;
        }
        // INT,PIXEL,IMAGE MINUS, TIMES, DIV, MOD ExprleftExpr.type ExprleftExpr.type
        else if ((leftType == Type.INT || leftType == Type.PIXEL || leftType == Type.IMAGE) && (op == Kind.MINUS || op == Kind.TIMES || op == Kind.DIV || op == Kind.MOD) && leftType == rightType) {
            return leftType;
        }
        else {
            throw new TypeCheckException("Invalid combination of leftType, op, and rightType");
        }
    }
    

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        st.enterScope();
        List<BlockElem> blockElems = block.getElems();
        for (BlockElem elem : blockElems) {
            elem.visit(this, arg);
        }
        st.leaveScope();
        return block;
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitBlockStatement");
    
        try {
            // Entering a new scope
            st.enterScope();
    
            // Visit the block within the StatementBlock
            Block block = statementBlock.getBlock();
            block.visit(this, arg);
    
            // Leaving the scope
            st.leaveScope();
    
            LOGGER.info("Successfully processed visitBlockStatement");
            return null;  // BlockStatement doesn't have a type
            
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitBlockStatement: " + e.getMessage());
            throw e;
        } finally {
            LOGGER.info("Leaving visitBlockStatement");
        }
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitChannelSelector");
    
        try {

            // Validate that the color is one of the allowed types (red, green, blue)
            Kind colorKind = channelSelector.color();
            if (colorKind != Kind.RES_red && colorKind != Kind.RES_blue && colorKind != Kind.RES_green) {
                throw new TypeCheckException("Invalid color channel specified");
            }
    

    
            LOGGER.info("Successfully processed visitChannelSelector");
    

            return colorKind;
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitChannelSelector: " + e.getMessage());
            throw e;
        } finally {
            LOGGER.info("Leaving visitChannelSelector");
        }
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitConditionalExpr");
    
        try {
            // Visit and type check the guard expression
            Type guardType = (Type) conditionalExpr.getGuardExpr().visit(this, arg);
            if (guardType != Type.BOOLEAN) {
                throw new TypeCheckException("The guard expression in a conditional must be of type BOOLEAN");
            }
    
            // Visit and type check the true and false expressions
            Type trueExprType = (Type) conditionalExpr.getTrueExpr().visit(this, arg);
            Type falseExprType = (Type) conditionalExpr.getFalseExpr().visit(this, arg);
    
            // The types of trueExpr and falseExpr must be the same
            if (trueExprType != falseExprType) {
                throw new TypeCheckException("The true and false expressions in a conditional must have the same type");
            }
    
            // Everything is fine, set the type of the ConditionalExpr to be the same as that of trueExpr
            conditionalExpr.setType(trueExprType);
    
            LOGGER.info("Successfully processed visitConditionalExpr");
            return trueExprType;
    
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitConditionalExpr: " + e.getMessage());
            throw e;
    
        } finally {
            LOGGER.info("Leaving visitConditionalExpr");
        }
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitDeclaration");
    
        try {
            // Get the NameDef and its type
            NameDef nameDef = declaration.getNameDef();
            Type nameDefType = (Type) nameDef.visit(this, arg); // This should populate the type in NameDef as well
    
            // Get the initializer Expr and its type if it is not null
            Expr initializer = declaration.getInitializer();
            Type initializerType = null;
            if (initializer != null) {
                initializerType = (Type) initializer.visit(this, arg); // This should populate the type in Expr as well
            }
    
            // Check conditions
            if (initializerType != null) {
                if (initializerType != nameDefType) {
                    if (initializerType == Type.STRING && nameDefType == Type.IMAGE) {
                        // Special case is valid, do nothing
                    } else {
                        throw new TypeCheckException("Type mismatch between NameDef and Expr in declaration");
                    }
                }
            }
    
            // Successfully processed the declaration
            LOGGER.info("Successfully processed visitDeclaration");
            return nameDefType;  // Returning the type of NameDef
    
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitDeclaration: " + e.getMessage());
            throw e;
    
        } finally {
            LOGGER.info("Leaving visitDeclaration");
        }
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitDimension");
    
        try {
            // Visit and type check the width expression
            Type widthType = (Type) dimension.getWidth().visit(this, arg);
            
            // Use the check method to ensure the width type is INT
            check(widthType == Type.INT, dimension, "Width expression must be of type INT");
            
            // Visit and type check the height expression
            Type heightType = (Type) dimension.getHeight().visit(this, arg);
            
            // Use the check method to ensure the height type is INT
            check(heightType == Type.INT, dimension, "Height expression must be of type INT");
            
            // Everything is fine
            LOGGER.info("Successfully processed visitDimension");
            
            return dimension;  // Return the dimension object
    
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitDimension: " + e.getMessage());
            throw e;
            
        } finally {
            LOGGER.info("Leaving visitDimension");
        }
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        Type type = null;  // Initialize to a default value
        LOGGER.info("Entering visitNameDef");
        try {
            type = nameDef.getType();  // Assuming that getType() returns the Type of the NameDef
            Dimension dimension = nameDef.getDimension();  // Assuming that getDimension() returns the Dimension
            
            if (dimension != null) {
                if (type != Type.IMAGE) {
                    throw new TypeCheckException("Dimension can only be associated with IMAGE type");
                }
            } else if (!Arrays.asList(Type.INT, Type.BOOLEAN, Type.STRING, Type.PIXEL, Type.IMAGE).contains(type)) {
                throw new TypeCheckException("Invalid type for NameDef");
            }
    
            st.insert(nameDef);  // Inserting the NameDef into the symbol table
            LOGGER.info("Successfully processed visitNameDef");
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitNameDef: " + e.getMessage());
            throw e;
        } finally {
            LOGGER.info("Leaving visitNameDef");
        }
        return type;  // Return the type, which could be null if an exception was thrown
    }
    


    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        Type type = Type.INT;
        numLitExpr.setType(type);
        return type;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        Type type = Type.kind2type(program.getTypeToken().kind());
        program.setType(type);  // Setting the type attribute for Program
        st.enterScope();  // Entering a new scope in the symbol table
        List<NameDef> params = program.getParams();
        for (NameDef param : params) {
            param.visit(this, arg);  // Visiting each NameDef child node
        }
        program.getBlock().visit(this, arg);  // Visiting the Block child node
        st.leaveScope();  // Leaving the scope in the symbol table
        return type;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
        /*
        PLCCompilerException {
        writeStatement.getExpr().visit(this, arg);
        return writeStatement;
         */
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        throw new UnsupportedOperationException();
    }
    private void check(boolean bool, AST ast, String str) throws TypeCheckException{
        LOGGER.info("Entering check method");
        if (!bool) {
            LOGGER.severe("TypeCheckException in check: " + str);
            throw new TypeCheckException(ast.firstToken.sourceLocation(), str);
        }
        LOGGER.info("Successfully processed check method");
    }
}