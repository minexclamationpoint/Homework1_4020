/*Copyright 2023 by Beverly A Sanders
 *
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the
 * University of Florida during the fall semester 2023 as part of the course project.
 *
 * No other use is authorized.
 *
 * This code may not be posted on a public web site either during or after the course.
 */
package edu.ufl.cise.cop4020fa23.ast;

import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

public interface ASTVisitor {

	Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException;

	Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException;

	Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException;

	Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException;

	Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException;

	Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException;

	Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException;

	Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException;

	Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException;

	Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException;

	Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException;

	Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException;

}
