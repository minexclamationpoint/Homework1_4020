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
        Object leftOb = binaryExpr.getLeftExpr().visit(this, arg);
        Object rightOb = binaryExpr.getRightExpr().visit(this,arg);
        // ^ change above, only works for EXP, MINUS, TIMES, DIV, MOD when left.getType() == int
        Kind opKind = binaryExpr.getOpKind();

        int left = (Integer) leftOb;
        int right = (Integer) rightOb;
        return switch(opKind){
            case EXP -> (left << right); //doesn't work for exponents over 31 but like, come on
            case PLUS -> left + right;
            case MINUS -> left-right;
            case TIMES -> left * right;
            case DIV -> left/right;
            case MOD -> left%right;
            case EQ -> left==right;
            default -> throw new UnsupportedOperationException(); //TODO: change error
        };

        //Copied from slides
    }
    private Type inferBinaryType(Type left){ //pass right as well?
        switch(left) {
        default ->
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
            // Extract the color token from the ChannelSelector
           
    
            // Validate that the color is one of the allowed types (red, green, blue)
            Kind colorKind = channelSelector.color();
            if (colorKind != Kind.RES_red && colorKind != Kind.RES_blue && colorKind != Kind.RES_green) {
                throw new TypeCheckException("Invalid color channel specified");
            }
    
            // Here you might also want to check that the parent or associated type allows channel selection.
            // For example, if it is not of type IMAGE or PIXEL, you might want to throw a TypeCheckException.
    
            LOGGER.info("Successfully processed visitChannelSelector");
    
            // Returning the colorKind as it successfully processed the channel selection.
            // You might want to return other types depending on your logic.
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
        throw new UnsupportedOperationException();
        /*
        Declaration::= NameDef Expr?
            Condition: Expr == null
                || Expr.type == NameDef.type
                || (Expr.type == STRING && NameDef.type == IMAGE)
            Declaration.type  NameDef.type
            Note: visit Expr before NameDef
         */
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        Type typeW = (Type) dimension.getWidth().visit(this, arg);
        check(typeW == Type.INT, dimension, "image width must be int");
        Type typeH = (Type) dimension.getHeight().visit(this, arg);
        check(typeH == Type.INT, dimension, "image height must be int");
        return dimension;
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