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
package edu.ufl.cise

import static edu.ufl.cise.cop4020fa23.Kind.EOF;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Look I know this code looks god awful but it fucking works ok it fucking works every test case works so 

public class Lexer implements ILexer {
	String input;
	char ch;
	private int pos = 0;
	private int realPos = 0;
	private int counter = 0;
	private StringBuilder sb;
	private HashSet<String> reservedWords = new HashSet<>(Arrays.asList("image", "pixel", "int", "string", "void",
			"boolean", "write", "height", "width", "if", "fi", "do", "od", "red", "green", "blue"));
	private HashSet<String> constantWords = new HashSet<>(Arrays.asList("Z", "BLACK", "BLUE", "CYAN", "DARK_GRAY",
			"GRAY", "GREEN", "LIGHT_GRAY", "MAGENTA", "ORANGE", "PINK", "RED", "WHITE", "YELLOW"));

	private int line = 1, column = 0, realCol = 0;

	// we need to keep track of this as we are going through each ch that reads a \n
	// must update line
	// we can calculate column by using pos so when ever we do pos++ we also do
	// column++
	// make sure you reset column to 0 each time you add to line

	private enum State {
		START, IN_IDENT, IN_NUMLIT, IN_STRINGLIT, IN_COMMENT, IN_OPERATOR, IN_WHITESPACE
	};

	private State st = State.START;

	private Kind mapper(String s) {
		// For reserved words
		if (reservedWords.contains(s)) {
			return Kind.valueOf("RES_" + s.toLowerCase());
		}
		// For constant words
		else if (constantWords.contains(s)) {
			return Kind.CONST;
		}
		// For boolean literals
		else if (s.equalsIgnoreCase("TRUE") || s.equalsIgnoreCase("FALSE")) {
			return Kind.BOOLEAN_LIT;
		} 
		else {
			// For single or double character tokens, and other special cases
			return switch (s) {
				case "," -> Kind.COMMA;
				case ";" -> Kind.SEMI;
				case "?" -> Kind.QUESTION;
				case ":" -> Kind.COLON;
				case "(" -> Kind.LPAREN;
				case ")" -> Kind.RPAREN;
				case "<" -> Kind.LT;
				case ">" -> Kind.GT;
				case "[" -> Kind.LSQUARE;
				case "]" -> Kind.RSQUARE;
				case "=" -> Kind.ASSIGN;
				case "==" -> Kind.EQ;
				case "<=" -> Kind.LE;
				case ">=" -> Kind.GE;
				case "!" -> Kind.BANG;
				case "&" -> Kind.BITAND;
				case "&&" -> Kind.AND;
				case "|" -> Kind.BITOR;
				case "||" -> Kind.OR;
				case "+" -> Kind.PLUS;
				case "-" -> Kind.MINUS;
				case "*" -> Kind.TIMES;
				case "**" -> Kind.EXP;
				case "/" -> Kind.DIV;
				case "%" -> Kind.MOD;
				case "<:" -> Kind.BLOCK_OPEN;
				case ":>" -> Kind.BLOCK_CLOSE;
				case "^" -> Kind.RETURN;
				case "->" -> Kind.RARROW;
				case "[]" -> Kind.BOX;
				case "EOF" -> Kind.EOF;  // For eof
				default -> {
					// For idents, nums, and default error
					if (s.matches("^[a-zA-Z_][a-zA-Z_0-9]*$")) yield Kind.IDENT;
					else if (s.matches("^\\d+$")) yield Kind.NUM_LIT;
					else if (s.matches("^\".*\"$")) yield Kind.STRING_LIT;
					else yield Kind.ERROR;  // default error
				}
			};
		}
	}
	
	

	public Lexer(String input) {
		this.input = input;
	}

