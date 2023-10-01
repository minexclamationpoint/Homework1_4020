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

import java.util.Objects;

import edu.ufl.cise.cop4020fa23.IToken;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

public class PostfixExpr extends Expr {

	final Expr primary;
	final PixelSelector pixel;
	final ChannelSelector channel;

	/**
	 * @param firstToken
	 * @param primary
	 * @param pixel
	 * @param channel
	 */
	public PostfixExpr(IToken firstToken, Expr primary, PixelSelector pixel, ChannelSelector channel) {
		super(firstToken);
		this.primary = primary;
		this.pixel = pixel;
		this.channel = channel;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitPostfixExpr(this, arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(channel, pixel, primary);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj) || (getClass() != obj.getClass()))
			return false;
		PostfixExpr other = (PostfixExpr) obj;
		return Objects.equals(channel, other.channel) && Objects.equals(pixel, other.pixel)
				&& Objects.equals(primary, other.primary);
	}

	@Override
	public String toString() {
		return "PostfixExpr [primary=" + primary + ",\n   pixel=" + pixel + ",\n   channel=" + channel + "]";
	}

	public Expr primary() {
		return primary;
	}

	public PixelSelector pixel() {
		return pixel;
	}

	public ChannelSelector channel() {
		return channel;
	}

}
