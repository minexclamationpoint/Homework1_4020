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
    private SymbolTable st = new SymbolTable();
    

   // private Type currentProgramType;

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visit AssignmentStatement");
        
        try {
            // Entering a new scope for this assignment statement
            st.enterScope();
    
            // Get the LValue and Expr from the assignment statement
            LValue lValue = assignmentStatement.getlValue();
            Expr expr = assignmentStatement.getE();
    
            // Visit LValue and Expr to populate their types
            Type lValueType = (Type) lValue.visit(this, true); // Assuming LValue context
            Type exprType = (Type) expr.visit(this, null); // Assuming no special context for Expr
            LOGGER.info("Type of LValue: " + lValueType);
            LOGGER.info("Type of Expr: " + exprType);
    
            // Type compatibility check
            if (lValueType != exprType &&
                !(lValueType == Type.PIXEL && exprType == Type.INT) &&
                !(lValueType == Type.IMAGE && (exprType == Type.PIXEL || exprType == Type.INT || exprType == Type.STRING))) {
                throw new TypeCheckException("Type mismatch in assignment statement");
            }
    
          
    
            LOGGER.info("Successfully processed visit AssignmentStatement");
            return lValueType;
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visit AssignmentStatement: " + e.getMessage());
            throw e;
        } finally {
            // Make sure to leave the scope in case of an exception
            st.leaveScope();
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
        LOGGER.info("Entering visitBlock");
    
        st.enterScope();
        List<BlockElem> blockElems = block.getElems();
        for (BlockElem elem : blockElems) {
            elem.visit(this, arg); // Passing the program type down to the block elements
        }
        st.leaveScope();
    
        LOGGER.info("Leaving visitBlock");
        return null; // Block itself doesn't have a type
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
        LOGGER.info("Entering visitDoStatement");

        try {
            // Entering a new scope
            st.enterScope();

            // Iterate over each GuardedBlock
            List<GuardedBlock> guardedBlocks = doStatement.getGuardedBlocks();
            for (GuardedBlock guardedBlock : guardedBlocks) {
                // Visit and type check the guard expression of the GuardedBlock
                Type guardType = (Type) guardedBlock.getGuard().visit(this, arg);
                if (guardType != Type.BOOLEAN) {
                    throw new TypeCheckException("The guard expression in a DoStatement must be of type BOOLEAN");
                }

                // Visit the block of the GuardedBlock
                Block block = guardedBlock.getBlock();
                block.visit(this, arg);
            }

            // Leaving the scope
            st.leaveScope();

            LOGGER.info("Successfully processed visitDoStatement");
            return null;  // DoStatement doesn't have a type

        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitDoStatement: " + e.getMessage());
            throw e;
        } finally {
            LOGGER.info("Leaving visitDoStatement");
        }
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitExpandedPixelExpr");
    
        boolean scopeEntered = false;
    
        try {
            // Check if we are in a context where the identifiers could be implicitly declared
            if (st.lookup("x") == null) {
                SyntheticNameDef nameX = new SyntheticNameDef("x");
                st.enterScope();
                st.insert(nameX);
                scopeEntered = true;
                LOGGER.info("Implicitly declared x");
            }
            if (st.lookup("y") == null) {
                SyntheticNameDef nameY = new SyntheticNameDef("y");
                if (!scopeEntered) {
                    st.enterScope();
                }
                st.insert(nameY);
                scopeEntered = true;
                LOGGER.info("Implicitly declared y");
            }
    
            // Visit and type-check the red, green, and blue expressions
            Type redType = (Type) expandedPixelExpr.getRed().visit(this, "PixelSelectorContext");
            Type greenType = (Type) expandedPixelExpr.getGreen().visit(this, "PixelSelectorContext");
            Type blueType = (Type) expandedPixelExpr.getBlue().visit(this, "PixelSelectorContext");
    
      
            if (redType != Type.INT || greenType != Type.INT || blueType != Type.INT) {
                throw new TypeCheckException("The expressions for red, green, and blue in an ExpandedPixelExpr must be of type INT");
            }

            expandedPixelExpr.setType(Type.PIXEL);
    
            LOGGER.info("Successfully processed visitExpandedPixelExpr");
            return Type.PIXEL;
    
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitExpandedPixelExpr: " + e.getMessage());
            throw e;
        } finally {

            if (scopeEntered) {
                st.leaveScope();
            }
            LOGGER.info("Leaving visitExpandedPixelExpr");
        }
    }

    

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitGuardedBlock");

        try {
            // Visit and type-check the guard expression
            Expr guard = guardedBlock.getGuard();
            Type guardType = (Type) guard.visit(this, arg);
            if (guardType != Type.BOOLEAN) {
                throw new TypeCheckException("The guard expression in a guarded block must be of type BOOLEAN");
            }

            // Visit the block within the GuardedBlock
            Block block = guardedBlock.getBlock();
            block.visit(this, arg);

            LOGGER.info("Successfully processed visitGuardedBlock");
            return null;  // GuardedBlock doesn't have a type

        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitGuardedBlock: " + e.getMessage());
            throw e;

        } finally {
            LOGGER.info("Leaving visitGuardedBlock");
        }
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitIdentExpr");
        LOGGER.info("Current Symbol Table: " + st.toString());
    
        try {
            // Lookup the NameDef for the identifier in the symbol table
            NameDef nameDef = st.lookup(identExpr.getName());
    
            // Check if we are in a context where the identifier could be implicitly declared
            // We use arg to pass the context information. It can be a String representing the context.
            if (nameDef == null && arg != null && arg.equals("PixelSelectorContext")) {
                nameDef = new SyntheticNameDef(identExpr.getName());  // Implicit type assumed to be INT
                st.insert(nameDef);
                identExpr.setNameDef(nameDef);
                identExpr.setType(Type.INT);
                LOGGER.info("Implicitly declared identifier: " + identExpr.getName());
                return Type.INT;
            } else if (nameDef == null) {
                throw new TypeCheckException("Undeclared identifier: " + identExpr.getName());
            }
    
            // Set the NameDef and Type of the IdentExpr
            identExpr.setNameDef(nameDef);
            Type type = nameDef.getType();
            identExpr.setType(type);
    
            LOGGER.info("Successfully processed visitIdentExpr");
            return type;
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitIdentExpr: " + e.getMessage());
            throw e;
        } finally {
            LOGGER.info("Leaving visitIdentExpr");
        }
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitIfStatement");

        try {
            // Retrieve the list of guarded blocks in the if statement
            List<GuardedBlock> guardedBlocks = ifStatement.getGuardedBlocks();

            // Loop through each guarded block
            for (GuardedBlock guardedBlock : guardedBlocks) {
                // Visit and type check the guard expression
                Expr guardExpr = guardedBlock.getGuard();
                Type guardType = (Type) guardExpr.visit(this, arg);

                if (guardType != Type.BOOLEAN) {
                    throw new TypeCheckException("The guard expression in a GuardedBlock must be of type BOOLEAN");
                }

                // Visit the block within the guarded block
                Block block = guardedBlock.getBlock();
                block.visit(this, arg);
            }

            LOGGER.info("Successfully processed visitIfStatement");
            return null; // IfStatement doesn't have a type

        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitIfStatement: " + e.getMessage());
            throw e;

        } finally {
            LOGGER.info("Leaving visitIfStatement");
        }
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitLValue");
    
        try {
            // Lookup the NameDef of this LValue from the symbol table
            NameDef nd = st.lookup(lValue.getName());
            if (nd == null) {
                throw new TypeCheckException("The identifier " + lValue.getName() + " has not been declared");
            }
            lValue.setNameDef(nd);
    
            Type varType = nd.getType();
    
            // Check the pixel selector and channel selector
            PixelSelector ps = lValue.getPixelSelector();
            ChannelSelector cs = lValue.getChannelSelector();
    
            if (ps != null) {
                ps.visit(this, true);  // True indicates we are in an LValue context
            }
    
            // Conditions for PixelSelector and ChannelSelector
    
            if (ps != null && varType != Type.IMAGE) {
                throw new TypeCheckException("PixelSelector is only valid for IMAGE type");
            }
    
            if (cs != null && (varType != Type.IMAGE && varType != Type.PIXEL)) {
                throw new TypeCheckException("ChannelSelector is only valid for IMAGE or PIXEL type");
            }
    
            // Infer the LValue type
            Type inferredType = inferLValueType(varType, ps, cs);
            lValue.setType(inferredType);
    
            LOGGER.info("Successfully processed visitLValue with inferredType: " + inferredType);
            return inferredType;
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitLValue: " + e.getMessage());
            throw e;
        } finally {
            LOGGER.info("Leaving visitLValue");
        }
    }
    

    private Type inferLValueType(Type varType, PixelSelector ps, ChannelSelector cs) throws TypeCheckException {
        if (ps == null && cs == null) {
            return varType;
        }
        if (ps != null && cs == null) {
            return Type.PIXEL;
        }
        if (ps != null && cs != null) {
            return Type.INT;
        }
        if (ps == null && cs != null) {
            if (varType == Type.IMAGE) {
                return Type.IMAGE;
            }
            if (varType == Type.PIXEL) {
                return Type.INT;
            }
        }
        throw new TypeCheckException("Unable to infer LValue type");
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        Type type = null;  // Initialize to a default value
        if(nameDef.getDimension() != null){
            nameDef.getDimension().visit(this, arg);
        }
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
        LOGGER.info("Entering visitPixelSelector");
    
        boolean scopeEntered = false;
    
        try {
            boolean inLValueContext = (Boolean) arg;
    
            if (inLValueContext) {
                if (st.lookup("x") == null) {
                    SyntheticNameDef nameX = new SyntheticNameDef("x");
                    st.enterScope();
                    st.insert(nameX);
                    scopeEntered = true;
                    LOGGER.info("Implicitly declared x");
                }
                if (st.lookup("y") == null) {
                    SyntheticNameDef nameY = new SyntheticNameDef("y");
                    if (!scopeEntered) {
                        st.enterScope();
                    }
                    st.insert(nameY);
                    scopeEntered = true;
                    LOGGER.info("Implicitly declared y");
                }
            }

            Type typeX = (Type) pixelSelector.xExpr().visit(this, "PixelSelectorContext");
            Type typeY = (Type) pixelSelector.yExpr().visit(this, "PixelSelectorContext");

    
            if (typeX != Type.INT || typeY != Type.INT) {
                throw new TypeCheckException("Expected type INT for component expressions");
            }
    
            LOGGER.info("Successfully processed visitPixelSelector");
            return Type.INT;
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitPixelSelector: " + e.getMessage());
            throw e;
        } finally {
          
            if (scopeEntered) {
                st.leaveScope();
            }
            LOGGER.info("Leaving visitPixelSelector");
        }
    }
    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        postfixExpr.primary().setType((Type) postfixExpr.primary().visit(this, arg));
        Object pixelVisitResult = postfixExpr.pixel().visit(this, false);
        if (!(pixelVisitResult instanceof Type)) {
            throw new TypeCheckException("Expected Type from PixelSelector visit");
        }
        Type pixelType = (Type) pixelVisitResult;
        postfixExpr.setType(inferPostfixExprType(postfixExpr.primary().getType(), postfixExpr.pixel(), postfixExpr.channel()));
        return postfixExpr.getType();
    }

    private Type inferPostfixExprType(Type prim, PixelSelector pix, ChannelSelector chan) throws TypeCheckException {
        boolean pixNull = (pix == null);
        boolean chanNull = (chan == null);
        switch(prim){
            case IMAGE -> {
                if(pixNull && chanNull){
                    return prim;
                    // both null case
                } else {
                    //requires at least one or both to be null
                    if(!pixNull){
                        //checks if pix is not null
                        if(!chanNull){
                            //checks if chan is not null
                            return INT;
                            //neither null case
                        }
                        return PIXEL;
                        //chan null case
                    }
                    return IMAGE;
                    //pix null case
                }
            }
            case PIXEL -> {
                if(pixNull && chanNull){
                    return prim;
                    // both null case
                } else {
                    if(!chanNull){
                        return INT;
                        // just chan null case
                    }
                    throw new TypeCheckException("invalid combination of parameters, likely pixelSelector is null when it shouldn't be");
                    // just pix null case
                }
            }
            default -> {
                if(pixNull && chanNull){
                    return prim;
                    // both null case
                }
                throw new TypeCheckException("invalid combination of parameters");
                // any other case
            }
        }
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitProgram");
    
        Type type = Type.kind2type(program.getTypeToken().kind());
        program.setType(type);  // Setting the type attribute for Program
        st.enterScope();  // Entering a new scope in the symbol table
        List<NameDef> params = program.getParams();
        for (NameDef param : params) {
            param.visit(this, arg);  // Visiting each NameDef child node
        }
        program.getBlock().visit(this, type);  // Visiting the Block child node, passing the program type
        st.leaveScope();  // Leaving the scope in the symbol table
    
        LOGGER.info("Leaving visitProgram");
        return type;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering visitReturnStatement");
    
        try {
            // Visit the expression in ReturnStatement to get its type
            Type exprType = (Type) returnStatement.getE().visit(this, arg);
    
            // Check if the type of the expression matches the type of the parent program (which is passed as arg)
            if (exprType == arg) {
                // Types match, so we can proceed
                LOGGER.info("Successfully processed visitReturnStatement: Type matches parent program.");
                return exprType;
            } else {
                // Types do not match, so we throw an exception
                String errorMsg = "Mismatched types: Expected " + arg + " but found " + exprType;
                LOGGER.severe("TypeCheckException in visitReturnStatement: " + errorMsg);
                throw new TypeCheckException(errorMsg);
            }
        } catch (TypeCheckException e) {
            LOGGER.severe("TypeCheckException in visitReturnStatement: " + e.getMessage());
            throw e;
        } finally {
            LOGGER.info("Leaving visitReturnStatement");
        }
    }


    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        stringLitExpr.setType(STRING);
        return STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        unaryExpr.getExpr().visit(this, arg);
        Kind opKind = unaryExpr.getOp();
        switch(unaryExpr.getExpr().getType()){
            case BOOLEAN -> {
                if(opKind==BANG){
                    unaryExpr.setType(BOOLEAN);
                    boolean val = !Boolean.parseBoolean(unaryExpr.getExpr().firstToken().text());
                    return BOOLEAN;
                }
                throw new UnsupportedOperationException("Invalid type for unaryExpr with BOOLEAN Expr type");
            }
            case INT -> {
                if(opKind==MINUS) {
                    unaryExpr.setType(INT);
                    int val = - Integer.parseInt(unaryExpr.getExpr().firstToken().text());
                    return INT;
                }
                throw new UnsupportedOperationException("Invalid type for unaryExpr with INT Expr type");
            }
            case IMAGE -> {
                switch(opKind){
                    case RES_width -> {
                        unaryExpr.setType(INT);
                        int val = Integer.parseInt(unaryExpr.getExpr().firstToken().text());
                        return INT;
                    }
                    case RES_height -> {
                        unaryExpr.setType(INT);
                        int val = Integer.parseInt(unaryExpr.getExpr().firstToken.text());
                        return INT;
                        //not actually sure if this is how height/width are supposed to work
                    }
                    default-> throw new UnsupportedOperationException("Invalid type for unaryExpr with IMAGE Expr type");
                }
            }
        };
        return null;
    }
    //If program, namedef, declaration, or part of expr, return type
    // (conditionalExpr, BinaryExpr, unarOp Expr, PostFixExpr[check], PrimaryExpr,
    // StringLitExpr, NumLitExpr[check], IdentExpr, ConstExpr[check], BooleanLitExpr[check], ExpandedPixelExpr)
    //otherwise, return the object type
    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        writeStatement.getExpr().visit(this, arg);
        return writeStatement;
    }
    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        booleanLitExpr.setType(BOOLEAN);
        return BOOLEAN;
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        LOGGER.info("Entering const expression");
        if (constExpr.getName().equals("Z")){
            constExpr.setType(INT);
            return INT;
        }
        constExpr.setType(PIXEL);
        return PIXEL;
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