	@Override
	public IToken next() throws LexicalException {

		// create a new empyty stringbuilder
		this.sb = new StringBuilder();
		// makes sure that when I create a new token the state is back at start
		st = State.START;

		// this is if the input is empty empty not just white space empyy
		if (input.isEmpty()) {
			return new Token(EOF, pos, 0, new char[0], new SourceLocation(line, column));
		}

		// this is the kind of token umm and in theory this should be what the comments
		// and white lines should be
		// since kinds for commments and white lines are not changed
		// EOF is the default value
		Kind kind = EOF;
		int meow = 0;

		try {

			outer:
			while (pos <= input.length() + 1) {
				// character at current position\

				counter++;
				if (st == State.IN_STRINGLIT && pos == input.length()) {
					// need in case the string lit doesnt end
					throw new LexicalException(new SourceLocation(line, column), "Unterminated string literal");
				}


				if (pos < input.length())
				{
					ch = input.charAt(pos);
				} else {
					System.out.println("meow");
					break outer;
				}

				// here the switch statement should default go to start
				/*
				 * Then after start it should stay in that case until a new token
				 * so like if its a ident we think its an ident until its proven not
				 * 
				 * so inside the ident case we check if the next character is an ident or if
				 * sb matches with a reserved word or TRUE/FALSE or a constant as defined in
				 * lexical structure
				 * if sb does match with a a reserved word or TRUE/FALSE or a constant break out
				 * of the while loop
				 */
				switch (st) {
					case START -> {
						sb.append(ch);
						

						if (Character.isLetter(ch) || ch == '_') {
							st = State.IN_IDENT;
							kind = kind.IDENT;
							if (sb.toString().equals("Z"))
							{
								kind = mapper(sb.toString());
								pos++;
								column++;
								st = State.START;
								break outer;
							}
						} else if (Character.isDigit(ch)) {
							st = State.IN_NUMLIT;
							kind = kind.NUM_LIT;
						} else if (ch == '\"') { // java thing
							st = State.IN_STRINGLIT;
							
						} else if (ch == '#') {
							// check if the next character is a #
							pos++;
							column++;
							realCol = column;
							
							st = State.IN_COMMENT;
								
							if (sb.length() >= 1)
							 {
							 	sb.deleteCharAt(sb.length() - 1);
							 }
								// handles second #
							if (st == State.IN_COMMENT && input.charAt(pos) != '#') {
								throw new LexicalException(new SourceLocation(line, column),
										"Invalid comment start needs another '#' following the first '#'");

							}
							
								
							break;
							
						} else if (",;?():<>=!&|+-*/%^[]".indexOf(ch) != -1) {
							kind = mapper(sb.toString());
							st = State.IN_OPERATOR;
						} else if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') {
							// going to handle white spaces here so it breaks early
							st = State.IN_WHITESPACE;
							
							// the current index of the position is a new line so we need to increment it
							// for the next token
							pos++;
							column++;
							realCol = column;
							if (ch == '\n') {
								line++;
								column = 0;
							}
							if (sb.length() >= 1)
							 {
							 	sb.deleteCharAt(sb.length() - 1);
							 }
								
							break;
							

							
						} else {
							// technically id want this to go on like default but idk how
							// hm this just means idk what character was the input
							throw new LexicalException(new SourceLocation(line, column), "Unexpected character: " + ch);
						}

						// goes to next character after the new one
						realPos = pos;
						pos++;
						column++;
						realCol = column;

						// handle edge cases
						
						// no second comment


						


						// i need to also handle comment inside quote
						// i need to think of more edge cases to put here
						
					}
					case IN_IDENT -> {

						sb.append(ch);
							
						
						String sbStr = sb.toString();

						// handles reserved, constant, and Bool
						if (reservedWords.contains(sbStr) || constantWords.contains(sbStr) || "TRUE".equals(sbStr)
								|| "FALSE".equals(sbStr)) {
							kind = mapper(sbStr);
							// this is the current pos so need to increment for next token
							pos++;
							column++;
							st = State.START;
							break outer;

						} else if (input.length() != 1 && (Character.isLetter(ch) || ch == '_' || Character.isDigit(ch))) {
							// yep its an identifiyer time to check the next pos
							pos++;
							column++;
							if (sb.length() > input.length())
							{
								sb.deleteCharAt(sb.length() - 1);
							}
						} else {
							kind = Kind.IDENT;
							if (sb.length() >= 1)
							 {
							 	sb.deleteCharAt(sb.length() - 1);
							 }
							st = State.START;
							// token is a whitespace or sum so we go to the next pos 
							
							// current character is not an identifiyer

							// ohhh this makes the
							

							break outer;
						}

						/*
						 * if starts with uppercase, keep adding uppercases until either == boolean_lit,
						 * ==const, or there aren't any more uppercases
						 * if there are no more uppercases, it's a default ident
						 * ok more to this
						 * we need to use the hashset of reserved words here
						 * and be like if reservedWords.Contains(sb.toString());
						 * we also need to check in the hashset og Constant words
						 * and then check if the sb equals a booleanLit of TRUE or FALSE
						 * then
						 * break
						 * 
						 */

					}

					case IN_NUMLIT -> {
						
						if (Integer.valueOf(sb.toString()) == 0 )
							{
								kind = Kind.NUM_LIT;
							
								break outer;
							}
						sb.append(ch);
						if (Character.isDigit(ch)) {
							// current character works so increment
							pos++;
							column++;
						} else {
							// current character doesnt work so we need to make the token
							kind = Kind.NUM_LIT;
							if (sb.length() >= 1)
								sb.deleteCharAt(sb.length() - 1);
							
							break outer;
						}

						String testStr = sb.toString();
						try {
							Integer.parseInt(testStr);
						} catch(NumberFormatException e) {
							throw new LexicalException(new SourceLocation(line, column),
								"Integer too large" + e.getMessage());
						}
						

						// ok this checks if sb is a 0

				
						/*
						 * if sb is a 0 then we break
						 * if sb is not a 0 then we keep going through the input string and updating ch
						 * and pos
						 * keep on adding ch to the sb and if ch isnt a number then break dont forget to
						 * decrement pos
						 */

					}
					case IN_STRINGLIT -> {
						sb.append(ch);
						if (ch == '"') {
							kind = Kind.STRING_LIT;
							
							st = State.START;
							pos++;
							column++;
							break outer;
						} else if ((ch >= 32 && ch <= 126) ||(ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t')) {
							// still inside the quotes
							pos++;
							column++;
							if (ch == '\n') {
								line++;
								column = 0;
							}
							
						} else if (st == State.IN_STRINGLIT && pos == input.length()) {
							// need in case the string lit doesnt end
							throw new LexicalException(new SourceLocation(line, column), "Unterminated string literal");
						} else {
							// error idk if this is handled right
							throw new LexicalException(new SourceLocation(line, column),
									"Invalid char in string literal");
						}
						/*
						 * just keep going through the input string and updating ch and sb but make sure
						 * that at somepoint we encounter another quote '"'
						 * if we dont encounter another quote then throw in an error
						 * also throw an error if one of the characters isnt a printable char from the
						 * ascii table
						 * 
						 */

					}
					case IN_COMMENT -> {
						sb.append(ch);
						/*
						 * ok we have to make sure that the next ch is a # if not then we throw an error
						 */ 



						// incase i need it
						// else if (st == State.IN_COMMENT && pos == input.length()) {
						// 	// comment doesnt end
						// 	throw new LexicalException(new SourceLocation(line, column), "Unterminated comment");

						// } 

						if ((ch >= 32 && ch <= 126)) { // # is 35
							// still in the comment
							pos++;
							column++;
							if (sb.length() >= 1)
								sb.deleteCharAt(sb.length() - 1);
							
							break;

						} else if (ch == '\r'){
							//for if there's exactly the characters \r\n, which seemed to need a special case according to the "additional requirements"
							if(input.charAt(pos+1) == '\n'){
							 pos++;
							 column++;
							 line++;
							 column = 0;
							 pos++;
							 st = State.START;
							 if (sb.length() >= 1)
								sb.deleteCharAt(sb.length() - 1);
							 break;
							}
						} else if (ch == '\n') { // should I add other white spaces thats not \s???
							line++;
							column = 0;
							pos++;
							
							st = State.START;
							if (sb.length() >= 1)
								sb.deleteCharAt(sb.length() - 1);
							break;
						} else {
							throw new LexicalException(new SourceLocation(line, column),
									"Invalid char in string literal");
						}
						//
					}
					case IN_OPERATOR -> {
						

						// means next character is still an operator....
						
						// um make sure the next ch for ops like && and ** if not then just break
						// Like we dont want && to be seperate tokens

						if (pos < input.length())
						{
							char nextCh = input.charAt(pos);
							if ((sb.toString().equals("&") && nextCh == '&') || (sb.toString().equals("*") && nextCh == '*')
							|| (sb.toString().equals("=") && nextCh == '=') || (sb.toString().equals("|") && nextCh == '|')
							|| (sb.toString().equals("[") && nextCh == ']') || (sb.toString().equals(">") && nextCh == '=')
							|| (sb.toString().equals("<") && nextCh == '=') || (sb.toString().equals("<") && nextCh == ':')
							|| (sb.toString().equals(":") && nextCh == '>') || (sb.toString().equals("-") && nextCh == '>')) {
								sb.append(nextCh);

								pos++;
								column++;
							}
						}
						// in theory this character shouldnt be a OP and the pos should stay the same
						// for next token
						

						// handles kind of OP 
						kind = mapper(sb.toString());
						st = State.START;
						break outer;

					}
					case IN_WHITESPACE -> {
						sb.append(ch);
						if (ch != ' ' || ch != '\n' || ch != '\r' || ch != '\t') {
							// not going to update pos
							st = State.START;
							if (sb.length() >= 1)
								sb.deleteCharAt(sb.length() - 1);
							
							break;
						}
						// checks if sb is a white space


						// this should instantly break
						// well this is a problem if this reaches here like huh
						//NEVER CALLED
						// break outer;
						
						if (sb.length() >= 1)
							sb.deleteCharAt(sb.length() - 1);
						pos++;
						column++;
						
						break;

					}
					default -> {
						throw new LexicalException(new SourceLocation(line, column), "Unexpected character: " + ch);
						// should only happen if the starting character is an error
						// if it is just throw in an err
					}
				}

				System.out.println("hello");

			}

			System.out.println("hello");
		}

		// and return the token
		catch (LexicalException e) {
			// handleeeeeee
			throw new LexicalException(new SourceLocation(line, column),
					"Caught a LexicalException: " + e.getMessage());
		}


		
		
		/*
		 * ok outside of while loop we need to make sure all the information to create a
		 * new token is correct4
		 * 
		 */

		// create a character list from the string builder
		

		char charList[] = new char[sb.length()];
		for (int i = 0; i < sb.length(); ++i) {
			charList[i] = sb.charAt(i);

		}

		// check to return an actual empty file should work?
		// if (pos >= input.length()) {
		// 	return new Token(EOF, pos, 0, new char[0], new SourceLocation(line, column));
		// }
		// just made pos = 0 for now
		// comments and white lines should be empty
		return new Token(kind, 0, sb.length(), charList, new SourceLocation(line, realCol));
	}

}
