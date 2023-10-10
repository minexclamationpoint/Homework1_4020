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

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

import static edu.ufl.cise.cop4020fa23.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser implements IParser {
	
	final ILexer lexer;
	private IToken t;

	public Parser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
	}


	@Override
	public AST parse() throws PLCCompilerException {
        return program();
	}

	// TODO: create a copy of this file and rename that copy to Parser.java
	// TODO: import modified version of componentfactory, new AST class, Parser.java and ParserTest_starter.java
	// TODO: complete grammar
	private AST program() throws PLCCompilerException {
		IToken firstToken = t;
		AST e = null;
		IToken type = Type();
		IToken name = null;
		if(t.kind() == IDENT){
			name = t;
			t = lexer.next();
		} else {
			throw new SyntaxException(t.sourceLocation(), "Expected 'IDENT' at " + t.sourceLocation());
		}
		if(t.kind() != LPAREN){
			throw new SyntaxException(t.sourceLocation(), "Expected 'LPAREN' at " + t.sourceLocation());
		}
		t = lexer.next();
		List<NameDef> params = ParamList();
		if(t.kind() != RPAREN){
			throw new SyntaxException(t.sourceLocation(), "Unmatched parentheses");
		}
		Block block = Block();
		e = new Program(firstToken, type, name, params, block);
		return e;
	}

	private Block Block() throws PLCCompilerException {
		IToken firstToken = t;
		if(t.kind() != BLOCK_OPEN){
			throw new SyntaxException(t.sourceLocation(), "Expected 'BLOCK_OPEN' at " + t.sourceLocation());
		}
		List<Block.BlockElem> elems = new ArrayList<>();
		AST newElem = null;
        do {
			switch (t.kind()) {
				case IDENT -> {
					newElem = Statement();
				} case RES_image, RES_pixel, RES_int, RES_string, RES_void, RES_boolean -> {
					newElem = Declaration();
				} default -> {
					continue;
				}
			}
			elems.add((Block.BlockElem) newElem);
        } while (t.kind() == SEMI);
		if(t.kind() != BLOCK_CLOSE){
			throw new SyntaxException(t.sourceLocation(), "Unmatched block tokens");
		}
        return new Block(firstToken, elems);
	}

	private List<NameDef> ParamList() throws PLCCompilerException {
		//image | pixel | int | string | void | boolean
		List<AST> list = new ArrayList<>();
		if(t.kind() == RES_image || t.kind() == RES_pixel || t.kind() == RES_int || t.kind() == RES_string || t.kind() == RES_void || t.kind() == RES_boolean){
			list.add(NameDef());
			if(t.kind() == COMMA){
				t = lexer.next();
			} else {
				throw new SyntaxException(t.sourceLocation(), "Expected ',' token at " + t.sourceLocation());
			}
		} else {
			return null;
		}
		throw new UnsupportedOperationException();
	}

	private AST NameDef() throws PLCCompilerException {
		throw new UnsupportedOperationException();
	}

	private IToken Type() throws PLCCompilerException {
		throw new UnsupportedOperationException();
	}

	private Expr Declaration() throws PLCCompilerException {
		throw new UnsupportedOperationException();
	}
	//TODO: functions above this comment are unfinished
	// TODO: see if the passing of argument (Expr e) is even needed. I don't think it's actually necessary

	private Expr expr() throws PLCCompilerException {
		// helper function for recursion with expr(), only called within the ExpressionParser() class
		Expr e = null;
		if (t.kind() == QUESTION) {
			t = lexer.next();
			e = ConditionalExpr();
		} else {
			e = LogicalOrExpr();
		}
		return e;
	}

	private Expr ConditionalExpr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e1 = expr();
		if (t.kind() == RARROW) {
			t = lexer.next();
		} else {
			throw new SyntaxException("Expected '->' token at " + t.sourceLocation()); // replace with specific error
		}
		Expr e2 = expr();
		if (t.kind() == COMMA) {
			t = lexer.next();
		} else {
			throw new SyntaxException("Expected ',' token at " + t.sourceLocation()); // replace with more specific error
		}
		Expr e3 = expr();
		return new ConditionalExpr(firstToken, e1, e2, e3);
	}

	private Expr LogicalOrExpr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e1 = null;
		Expr e2 = null;
		e1 = LogicalAndExpr();
		while (t.kind() == BITOR || t.kind() == OR) {
			IToken op = t;
			t = lexer.next();
			e2 = LogicalAndExpr();
			e1 = new BinaryExpr(firstToken, e1, op, e2);
		}
		return e1;
	}

	private Expr LogicalAndExpr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e1 = null;
		Expr e2 = null;
		e1 = ComparisonExpr();
		while (t.kind() == BITAND || t.kind() == AND) {
			IToken op = t;
			t = lexer.next();
			e2 = ComparisonExpr();
			e1 = new BinaryExpr(firstToken, e1, op, e2);
		}
		return e1;
	}

	private Expr ComparisonExpr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e1 = null;
		Expr e2 = null;
		e1 = PowExpr();
		while(t.kind() == LT || t.kind() == GT || t.kind() == EQ || t.kind() == LE || t.kind() == GE){
			IToken op = t;
			t = lexer.next();
			e2 = PowExpr();
			e1 = new BinaryExpr(firstToken, e1, op, e2);
		}
		return e1;
	}

	private Expr PowExpr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e = AdditiveExpr();
		if (t.kind() == EXP) {
			IToken op = t;
			t = lexer.next();
			e = new BinaryExpr(firstToken, e, op, PowExpr());
		}
		return e;
	}

	private Expr AdditiveExpr() throws PLCCompilerException {
		// everything that uses BinaryExpr() should function roughly identically, just with different conditionals
		IToken firstToken = t;
		Expr e1 = null;
		Expr e2 = null;
		e1 = MultiplicativeExpr();
		while (t.kind() == PLUS || t.kind() == MINUS) {
			IToken op = t;
			t = lexer.next();
			e2 = MultiplicativeExpr();
			e1 = new BinaryExpr(firstToken, e1, op, e2);
		}
		return e1;
	}

	private Expr MultiplicativeExpr() throws PLCCompilerException {
		// might be a bad way of handling binary expressions?
		IToken firstToken = t;
		Expr e1 = null;
		Expr e2 = null;
		e1 = UnaryExpr();
		while (t.kind() == TIMES || t.kind() == DIV || t.kind() == MOD) {
			IToken op = t;
			t = lexer.next();
			e2 = UnaryExpr();
			e1 = new BinaryExpr(firstToken, e1, op, e2);
		}
		return e1;
	}

	private Expr UnaryExpr() throws PLCCompilerException {
		if (t.kind() == BANG || t.kind() == MINUS || t.kind() == RES_width || t.kind() == RES_height) {
			IToken op = t;
			t = lexer.next();
			Expr e = UnaryExpr();
			return new UnaryExpr(op, op, e);
		} else {
			return PostfixExpr();
		}

	}

	private Expr PostfixExpr() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e = PrimaryExpr();
		PixelSelector p = null; // unsure if this is best practice
		ChannelSelector s = null;
		if (t.kind() == LSQUARE) {
			p = PixelSelector();
		}
		if (t.kind() == COLON) {
			s = ChannelSelector();
		}
		if (p != null || s != null) {
			return new PostfixExpr(firstToken, e, p, s);
		} else {
			return e;
		}
	}

	private Expr PrimaryExpr() throws PLCCompilerException {
		Expr e = null;
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
				e = new IdentExpr(t);
				t = lexer.next();
			}
			case CONST -> {
				e = new ConstExpr(t);
				t = lexer.next();
			}
			case LPAREN -> {
				// LPAREN is consumed and not counted as a part of the grammar, not sure if doing so is correct
				t = lexer.next();
				e = expr(); // should I call expr(e) here or just expr()? can't quite tell
				if (t.kind() != RPAREN) {
					throw new SyntaxException("Unmatched parentheses");
				} else {
					t = lexer.next();
				}
			}
			case LSQUARE -> {
				e = ExpandedPixelSelector();
			}
			default -> {
				throw new SyntaxException("Unexpected token encountered: " + t.kind());
			}
		}
		return e;
	}

	private ChannelSelector ChannelSelector() throws PLCCompilerException { // oh the non-ll(1) of it all
		IToken firstToken = t;
		IToken color = lexer.next(); // I really don't know what to do to "fix" the grammar so it isn't ll(1). this should work though
		ChannelSelector newSelector = null;
		if (color.kind() == RES_blue || color.kind() == RES_green || color.kind() == RES_red) {
			newSelector = new ChannelSelector(firstToken, color); //ChannelSelector extends AST, not Expr. Idk if this is a mistake or not?
			t = color;
			return newSelector;
		} else {
			throw new SyntaxException(color.sourceLocation(), "Expected a color channel token (RES_blue, RES_green, or RES_red)"); // replace with more specific error
		}
	}

	private PixelSelector PixelSelector() throws PLCCompilerException {
		IToken firstToken = t;
		t = lexer.next();
		Expr eX = expr();
		Expr eY;
		if (t.kind() == COMMA) {
			t = lexer.next();
			eY = expr();
		} else {
			throw new SyntaxException("Expected ',' token at " + t.sourceLocation()); // replace with more specific error
		}
		if (t.kind() != RSQUARE) {
			throw new SyntaxException("Expected ']' token at " + t.sourceLocation()); // replace with specific error
		} else {
			t = lexer.next();
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
		if (t.kind() == COMMA) {
			t = lexer.next();
			eG = expr();
		} else {
			throw new SyntaxException(t.sourceLocation(), "Expected ',' token after the red component expression.");
		}
		if (t.kind() == COMMA) {
			t = lexer.next();
			eB = expr();
		} else {
			throw new SyntaxException(t.sourceLocation(), "Expected ',' token after the green component expression.");
		}
		t = lexer.next();
		if (t.kind() != RSQUARE) {
			throw new SyntaxException(t.sourceLocation(), "Expected ']' token to close the pixel selector expression.");
		}
		return new ExpandedPixelExpr(firstToken, eR, eG, eB);
	}
	//TODO: functions below this comment are unfinished
	private Dimension Dimension() throws PLCCompilerException {
		// This is copied directly from PixelSelector's code, may not be exactly what is needed
		IToken firstToken = t;
		t = lexer.next();
		Expr eX = expr();
		Expr eY;
		if (t.kind() == COMMA) {
			t = lexer.next();
			eY = expr();
		} else {
			throw new SyntaxException("Expected ',' token at " + t.sourceLocation()); // replace with more specific error
		}
		if (t.kind() != RSQUARE) {
			throw new SyntaxException("Expected ']' token at " + t.sourceLocation()); // replace with specific error
		} else {
			t = lexer.next();
		}
		return new Dimension(firstToken, eX, eY);
	}
	private AST Statement() throws PLCCompilerException {
		throw new UnsupportedOperationException();
	}
	private Expr GuardedBlock() throws PLCCompilerException {
		throw new UnsupportedOperationException();
	}
	private Expr BlockStatement() throws PLCCompilerException {
		throw new UnsupportedOperationException();
	}

}
