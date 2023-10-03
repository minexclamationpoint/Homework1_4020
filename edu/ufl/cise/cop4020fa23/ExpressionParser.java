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
	public ExpressionParser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		t = lexer.next();
	}
	@Override
	public AST parse() throws PLCCompilerException {
		// idk if this might need changing....?
		// EDIT: needs changing to properly handle EOF I think, but also idk if more is needed as well.
		return expr();
	}

	// TODO: Implement proper error statements (Arsen I'm talking to you)
	// TODO: Test and revise (Arsen I'm still talking to you (lol))
	// TODO: check if the parser properly handles EOF
	private Expr expr() throws PLCCompilerException {
		// Starter statement, called outside of
		Expr e = null; // not sure if this is best practice
		if(t.kind() == QUESTION){
			t = lexer.next();
			e = ConditionalExpr(e);
		} else {
			e = LogicalOrExpr(e);
		}
		return e;
	}

	private Expr expr(Expr e) throws PLCCompilerException {
		// helper function for recursion with expr(), only called within the ExpressionParser() class
		if(t.kind() == QUESTION){
			t = lexer.next();
			e = ConditionalExpr(e);
		} else {
			e = LogicalOrExpr(e);
		}
		return e;
	}

	private Expr ConditionalExpr(Expr e) throws PLCCompilerException {
		// slightly different syntax as the rest of the program, shouldn't be too much of a problem
		Expr e1 = expr(e);
		if(t.kind() == RARROW){
			t = lexer.next();
		} else {
			throw new UnsupportedOperationException("THE PARSER ERRORS HAVE NOT BEEN IMPLEMENTED YET"); // replace with specific error
		}
		Expr e2 = expr(e);
		if(t.kind() == COMMA){
			t = lexer.next();
		} else {
			throw new UnsupportedOperationException("THE PARSER ERRORS HAVE NOT BEEN IMPLEMENTED YET"); // replace with more specific error
		}
		Expr e3 = expr(e);
		return new ConditionalExpr(e.firstToken, e1, e2, e3);
	}
	private Expr LogicalOrExpr(Expr e) throws PLCCompilerException {
		Expr e1 = LogicalAndExpr(e);
		if(t.kind() == BITOR || t.kind() == OR){
			IToken op = t;
			t = lexer.next();
			e = new BinaryExpr(e.firstToken, e1, op, LogicalOrExpr(e));
		} else {
			return e1;
		}
		return e;
	}
	private Expr LogicalAndExpr(Expr e) throws PLCCompilerException {
		Expr e1 = ComparisonExpr(e);
		if(t.kind() == BITAND || t.kind() == AND){
			IToken op = t;
			t = lexer.next();
			e = new BinaryExpr(e.firstToken, e1, op, ComparisonExpr(e));
		} else {
			return e1;
		}
		return e;
	}
	private Expr ComparisonExpr(Expr e) throws PLCCompilerException {
		Expr e1 = PowExpr(e);
		if(t.kind() == LT || t.kind() == GT|| t.kind() == EQ|| t.kind() == LE || t.kind() == GE){
			IToken op = t;
			t = lexer.next();
			e = new BinaryExpr(e.firstToken, e1, op, PowExpr(e));
		} else {
			return e1;
		}
		return e;
	}
	private Expr PowExpr(Expr e) throws PLCCompilerException {
		e = AdditiveExpr(e);
		if(t.kind() == EXP){
			IToken op = t;
			t = lexer.next();
			e = new BinaryExpr(e.firstToken, e, op, PowExpr(e));
		}
		return e;
	}
	private Expr AdditiveExpr(Expr e) throws PLCCompilerException {
		// everything that uses BinaryExpr() should function roughly identically, just with different conditionals
		Expr e1 = MultiplicativeExpr(e);
		if(t.kind() == PLUS || t.kind() == MINUS){
			IToken op = t;
			t = lexer.next();
			e = new BinaryExpr(e.firstToken, e1, op, AdditiveExpr(e));
		} else {
			return e1;
		}
		return e;
	}
	private Expr MultiplicativeExpr(Expr e) throws PLCCompilerException {
		// might be a bad way of handling binary expressions?
		Expr e1 = UnaryExpr(e);
		if (t.kind() == TIMES || t.kind() == DIV || t.kind() == MOD) {
			IToken op = t;
			t = lexer.next();
			e = new BinaryExpr(e.firstToken, e1, op, MultiplicativeExpr(e));
		} else {
			return e1;
		}
		return e;
	}
	private Expr UnaryExpr(Expr e) throws PLCCompilerException {
		if (t.kind() == BANG || t.kind() == MINUS || t.kind() == RES_width || t.kind() == RES_height) {
			IToken op = t;
			t = lexer.next();
			e = UnaryExpr(e);
			return new UnaryExpr(op, op, e);
		} else {
			return PostfixExpr(e);
		}

	}
	private Expr PostfixExpr(Expr e) throws PLCCompilerException {
		e = PrimaryExpr(e);
		PixelSelector p = null; // unsure if this is best practice
		ChannelSelector s = null;
		if(t.kind() == LSQUARE){
			p = PixelSelector();
		}
		if(t.kind() == COLON){
			s = ChannelSelector();
		}
		if(p != null && s != null){
			return new PostfixExpr(e.firstToken, e, p, s);
		} else {
			return e;
		}
	}
	private Expr PrimaryExpr(Expr e) throws PLCCompilerException {
		switch (t.kind()) {
			case STRING_LIT -> {
				e = new StringLitExpr(t);
				t = lexer.next();
			}
			case NUM_LIT -> {
				e = new NumLitExpr(t);
				t = lexer.next();
			}
			case BOOLEAN_LIT -> {
				e = new BooleanLitExpr(t);
				t = lexer.next();
			}
			case IDENT -> {
				e= new IdentExpr(t);
				t = lexer.next();
			}
			case CONST -> {
				e = new ConstExpr(t);
				t = lexer.next();
			}
			case LPAREN -> {
				// LPAREN is consumed and not counted as a part of the grammar, not sure if doing so is correct
				t = lexer.next();
				e = expr(e); // should I call expr(e) here or just expr()? can't quite tell
			}
			case RPAREN -> {
				t = lexer.next();
			}
			case LSQUARE -> {
				e = ExpandedPixelSelector();
			}
			default -> {
				throw new UnsupportedOperationException("THE PARSER ERRORS HAVE NOT BEEN IMPLEMENTED YET"); // replace with more specific error
			}
		};
		return e;
	}
	private ChannelSelector ChannelSelector() throws PLCCompilerException { // oh the non-ll(1) of it all
		IToken color = lexer.next(); // I really don't know what to do to "fix" the grammar so it isn't ll(1). this should work though
		ChannelSelector newSelector = null;
		if(color.kind() == RES_blue || color.kind() == RES_green || color.kind() == RES_red) {
			newSelector = new ChannelSelector(t, color); //ChannelSelector extends AST, not Expr. Idk if this is a mistake or not?
			t = color;
			return newSelector;
		} else {
			throw new UnsupportedOperationException("THE PARSER ERRORS HAVE NOT BEEN IMPLEMENTED YET"); // replace with more specific error
		}
	}
	private PixelSelector PixelSelector() throws PLCCompilerException{
		IToken firstToken = t;
		t = lexer.next();
		Expr eX = expr();
		Expr eY;
		if(t.kind() == COMMA){
			t = lexer.next();
			eY = expr();
		} else {
			throw new UnsupportedOperationException("THE PARSER ERRORS HAVE NOT BEEN IMPLEMENTED YET"); // replace with more specific error
		}
		t = lexer.next();
		if(t.kind() != RSQUARE){
			throw new UnsupportedOperationException("THE PARSER ERRORS HAVE NOT BEEN IMPLEMENTED YET"); // replace with more specific error
		}
		return new PixelSelector(firstToken, eX, eY);
	}
	// may have unnecessary lexer.next() calls
	private Expr ExpandedPixelSelector() throws PLCCompilerException {
		IToken firstToken = t;
		t = lexer.next();
		Expr eR = expr();
		Expr eG;
		Expr eB;
		if(t.kind() == COMMA){
			t = lexer.next();
			eG = expr();
		} else {
			throw new UnsupportedOperationException("THE PARSER ERRORS HAVE NOT BEEN IMPLEMENTED YET"); // replace with more specific error
		}
		if(t.kind() == COMMA){
			t = lexer.next();
			eB = expr();
		} else {
			throw new UnsupportedOperationException("THE PARSER ERRORS HAVE NOT BEEN IMPLEMENTED YET"); // replace with more specific error
		}
		t = lexer.next();
		if(t.kind() != RSQUARE) {
			throw new UnsupportedOperationException("THE PARSER ERRORS HAVE NOT BEEN IMPLEMENTED YET"); // replace with more specific error
		}
		return new ExpandedPixelExpr(firstToken, eR, eG, eB);
	}
}



