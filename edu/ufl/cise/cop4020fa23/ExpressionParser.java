/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */
package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.AND;
import static edu.ufl.cise.cop4020fa23.Kind.BANG;
import static edu.ufl.cise.cop4020fa23.Kind.BITAND;
import static edu.ufl.cise.cop4020fa23.Kind.BITOR;
import static edu.ufl.cise.cop4020fa23.Kind.COLON;
import static edu.ufl.cise.cop4020fa23.Kind.COMMA;
import static edu.ufl.cise.cop4020fa23.Kind.DIV;
import static edu.ufl.cise.cop4020fa23.Kind.EOF;
import static edu.ufl.cise.cop4020fa23.Kind.EQ;
import static edu.ufl.cise.cop4020fa23.Kind.EXP;
import static edu.ufl.cise.cop4020fa23.Kind.GE;
import static edu.ufl.cise.cop4020fa23.Kind.GT;
import static edu.ufl.cise.cop4020fa23.Kind.IDENT;
import static edu.ufl.cise.cop4020fa23.Kind.LE;
import static edu.ufl.cise.cop4020fa23.Kind.LPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.LSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.LT;
import static edu.ufl.cise.cop4020fa23.Kind.MINUS;
import static edu.ufl.cise.cop4020fa23.Kind.MOD;
import static edu.ufl.cise.cop4020fa23.Kind.NUM_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.OR;
import static edu.ufl.cise.cop4020fa23.Kind.PLUS;
import static edu.ufl.cise.cop4020fa23.Kind.QUESTION;
import static edu.ufl.cise.cop4020fa23.Kind.RARROW;
import static edu.ufl.cise.cop4020fa23.Kind.RES_blue;
import static edu.ufl.cise.cop4020fa23.Kind.RES_green;
import static edu.ufl.cise.cop4020fa23.Kind.RES_height;
import static edu.ufl.cise.cop4020fa23.Kind.RES_red;
import static edu.ufl.cise.cop4020fa23.Kind.RES_width;
import static edu.ufl.cise.cop4020fa23.Kind.RPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.RSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.TIMES;
import static edu.ufl.cise.cop4020fa23.Kind.CONST;

import java.util.Arrays;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
/**
Expr::=  ConditionalExpr | LogicalOrExpr    
ConditionalExpr ::=  ?  Expr  :  Expr  :  Expr 
LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*
LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*
PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*
MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)*
UnaryExpr ::=  ( ! | - | length | width) UnaryExpr  |  UnaryExprPostfix
UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
PrimaryExpr ::=STRING_LIT | NUM_LIT |  IDENT | ( Expr ) | Z 
    ExpandedPixel  
ChannelSelector ::= : red | : green | : blue
PixelSelector  ::= [ Expr , Expr ]
ExpandedPixel ::= [ Expr , Expr , Expr ]
Dimension  ::=  [ Expr , Expr ]                         

 */

public class ExpressionParser implements IParser {
	
	final ILexer lexer;
	private IToken t;

