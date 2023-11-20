/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */


package edu.ufl.cise.cop4020fa23.runtime;

/**
 * Pixels are always represented internally as a single int with each color channel value represented by 8 bits. The alpha channel (transparency) is
 * not used in our language--it always has the value 0xff.  
 * 
 * Because only 8 bits are available for each color, the maximum value for a color component is 255.  
 * 
 * This class contains methods to pack 3 color values into a single int as well as routines to get and set individual color channels.  
 * 
 * Hint:  To show an integer representing a pixel as a hex number (where the digits represent the channels as follows) AARRGGBB use Integer.toHexString(pixel);
 */

public class PixelOps {

	/*
	 * create a packed color with the given color component values. Values less than
	 * 0 or greater than 255 are truncated.
	 */
	public static int pack(int redVal, int grnVal, int bluVal) {
		int pixel = ((0xFF << SHIFT_ALPHA) | (truncate(redVal) << SHIFT_RED) | (truncate(grnVal) << SHIFT_GREEN)
				| (truncate(bluVal) << SHIFT_BLUE));
		return pixel;
	}
	
	public static int red(int pixel) {
		return (pixel & SELECT_RED) >> SHIFT_RED;
	}

	public static int green(int pixel) {
		return (pixel & SELECT_GREEN) >> SHIFT_GREEN;
	}

	public static int blue(int pixel) {
		return (pixel & SELECT_BLUE) >> SHIFT_BLUE;
	}

	public static int setRed(int pixel, int val) {
		return pack(val, green(pixel), blue(pixel));
	}

	public static int setGreen(int pixel, int val) {
		return pack(red(pixel), val, blue(pixel));
	}
	
	public static int setBlue(int pixel, int val) {
		return pack(red(pixel), green(pixel), val);
	}
	
	public enum Colors {red, green, blue};
	
	public static int setColor(int pixel, int val, Colors color) {
		return switch (color) {
		case red -> setRed(pixel,val);
		case blue -> setBlue(pixel,val);
		case green -> setGreen(pixel,val);
		default -> throw new IllegalArgumentException("Unexpected value: " + color);
		};
	}
	
	/**
	 * truncates an int to value in range of [0,256)
	 * 
	 * @param z
	 * @return value in [0,256)
	 */
	private static int truncate(int z) {
		return z < 0 ? 0 : (z > 255 ? 255 : z);
	}
	
	
	/** Constants used in building and select color components from a packed int */
	public static final int SELECT_RED = 0x00ff0000;
	public static final int SELECT_GREEN = 0x0000ff00;
	public static final int SELECT_BLUE = 0x000000ff;
	public static final int SELECT_ALPHA = 0xff000000;
	public static final int SHIFT_ALPHA = 24;
	public static final int SHIFT_RED = 16;
	public static final int SHIFT_GREEN = 8;
	public static final int SHIFT_BLUE = 0;


}
