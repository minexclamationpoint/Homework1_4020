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
		t = lexer.next();
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
			throw new SyntaxException(t.sourceLocation(), "Expected 'RPAREN' at " + t.sourceLocation());
		}
		t = lexer.next();
		Block block = Block();
		e = new Program(firstToken, type, name, params, block);
		return e;
	}
	// helpers
	private boolean isStartOfDeclaration(Kind kind) {
		return kind == RES_image || kind == RES_pixel || kind == RES_int || 
			   kind == RES_string || kind == RES_void || kind == RES_boolean;
	}

	private boolean isStartOfStatement(Kind kind) {
		return kind == IDENT || kind == RES_write || kind == RES_do || 
			   kind == RES_if || kind == BLOCK_OPEN;
	}
	
	

	private Block Block() throws PLCCompilerException {
		IToken firstToken = t;
		if (t.kind() != BLOCK_OPEN) {
			throw new SyntaxException(t.sourceLocation(), "Expected 'BLOCK_OPEN' at " + t.sourceLocation());
		}
		List<Block.BlockElem> elems = new ArrayList<>();
		AST newElem = null;
		t = lexer.next();
		while (t.kind() != BLOCK_CLOSE) {
			if (isStartOfDeclaration(t.kind())) {
				newElem = Declaration();
				elems.add((Block.BlockElem) newElem);
			} else if (isStartOfStatement(t.kind())) {
				newElem = Statement();
				
				elems.add((Block.BlockElem) newElem);
				
			
			} else {
				throw new SyntaxException(t.sourceLocation(), "Unexpected token in block: " + t.kind());
			}
			if (t.kind() != SEMI) {
				throw new SyntaxException(t.sourceLocation(), "Expected semicolon after Declaration or Statement" + t.kind());
			}
			t = lexer.next();  // Move past the SEMI token
			
			
		}
		if (t.kind() != BLOCK_CLOSE) {
			throw new SyntaxException(t.sourceLocation(), "Unmatched block tokens");
		}
		t = lexer.next();  // Move past the BLOCK_CLOSE token
		return new Block(firstToken, elems);
	}

	private List<NameDef> ParamList() throws PLCCompilerException {
		//image | pixel | int | string | void | boolean
		List<NameDef> list = new ArrayList<>();
		if(t.kind() == RES_image || t.kind() == RES_pixel || t.kind() == RES_int || t.kind() == RES_string || t.kind() == RES_void || t.kind() == RES_boolean){
			list.add(NameDef());
			while (t.kind() == COMMA){
				t = lexer.next();
				list.add(NameDef());
			}
        }
        return list;
    }

	private NameDef NameDef() throws PLCCompilerException {
		IToken firstToken = t;
		IToken type;
		Dimension dim = null;
		if(t.kind() == IDENT){
			type = t;
			t = lexer.next();
		} else {
			type = Type();
		}
		if(t.kind() == LSQUARE) {
			dim = Dimension();
		}
		if(t.kind() != IDENT){
			throw new SyntaxException(t.sourceLocation(), "Expected 'IDENT' token at " +t.sourceLocation());
		}
		IToken ident = t;
		t = lexer.next();
		return new NameDef(firstToken, type, dim, ident);
	}

	private IToken Type() throws PLCCompilerException {
		IToken type = null;
		if(t.kind() == RES_image || t.kind() == RES_pixel || t.kind() == RES_int || t.kind() ==  RES_string || t.kind() ==  RES_void || t.kind() == RES_boolean) {
			type = t;
			t = lexer.next();
			return type;
		}
		throw new SyntaxException(t.sourceLocation(), "Unexpected token encountered: " + t.kind());
	}

	private AST Declaration() throws PLCCompilerException {
		IToken firstToken = t;
		NameDef e1 = NameDef();
		Expr e2 = null;
		if(t.kind() == ASSIGN){
			t = lexer.next();
			e2 = expr();
		}
		// t = lexer.next(); fixes test case 1
		return new Declaration(firstToken, e1, e2);
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
			case LSQUARE -> e = ExpandedPixelSelector();
			default -> throw new SyntaxException("Unexpected token encountered: " + t.kind());
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
			t = lexer.next();
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
		if (t.kind() != COMMA) {
			throw new SyntaxException("Expected ',' token at " + t.sourceLocation()); // replace with more specific error
		}
		t = lexer.next();
		eY = expr();
		if (t.kind() != RSQUARE) {
			throw new SyntaxException("Expected ']' token at " + t.sourceLocation()); // replace with specific error
		}
			t = lexer.next();
		return new PixelSelector(firstToken, eX, eY);
	}

	// may have unnecessary lexer.next() calls
	private Expr ExpandedPixelSelector() throws PLCCompilerException {
		IToken firstToken = t;
		
		t = lexer.next();
		Expr eR = expr();
		Expr eG;
		Expr eB;
		if (t.kind() != COMMA) {
			throw new SyntaxException(t.sourceLocation(), "Expected ',' token after the red component expression.");
		}
		t = lexer.next();
		eG = expr();
		if (t.kind() != COMMA) {
			throw new SyntaxException(t.sourceLocation(), "Expected ',' token after the green component expression.");
		}
		t = lexer.next();
		eB = expr();
		
		if (t.kind() != RSQUARE) {
			throw new SyntaxException(t.sourceLocation(), "asdasdasdas Expected ']' mewo meow mewo mewo meow token to close the pixel selector expression.");
		}
		t = lexer.next();
		return new ExpandedPixelExpr(firstToken, eR, eG, eB);
	}
	//TODO: functions below this comment are unfinished
	private Dimension Dimension() throws PLCCompilerException {
		// This is copied directly from PixelSelector's code, may not be exactly what is needed
		IToken firstToken = t;
		t = lexer.next();
		Expr eX = expr();
		Expr eY;
		if (t.kind() != COMMA) {
			throw new SyntaxException("Expected ',' token at " + t.sourceLocation());
		}
		t = lexer.next();
		eY = expr();
		if (t.kind() != RSQUARE) {
			throw new SyntaxException("Expected ']' token at " + t.sourceLocation()); // replace with specific error
		}
		t = lexer.next();
		return new Dimension(firstToken, eX, eY);
	}
	private LValue LValue() throws PLCCompilerException {
		if(t.kind() != IDENT) {
			throw new SyntaxException("Expected 'IDENT' token at " + t.sourceLocation());
		}
		IToken firstToken = t;
		t = lexer.next();
		PixelSelector pix = null;
		ChannelSelector chan = null;
	
		// Check for PixelSelector
		if (t.kind() == LSQUARE) {
			pix = PixelSelector();
		}
		// t = lexer.next(); this cant be there
		
		// Check for ChannelSelector
		if (t.kind() == COLON) {
			chan = ChannelSelector();
		}
		
		
		return new LValue(firstToken, firstToken, pix, chan);
	}
	
	private AST Statement() throws PLCCompilerException {
		IToken firstToken = t;
		switch (t.kind()) {
			case IDENT -> {
				LValue e = LValue();
				// t = lexer.next();
				if(t.kind() != ASSIGN){
					throw new SyntaxException("Expected 'ASSIGN' token at " + t.sourceLocation() + t.kind());
				}
				t = lexer.next();
				AST statement = new AssignmentStatement(firstToken, e, expr());
				// t = lexer.next();
				return statement;
			}
			case RES_write -> {
				t = lexer.next();
				return new WriteStatement(firstToken, expr());
			}
			case RES_do -> {
				t = lexer.next();
				List<GuardedBlock> list = new ArrayList<>();
				list.add(GuardedBlock());
				if(t.kind() != RSQUARE){
					throw new SyntaxException("Expected 'RSQUARE' token at " + t.sourceLocation());
				}
				t = lexer.next();
				if(t.kind() != LSQUARE){
					throw new SyntaxException("Expected 'LSQUARE' token at " + t.sourceLocation());
				}
				t = lexer.next();
				while(t.kind() != RES_od){
					list.add(GuardedBlock());
				}
				t = lexer.next(); // hmmm
				//TODO: could need an error to throw if od is never detected, not sure
				return new DoStatement(firstToken, list);
			}
			case RES_if -> {
				t = lexer.next();
				List<GuardedBlock> list = new ArrayList<>();
				list.add(GuardedBlock());
				if(t.kind() != RSQUARE){
					throw new SyntaxException("Expected 'RSQUARE' token at " + t.sourceLocation());
				}
				t = lexer.next();
				if(t.kind() != LSQUARE){
					throw new SyntaxException("Expected 'LSQUARE' token at " + t.sourceLocation());
				}
				t = lexer.next();
				while(t.kind() != RES_fi){
					list.add(GuardedBlock());
				}
				t = lexer.next(); /// hmmmm
				//TODO: could need an error to throw if od is never detected, not sure
				return new IfStatement(firstToken, list);
			}
			case RETURN -> {
				t = lexer.next();
				return new ReturnStatement(firstToken, expr());
			}
			case BLOCK_OPEN -> {
				return new StatementBlock(firstToken, Block());
			}
			default -> throw new SyntaxException("Unexpected token encountered: " + t.kind());
		}
	}
	private GuardedBlock GuardedBlock() throws PLCCompilerException {
		IToken firstToken = t;
		Expr e = expr();
		if(t.kind() != RARROW){
			throw new SyntaxException("Expected 'RARROW' token at " + t.sourceLocation());
		}
		t = lexer.next();
		Block block = Block();
		return new GuardedBlock(firstToken, e, block);
	}
}
