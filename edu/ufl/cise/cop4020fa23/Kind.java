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

/**
 *  enum representing different Token kinds for compiler
 */
public enum Kind {
	IDENT,
	NUM_LIT,
	STRING_LIT,
	RES_image, 
	RES_pixel,
	RES_int,
	RES_string,
	RES_void,
	RES_boolean,
	RES_nil,
	RES_write,
	RES_height,
	RES_width,
	RES_if,
	RES_fi,
	RES_do,
	RES_od,
	RES_red,
	RES_green,
	RES_blue,
	CONST, // Z | BLACK | BLUE | CYAN | DARK_GRAY | GRAY | GREEN | LIGHT_GRAY | MAGENTA | ORANGE | PINK | RED | WHITE | YELLOW
	BOOLEAN_LIT,// TRUE, FALSE
	COMMA, // ,
	SEMI, // ;
	QUESTION, // ?
	COLON, // :
	LPAREN, // (
	RPAREN, // )
	LT, // <
	GT, // >
	LSQUARE, // [
	RSQUARE, // ]
	ASSIGN, // =
	EQ, // ==
	LE, // <=
	GE, // >=
	BANG, // !
	BITAND, // &
	AND, // &&
	BITOR, // |
	OR, // ||
	PLUS, // +
	MINUS, // -
	TIMES, // *
	EXP, // **
	DIV, // /
	MOD, // %
	BLOCK_OPEN, // <:
	BLOCK_CLOSE, // :>
	RETURN, // ^
	RARROW, // ->
	BOX, //  []
	EOF,
	ERROR;

	
}