	/**
	 * @param lexer
	 * @throws LexicalException 
	 */
	// Expression parser should recognize a legal expression and return its AST. If there are tokens remaining, they should be ignored
	// For example: "a + b c" should return BinaryExpr representing a + b and ignore the ident representing c.
	// This class is implemented incompletely and incorrectly. It is the only class that needs changing.
	public ExpressionParser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		t = lexer.next();
	}
	@Override
	public AST parse() throws PLCCompilerException {
		Expr e = expr();
		// create match set based on the expected next token
		// TODO: convert recursion from psuedocode into actual code, create match() function?
		return e;
	}


	// psuedocode of recursive functions
	// TODO: resolve clashing methods, implement the return chain so the recursion has an actual output
	// TODO: figure out how information is passed up through recursion
	private Expr expr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e;
		if(t.kind() == QUESTION){
			t = lexer.next();
			e = ConditionalExpr(firstToken, e);
		} else {
			e = LogicalOrExpr(firstToken, e);
		}
		return e;
	}

	private Expr expr(IToken firstToken, Expr e) throws PLCCompilerException {
		if(t.kind() == QUESTION){
			t = lexer.next();
			e = ConditionalExpr(firstToken, e);
		} else {
			e = LogicalOrExpr(firstToken, e);
		}
		return e;
	}

	private Expr ConditionalExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		e = expr(firstToken, e);
		if(t.kind() == RARROW){
			t = lexer.next();
		} else {
			throw error(); // replace with specific error
		}
		e = expr(firstToken, e);
		if(t.kind() == COMMA){
			t = lexer.next();
		} else {
			throw error(); // replace with more specific error
		}
		e = expr(firstToken, e);
		return e;
	}
	private Expr LogicalOrExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		e = LogicalAndExpr(firstToken, e);
		while(t.kind() == BITOR || t.kind() == OR){
			t = lexer.next();
			e = LogicalAndExpr(firstToken, e);
		}
		return e;
	}
	private Expr LogicalAndExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		e = ComparisonExpr(firstToken, e);
		while(t.kind() == BITAND || t.kind() == AND){
			t = lexer.next();
			e = LogicalAndExpr(firstToken, e);
		}
		return e;
	}
	private Expr ComparisonExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		PowExpr(firstToken, e);
		while(t.kind() == LT || t.kind() == GT|| t.kind() == EQ|| t.kind() == LE || t.kind() == GE){
			t = lexer.next();
			PowExpr(firstToken, e);
		}
		return e;
	}
	private Expr PowExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		e = AdditiveExpr(firstToken, e);
		if(t.kind() == EXP){
			t = lexer.next();
			PowExpr(firstToken, e);
		}
		return e;
	}
	private Expr AdditiveExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		e = MultiplicativeExpr(firstToken, e);
		while(t.kind() == PLUS || t.kind() == MINUS){
			t = lexer.next();
			e = MultiplicativeExpr(firstToken, e);
		}
		return e;
	}
	private Expr MultiplicativeExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		e = UnaryExpr(firstToken, e);
		while(t.kind() == TIMES || t.kind() == DIV || t.kind() == MOD){
			t = lexer.next();
			e = UnaryExpr(firstToken, e);
		}
		return e;
	}
	private Expr UnaryExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		if(t.kind() == BANG || t.kind() == MINUS || t.kind() == RES_width || t.kind() == RES_height){
			t = lexer.next();
			e = UnaryExpr(firstToken, e);
		} else {
			e = PostfixExpr(firstToken, e);
		}
		return e;
	}
	private Expr PostfixExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		e = PrimaryExpr(firstToken, e);
		t = lexer.next();
		if(t.kind() == LSQUARE){
			e = PixelSelector(firstToken, e);
		}
		if(t.kind() == COLON){
			e = ChannelSelector(firstToken, e);
		}
		return e;
	}
	private Expr PrimaryExpr(IToken firstToken, Expr e) throws PLCCompilerException {
		switch (t.kind()) {
			case STRING_LIT -> {
				e = new StringLitExpr(t);
			}
			case NUM_LIT -> {
				e = new NumLitExpr(t);
			}
			case BOOLEAN_LIT -> {
				e = new BooleanLitExpr(t);
			}
			case IDENT -> {
				e= new IdentExpr(t);
			}
			case CONST -> {
				e = new ConstExpr(t);
			}
			case LPAREN -> {
				t = lexer.next();
				expr();
			}
			case LSQUARE -> {
				ExpandedPixelSelector(firstToken, e);
			}
			default -> {
				error(); // replace with more specific error
			}
		};
		return e;
	}
	private Expr ChannelSelector(IToken firstToken, Expr e) throws PLCCompilerException { // oh the non-ll(1) of it all
		IToken color = lexer.next();
		e = new ChannelSelector(t, color); //ChannelSelector extends AST, not Expr. Idk if this is a mistake or not?
		t = color;
		return e;
	}
	private Expr PixelSelector(IToken firstToken, Expr e) throws PLCCompilerException{
		t = lexer.next();
		e = expr(firstToken, e);
		if(t.kind() == COMMA){
			t = lexer.next();
			e = expr(firstToken, e);
		} else {
			error(); // replace with more specific error
		}
		t = lexer.next();
		if(t.kind() != RSQUARE){
			error(); // replace with more specific error
		}
		return e;
	}
	private Expr ExpandedPixelSelector(IToken firstToken, Expr e) throws PLCCompilerException {
		t = lexer.next();
		e = expr(firstToken, e);
		if(t.kind() == COMMA){
			t = lexer.next();
			e = expr(firstToken, e);
		} else {
			error(); // replace with more specific error
		}
		if(t.kind() == COMMA){
			t = lexer.next();
			e = expr(firstToken, e);
		} else {
			error(); // replace with more specific error
		}
		t = lexer.next();
		if(t.kind() != RSQUARE){
			error(); // replace with more specific error
		}
		return e;
	}
}



