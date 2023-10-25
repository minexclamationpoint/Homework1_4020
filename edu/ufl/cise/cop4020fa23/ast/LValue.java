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

import edu.ufl.cise.cop4020fa23.IToken;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

/**
 * 
 */
public class LValue extends AST {

	final IToken nameToken;
	final PixelSelector pixelSelector;
	final ChannelSelector channelSelector;
	NameDef nameDef;

	Type inferredType;
	/**
	 * @param firstToken
	 * @param name
	 * @param pixelSelector
	 * @param channelSelector
	 */
	public LValue(IToken firstToken, IToken name, PixelSelector pixelSelector, ChannelSelector channelSelector) {
		super(firstToken);
		this.nameToken = name;
		this.pixelSelector = pixelSelector;
		this.channelSelector = channelSelector;
	}
	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitLValue(this, arg);
	}
	public IToken getNameToken() {
		return nameToken;
	}
	public String getName() {
		return nameToken.text();
	}
	public PixelSelector getPixelSelector() {
		return pixelSelector;
	}
	public ChannelSelector getChannelSelector() {
		return channelSelector;
	}
	
	public void setType(Type type) {
		inferredType = type;
		return;
	}
	
	public Type getType() {
		return inferredType;
	}

	public NameDef getNameDef() {
		return nameDef;
	}
	public void setNameDef(NameDef nameDef) {
		this.nameDef = nameDef;
	}

	/* precondition:  namdDef != null */
	public Type getVarType() {
		return nameDef.getType();
	}
	@Override
	public String toString() {
		return "LValue [nameToken=" + nameToken + ", pixelSelector=" + pixelSelector + ", channelSelector="
				+ channelSelector + ", nameDef=" + nameDef + ", inferredType=" + inferredType + "]";
	}
	
	
}
