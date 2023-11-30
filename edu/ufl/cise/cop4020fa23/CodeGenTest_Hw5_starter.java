package edu.ufl.cise.cop4020fa23;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.PLCLangExec;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;

import static org.junit.jupiter.api.Assertions.*;

class CodeGenTest_Hw5_starter {

	String packageName = "edu.ufl.cise.cop4020fa23";
	String testURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d3/Statue_of_Liberty%2C_NY.jpg/1280px-Statue_of_Liberty%2C_NY.jpg";

	@AfterEach
	public void separatingLine() {
		show("----------------------------------------------");
	}

	// makes it easy to turn output on and off (and less typing than
	// System.out.println)
	static final boolean VERBOSE = true;
	static final boolean WAIT_FOR_INPUT = false;

	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}

	void showPixel(int p) {
		if (VERBOSE) {
			System.out.println(Integer.toHexString(p));
		}
	}

	/**
	 * Displays the given image on the screen. If WAIT_FOR_INPUT, a prompt to enter
	 * a char is displayed on the console, and execution waits until some character
	 * is entered. This is to ensure that displayed images are not immediately
	 * closed by Junit before you have a chance to view them.
	 * 
	 * @param image
	 * @throws IOException
	 */
	void show(BufferedImage image) throws IOException {
		if (VERBOSE) {
			ConsoleIO.displayImageOnScreen(image);
			if (WAIT_FOR_INPUT) {
				System.out.println("Enter a char");
				int ch = System.in.read();
			}
		}

	}

	void compareImages(BufferedImage image0, BufferedImage image1) {
		assertEquals(image0.getWidth(), image1.getWidth(), "widths not equal");
		assertEquals(image0.getHeight(), image1.getHeight(), "heights not equal");
		for (int y = 0; y < image0.getHeight(); y++)
			for (int x = 0; x < image0.getWidth(); x++) {
				int p0 = image0.getRGB(x, y);
				int p1 = image1.getRGB(x, y);
				assertEquals(p0, p1, "pixels at [" + x + "," + y + "], expected: " + Integer.toHexString(p0)
						+ ", but was: " + Integer.toHexString(p1));
			}
	}

	@Test
	void hw5_0() throws Exception {
		String source = """
				int f()<:
				  ^Z;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source);
		show((int) result);
		assertEquals(255, (int) result);
	}

	@Test
	void hw5_1() throws Exception {
		String source = """
				pixel REDPixel()<:
				           ^RED;
				         :>

				""";
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.RED.getRGB(), (int) result);
	}

	@Test
	void hw5_2() throws Exception {
		String source = """
				pixel PINKPixel()<:
				           ^PINK;
				         :>

				""";
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.PINK.getRGB(), (int) result);
	}

	/**
	 * package edu.ufl.cise.cop4020fa23; public class BLUEPixel{ public static int
	 * apply(){ return 0xff0000ff; } }
	 * 
	 */
	@Test
	void hw5_3() throws Exception {
		String source = """
				pixel BLUEPixel()<:
				           ^BLUE;
				         :>

				""";
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.BLUE.getRGB(), (int) result);
	}

	/*
	 * 
	 * package edu.ufl.cise.cop4020fa23; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; public class PixelSum{ public
	 * static int apply(){
	 * int p0$2=0xff0000ff;
	 * int p1$2=0xff00ff00;
	 * return (ImageOps.binaryPackedPixelPixelOp(ImageOps.OP.PLUS,p0$2,p1$2)); } }
	 */
	@Test
	void hw5_4() throws Exception {
		String source = """
				pixel PixelSum() <:
				  pixel p0 = BLUE;
				  pixel p1 = GREEN;
				  ^ p0 + p1;
				  :>
				""";
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel(Color.BLUE.getRGB());
		showPixel(Color.GREEN.getRGB());
		showPixel((int) result);
		assertEquals(Color.CYAN.getRGB(), (int) result);
	}

	/**
	 * 
	 * Expected Output from program (not including other test case output) ff00ff00
	 * ff0000ff 33
	 * 
	 * Generated Java code
	 * 
	 * package edu.ufl.cise.cop4020fa23; import
	 * edu.ufl.cise.cop4020fa23.runtime.PixelOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.ConsoleIO; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; public class colors{ public static
	 * int apply(){ ConsoleIO.writePixel(0xff00ff00);
	 * ConsoleIO.writePixel(0xff0000ff); ConsoleIO.write(33); return
	 * (ImageOps.binaryPackedPixelPixelOp(ImageOps.OP.PLUS,0xffff0000,PixelOps.pack(33,33,33)));
	 * } }
	 */
	@Test
	void hw5_5() throws Exception {
		String source = """
				pixel colors()<:
				write GREEN;
				write BLUE;
				write 33;
				^ (RED + [33,33,33]);
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(0xffff2121, (int) result);
	}

	/**
	 * 
	 * 
	 * package edu.ufl.cise.cop4020fa23; import
	 * edu.ufl.cise.cop4020fa23.runtime.PixelOps; public class f{ public static int
	 * apply(int a$1){ int b$2; b$2=PixelOps.pack(a$1,a$1,a$1); return b$2; } }
	 */
	@Test
	void hw5_6() throws Exception {
		String source = """
				pixel f(int a)
				<:
				pixel b;
				b = a;
				^b;
				:>
				""";
		int val = 33;
		int p = (Integer) PLCLangExec.runCode(packageName, source, val);
		show(Integer.toHexString(p));
		int expected = PixelOps.pack(val, val, val);
		assertEquals(expected, p);
	}

	/**
	 * package edu.ufl.cise.cop4020fa23; import
	 * edu.ufl.cise.cop4020fa23.runtime.PixelOps; public class f{ public static int
	 * apply(int a$1){ int b$2; b$2=PixelOps.pack(PixelOps.red(a$1),255,255); return
	 * b$2; } }
	 */
	@Test
	void hw5_7() throws Exception {
		String source = """
				pixel f(pixel a)
				<:
				pixel b;
				b = [a:red, Z, Z];
				^b;
				:>
				""";
		int val = 33;
		int p = (Integer) PLCLangExec.runCode(packageName, source, PixelOps.pack(val, val, val));
		show(Integer.toHexString(p));
		int expected = PixelOps.pack(val, 255, 255);
		assertEquals(expected, p);
	}

	@Test
	void hw5_8() throws Exception {
		String source = """
				pixel f(int r, int g, int b)
				<:
				pixel p = [r,g,b];
				^p;
				:>
				""";
		int r = 1;
		int g = 2;
		int b = 3;
		int p = (Integer) PLCLangExec.runCode(packageName, source, r, g, b);
		show(Integer.toHexString(p));
		int expected = PixelOps.pack(r, g, b);
		assertEquals(expected, p);
	}

	/*
	 * package edu.ufl.cise.cop4020fa23; import
	 * edu.ufl.cise.cop4020fa23.runtime.PixelOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.ConsoleIO; public class f{ public static int
	 * apply(){ int pred$2; pred$2=0xffff0000; ConsoleIO.write("initial value");
	 * ConsoleIO.writePixel(pred$2);
	 * pred$2=PixelOps.setGreen(pred$2,255);
	 * ConsoleIO.write("after assignment to green channel");
	 * ConsoleIO.writePixel(pred$2);
	 * return pred$2; } }
	 * 
	 * Expected output: initial value ffff0000 after assignment to green channel
	 * ffffff00
	 */
	@Test
	void hw5_9() throws Exception {
		String source = """
				pixel f()
				<:
				pixel pred;
				pred = RED;
				write "initial value";
				write pred;
				pred:green = Z;
				write "after assignment to green channel";
				write pred;
				^pred;
				:>
				""";
		int p = (Integer) PLCLangExec.runCode(packageName, source);
		assertEquals(PixelOps.pack(255, 255, 0), p);
		assertEquals(java.awt.Color.YELLOW.getRGB(), p);
	}

	/**
	 * 
	 * package edu.ufl.cise.cop4020fa23; import java.awt.image.BufferedImage; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.FileURLIO; public class f{ public static
	 * BufferedImage apply(String url$1){ BufferedImage
	 * i$2=FileURLIO.readImage(url$1); return i$2; } }
	 */
	@Test
	void hw5_10() throws Exception {
		String source = """
				image f(string url)
				<:
				image i = url;
				^i;
				:>
				""";
		String url = testURL;
		BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, url);
		BufferedImage expectedImage = FileURLIO.readImage(url);
		compareImages(expectedImage, result);
		show(result);
	}

	/**
	 * package edu.ufl.cise.cop4020fa23; import java.awt.image.BufferedImage; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.FileURLIO; public class f{ public static
	 * BufferedImage apply(String url$1){ BufferedImage
	 * i$2=FileURLIO.readImage(url$1); BufferedImage j$2=ImageOps.cloneImage(i$2); ;
	 * return j$2; } }
	 * 
	 */
	@Test
	void hw5_11() throws Exception {
		String source = """
				image f(string url)
				<:
				image i = url;
				image j = i;
				^j;
				:>
				""";
		String url = testURL;
		BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, url);
		BufferedImage expected = FileURLIO.readImage(url);
		compareImages(expected, result);
		show(result);
	}

	/**
	 * package edu.ufl.cise.cop4020fa23; import java.awt.image.BufferedImage; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.FileURLIO; public class f{ public static
	 * BufferedImage apply(String url$1){ BufferedImage
	 * i$2=FileURLIO.readImage(url$1); BufferedImage
	 * j$2=ImageOps.copyAndResize(i$2,50,100); ; return j$2; } }
	 */
	@Test
	void hw5_12() throws Exception {
		String source = """
				image f(string url)
				<:
				image i = url;
				image[50,100] j = i;
				^j;
				:>
				""";
		String url = testURL;
		BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, url);
		BufferedImage expected = FileURLIO.readImage(url, 50, 100);
		compareImages(expected, result);
		show(result);
	}

	/**
	 * package edu.ufl.cise.cop4020fa23; import
	 * edu.ufl.cise.cop4020fa23.runtime.PixelOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.ConsoleIO; public class f{ public static int
	 * apply(){ int p$2=0xffff00ff; ConsoleIO.writePixel(p$2);
	 * ConsoleIO.write(PixelOps.red(p$2)); ConsoleIO.write(PixelOps.green(p$2));
	 * ConsoleIO.write(PixelOps.blue(p$2)); return PixelOps.blue(p$2); } }
	 * 
	 * Expected output:
	 * 
	 * ffff00ff 255 0 255
	 */
	@Test
	void hw5_13() throws Exception {
		String source = """
				int f()
				<:
				pixel p = MAGENTA;
				write p;
				write p:red;
				write p:green;
				write p:blue;
				^ p:blue;
				:>
				""";
		int result = (Integer) PLCLangExec.runCode(packageName, source);
		int expected = Color.blue.getBlue();
		assertEquals(expected, result);
	}

	/**
	 *
	 * package edu.ufl.cise.cop4020fa23; public class f{ public static int apply(){
	 * int x$2=3; if(x$2>2){ } else if(true){ x$2=(x$2+1); } ; return x$2; } }
	 * 
	 * @throws Exception
	 */
	@Test
	void hw5_14() throws Exception {
		String source = """
				int f()
				<:
				int x = 3;
				if
				   x > 2 -> <:  :>
				   []
				   TRUE -> <: x = x+1; :>
				   fi;
				   ^x;
				:>
				""";
		int result = (Integer) PLCLangExec.runCode(packageName, source);
		assertEquals(3, result);

	}

	/**
	 * package edu.ufl.cise.cop4020fa23;
	 * 
	 * public class f{ public static int apply(){ int x$2=1; if(x$2>2){ } else
	 * if(true){ x$2=(x$2+1); } ; return x$2; } }
	 */
	@Test
	void hw5_15() throws Exception {
		String source = """
				int f()
				<:
				int x = 1;
				if
				   x > 2 -> <:  :>
				   []
				   TRUE -> <: x = x+1; :>
				   fi;
				   ^x;
				:>
				""";
		int result = (Integer) PLCLangExec.runCode(packageName, source);
		assertEquals(2, result);
	}

	/*
	 * package edu.ufl.cise.cop4020fa23; public class gcd{ public static int
	 * apply(int a$1, int b$1){ {boolean continue$0= false; while(!continue$0){
	 * continue$0=true; if(a$1<b$1){ continue$0 = false; { b$1=(b$1-a$1); } }
	 * if(b$1<a$1){ continue$0 = false; { a$1=(a$1-b$1); } } } }; return a$1; } }
	 */
	@Test
	void hw5_16() throws Exception {
		String source = """
				int gcd(int a, int b)
				<:
				do
				   a < b -> <: b = b-a; :>
				[]
				   b< a -> <:  a = a -b; :>
				od;
				^a;
				:>
				""";
		int gcd = (Integer) PLCLangExec.runCode(packageName, source, 10, 15);
		assertEquals(5, gcd);
	}

	/**
	 * package edu.ufl.cise.cop4020fa23; import java.awt.image.BufferedImage; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; public class makeImage{ public
	 * static BufferedImage apply(int w$1, int h$1){ final BufferedImage
	 * im$2=ImageOps.makeImage(w$1,h$1); for (int x$3=0; x$3<im$2.getWidth();x$3++){
	 * for (int y$3=0; y$3<im$2.getHeight();y$3++){
	 * ImageOps.setRGB(im$2,x$3,y$3,(y$3>(h$1/2)?0xff0000ff:0xff00ff00)); } };
	 * return im$2; } }
	 */
	@Test
	void hw5_17() throws Exception {
		String source = """
				image makeImage(int w, int h) <:
				   image[w,h] im;
				   im[x,y] = ? (y > h/2) -> BLUE , GREEN;
				   ^im;
				   :>
				   """;
		BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, 200, 300);
		show(result);
		BufferedImage expected = ImageOps.makeImage(200, 300);
		for (int y = 0; y < 300; y++)
			for (int x = 0; x < 200; x++)
				expected.setRGB(x, y, y > 150 ? Color.blue.getRGB() : Color.green.getRGB());
		compareImages(expected, result);
	}

	@Test
	void hw5_18() throws Exception {
		BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, """
				image makeImage(int w, int h) <:
				   image[w,h] im;
				   im[x,y] = ? (y > h/2) -> BLUE , GREEN;
				   image[h,w] imRotate;
				   imRotate[x,y] = im[y,x];
				   ^imRotate;
				   :>
				   """, 500, 600);
		BufferedImage image0 = ImageOps.makeImage(500, 600);
		for (int y = 0; y < 600; y++)
			for (int x = 0; x < 500; x++)
				image0.setRGB(x, y, y > 300 ? Color.blue.getRGB() : Color.green.getRGB());
		BufferedImage expected = ImageOps.makeImage(600, 500);
		for (int y = 0; y < 500; y++)
			for (int x = 0; x < 600; x++)
				expected.setRGB(x, y, image0.getRGB(y, x));
		show(result);
		compareImages(expected, result);
	}

	/**
	 * public class addImages{ public static BufferedImage apply(int w$1, int h$1){
	 * final BufferedImage im0$2=ImageOps.makeImage(w$1,h$1); final BufferedImage
	 * im1$2=ImageOps.makeImage(w$1,h$1); for (int x$3=0;
	 * x$3<im0$2.getWidth();x$3++){ for (int y$3=0; y$3<im0$2.getHeight();y$3++){
	 * ImageOps.setRGB(im0$2,x$3,y$3,(y$3>(h$1/2)?0xff0000ff:0xff00ff00)); } }; for
	 * (int x$4=0; x$4<im1$2.getWidth();x$4++){ for (int y$4=0;
	 * y$4<im1$2.getHeight();y$4++){
	 * ImageOps.setRGB(im1$2,x$4,y$4,(y$4>(h$1/2)?0xffff0000:0xff404040)); } };
	 * BufferedImage
	 * im2$2=ImageOps.cloneImage((ImageOps.binaryImageImageOp(ImageOps.OP.PLUS,im0$2,im1$2)));
	 * ; return im2$2; } }
	 * 
	 */
	@Test
	void hw5_19() throws Exception {
		String source = """
				image addImages(int w, int h) <:
				   image[w,h] im0;
				   image[w,h] im1;
				   im0[x,y] = ? (y > h/2) -> BLUE , GREEN;
				   im1[x,y] = ? (y > h/2) -> RED , DARK_GRAY;
				   image im2 = im0+im1;
				   ^ im2;
				   :>
				   """;
		int w = 200;
		int h = 300;
		BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, w, h);
		BufferedImage image0 = ImageOps.makeImage(w, h);
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				image0.setRGB(x, y, y > h / 2 ? Color.blue.getRGB() : Color.green.getRGB());
		BufferedImage image1 = ImageOps.makeImage(w, h);
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				image1.setRGB(x, y, y > h / 2 ? Color.red.getRGB() : Color.darkGray.getRGB());

		BufferedImage expected = ImageOps.binaryImageImageOp(ImageOps.OP.PLUS, image0, image1);
		compareImages(expected, result);
		show(result);
	}

	/**
	 * package edu.ufl.cise.cop4020fa23; import java.awt.image.BufferedImage; import
	 * edu.ufl.cise.cop4020fa23.runtime.ConsoleIO; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.FileURLIO; public class checkerBoard{ public
	 * static BufferedImage apply(String url$1, int w$1, int h$1){ BufferedImage
	 * im0$2=FileURLIO.readImage(url$1,w$1,h$1); final BufferedImage
	 * redImage$2=ImageOps.makeImage(w$1,h$1);
	 * ImageOps.setAllPixels(redImage$2,0xffff0000); int xslice$2=(w$1/8); int
	 * yslice$2=(h$1/8); ConsoleIO.write(im0$2); ConsoleIO.write(redImage$2); final
	 * BufferedImage checkers$2=ImageOps.makeImage(w$1,h$1); for (int x$4=0;
	 * x$4<checkers$2.getWidth();x$4++){ for (int y$4=0;
	 * y$4<checkers$2.getHeight();y$4++){
	 * ImageOps.setRGB(checkers$2,x$4,y$4,(((x$4/xslice$2)%2)==((y$4/yslice$2)%2)?ImageOps.getRGB(im0$2,x$4,y$4):ImageOps.getRGB(redImage$2,x$4,y$4)));
	 * } }; return checkers$2; } }
	 */
	@Test
	void hw5_20() throws Exception {
		String source = """
				image checkerBoard(string url, int w, int h) <:
				   image[w,h] im0 = url;
				   image[w,h] redImage;
				   redImage = RED;
				   int xslice = w/8;
				   int yslice = h/8;
				   image [w,h] checkers;
				   checkers[x,y] = ? (x/xslice)%2 == (y/yslice)%2 -> im0[x,y], redImage[x,y];
				   ^checkers;
				   :>
				   """;
		String url = testURL;
		int w = 200;
		int h = 300;
		BufferedImage image = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
		show(image);
		BufferedImage redImage = ImageOps.makeImage(w, h);
		ImageOps.setAllPixels(redImage, Color.red.getRGB());
		BufferedImage im0 = FileURLIO.readImage(url, w, h);
		BufferedImage expected = ImageOps.makeImage(w, h);
		int xslice = w / 8;
		int yslice = h / 8;
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				expected.setRGB(x, y, (x / xslice) % 2 == (y / yslice) % 2 ? im0.getRGB(x, y) : redImage.getRGB(x, y));
		compareImages(expected, image);
	}

	/**
	 * 
	 * package edu.ufl.cise.cop4020fa23;
	 * import java.awt.image.BufferedImage;
	 * import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
	 * import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
	 * public class example{
	 * 		public static BufferedImage apply(int w$1, int h$1){
	 * 			final BufferedImage im$2=ImageOps.makeImage(w$1,h$1);
	 * 				for (int x$3=0; x$3<im$2.getWidth();x$3++){
	 * 					for (int y$3=0; y$3<im$2.getHeight();y$3++){
	 * 						ImageOps.setRGB(im$2,x$3,y$3,PixelOps.pack(x$3,y$3,255));
	 * 					}
	 * 				};
	 * 		return im$2;
	 * 		}
	 * }
	 */
	@Test
	void hw5_21() throws Exception {
		String source = """
				image example(int w, int h) <:
				image[w,h] im;
				im[x,y] = [x,y,Z];
				^im;
				:>
				""";
		int w = 512;
		int h = 512;
		BufferedImage image = (BufferedImage) PLCLangExec.runCode(packageName, source, w, h);
		show(image);
		BufferedImage expected = ImageOps.makeImage(w, h);
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				expected.setRGB(x, y, PixelOps.pack(x, y, 255));
		compareImages(expected, image);
	}

	/**
	 * 
	 * package edu.ufl.cise.cop4020fa23; import java.awt.image.BufferedImage; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.FileURLIO; public class scale{ public static
	 * BufferedImage apply(String url$1, int factor$1){ BufferedImage
	 * im0$2=FileURLIO.readImage(url$1); BufferedImage
	 * im1$2=ImageOps.cloneImage((ImageOps.binaryImageScalarOp(ImageOps.OP.DIV,im0$2,factor$1)));
	 * ; return im1$2; } }
	 */
	@Test
	void hw5_22() throws Exception {
		String source = """
				image scale(string url,int factor)
				<:
				image im0 = url;
				image im1 = im0/factor;
				^im1;
				:>
				""";

		int factor = 2;
		String url = testURL;
		BufferedImage image = (BufferedImage) PLCLangExec.runCode(packageName, source, url, factor);
		show(image);
		BufferedImage im00 = FileURLIO.readImage(url);
		int w = im00.getWidth();
		int h = im00.getHeight();
		BufferedImage expected = ImageOps.copyAndResize((ImageOps.binaryImageScalarOp(ImageOps.OP.DIV, im00, factor)),
				w, h);
		compareImages(expected, image);
	}

	/**
	 * package edu.ufl.cise.cop4020fa23; import java.awt.image.BufferedImage; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.FileURLIO; public class scale{ public static
	 * BufferedImage apply(String url$1, int w$1, int h$1, int factor$1){
	 * BufferedImage im0$2=FileURLIO.readImage(url$1); BufferedImage
	 * im1$2=ImageOps.copyAndResize((ImageOps.binaryImageScalarOp(ImageOps.OP.TIMES,im0$2,factor$1)),w$1,h$1);
	 * ; return im1$2; } }
	 */
	@Test
	void hw5_23() throws Exception {
		String source = """
				image scale(string url, int w, int h, int factor)
				<:
				image im0 = url;
				image[w,h] im1 = im0*factor;
				^im1;
				:>
				""";
		int w = 512;
		int h = 256;
		int factor = 2;
		String url = testURL;
		BufferedImage image = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h, factor);
		show(image);
		BufferedImage im00 = FileURLIO.readImage(url);
		BufferedImage expected = ImageOps.copyAndResize((ImageOps.binaryImageScalarOp(ImageOps.OP.TIMES, im00, factor)),
				w, h);
		compareImages(expected, image);
	}
	// #region Tests by Gabriel Aldous
	@Test
	void unitTestZ() throws Exception {
		String source = """
                int Zint()<:
                           ^Z;
                         :>
                """;
		Object result = PLCLangExec.runCode(packageName, source);
		assertEquals(255, (int) result);
	}


	@Test
	void unitTestBLUE() throws Exception {
		String source = """
                pixel BLUEPixel()<:
                           ^BLUE;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.BLUE.getRGB(), (int) result);
	}


	@Test
	void unitTestBLACK() throws Exception {
		String source = """
                pixel BLACKPixel()<:
                           ^BLACK;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.BLACK.getRGB(), (int) result);
	}


	@Test
	void unitTestCYAN() throws Exception {
		String source = """
                pixel CYANPixel()<:
                           ^CYAN;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.CYAN.getRGB(), (int) result);
	}


	@Test
	void unitTestDARK_GRAY() throws Exception {
		String source = """
                pixel DARKGRAY()<:
                           ^DARK_GRAY;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.DARK_GRAY.getRGB(), (int) result);
	}


	@Test
	void unitTestGRAY() throws Exception {
		String source = """
                pixel GRAYPixel()<:
                           ^GRAY;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.GRAY.getRGB(), (int) result);
	}


	@Test
	void unitTestGREEN() throws Exception {
		String source = """
                pixel GREENPixel()<:
                           ^GREEN;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.GREEN.getRGB(), (int) result);
	}


	@Test
	void unitTestLIGHT_GRAY() throws Exception {
		String source = """
                pixel LIGHT_GRAYPixel()<:
                           ^LIGHT_GRAY;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.LIGHT_GRAY.getRGB(), (int) result);
	}


	@Test
	void unitTestMAGENTA() throws Exception {
		String source = """
                pixel MAGENTAPixel()<:
                           ^MAGENTA;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.MAGENTA.getRGB(), (int) result);
	}


	@Test
	void unitTestORANGE() throws Exception {
		String source = """
                pixel ORANGEPixel()<:
                           ^ORANGE;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.ORANGE.getRGB(), (int) result);
	}


	@Test
	void unitTestPINK() throws Exception {
		String source = """
                pixel PINKPixel()<:
                           ^PINK;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.PINK.getRGB(), (int) result);
	}


	@Test
	void unitTestRED() throws Exception {
		String source = """
                pixel REDPixel()<:
                           ^RED;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.RED.getRGB(), (int) result);
	}


	@Test
	void unitTestWHITE() throws Exception {
		String source = """
                pixel WHITEPixel()<:
                           ^WHITE;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.WHITE.getRGB(), (int) result);
	}


	@Test
	void unitTestYELLOW() throws Exception {
		String source = """
                pixel YELLOWPixel()<:
                           ^YELLOW;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(Color.YELLOW.getRGB(), (int) result);
	}


	@Test
	void unitTestExpandedPixelExpression() throws Exception {
		String source = """
                pixel Pixel()<:
                           ^[25, 50, 75];
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		showPixel((int) result);
		assertEquals(PixelOps.pack(25, 50, 75), (int) result);
	}


	@Test
	void unittestWriteStatement() throws Exception {
		String source = """
                void noRet()<:
                    write RED;
                    write 150;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName, source);
		assertNull(result);


		// To make sure the two compiled correctly, check for an instance of
		// ConsoleIO.writePixel( before ConsoleIO.write( in the java code


		// Obtain AST from parser
		edu.ufl.cise.cop4020fa23.ast.AST ast = ComponentFactory.makeParser(source).parse();
		// Type check and decorate AST with declaration and type info
		ast.visit(ComponentFactory.makeTypeChecker(), null);
		// Generate Java code
		String javaCode = (String) ast.visit(ComponentFactory.makeCodeGenerator(), packageName);


		int writePixelIndex = javaCode.indexOf("ConsoleIO.writePixel(");
		int writeIntIndex = javaCode.indexOf("ConsoleIO.write(");
		assertTrue(writePixelIndex < writeIntIndex);
	}


	@Test
	void unittestGetWidth() throws Exception {
		String source = """
                int scale(int w, int h)
                <:
                image[w,h] im0;
                im0 = RED;
                ^(width im0);
                :>
                """;
		int w = 512;
		int h = 256;


		int result = (int) PLCLangExec.runCode(packageName, source, w, h);
		assertEquals(result, 512);
	}


	@Test
	void unittestGetHeight() throws Exception {
		String source = """
                int scale(int w, int h)
                <:
                image[w,h] im0;
                im0 = RED;
                ^(height im0);
                :>
                """;
		int w = 512;
		int h = 256;


		int result = (int) PLCLangExec.runCode(packageName, source, w, h);
		assertEquals(result, 256);
	}
	// #endregion Tests by Gabriel Aldous

	// #region Tests by Christina Castillo
	@Test
	void unaryWidth() throws Exception {
		String source = """
                int f(string url)
                <:
                image i = url;
                image[50,100] j = i;
                ^width j;
                :>
                """;
		String url = testURL;
		int result = (int) PLCLangExec.runCode(packageName, source, url);
		assertEquals(result, 50);
		show(result);
	}


	@Test
	void unaryHeight() throws Exception {
		String source = """
                int f(string url)
                <:
                image i = url;
                image[50,100] j = i;
                ^height j;
                :>
                """;
		String url = testURL;
		int result = (int) PLCLangExec.runCode(packageName, source, url);
		assertEquals(result, 100);
		show(result);
	}


	@Test
	void doRunsTwice() throws Exception {
		String source = """
                int gcd(int a, int b)
                <:
                do
                   b< a -> <:  a = a -b; :>
                []
                   a < b -> <: b = b-a; :>
                od;
                ^a;
                :>
                """;
		int gcd = (Integer) PLCLangExec.runCode(packageName, source, 10, 15);
		assertEquals(5, gcd);
	}


	@Test
	void doRunsOnce() throws Exception {
		String source = """
                int test(int a)
                <:
                do
                   a == 0 -> <:  a = a - 1; :>
                od;
                ^a;
                :>
                """;
		int result = (Integer) PLCLangExec.runCode(packageName, source, 0);
		assertEquals(-1, result);
	}


	@Test
	void doRunsZeroTimes() throws Exception {
		String source = """
                int test(int a)
                <:
                do
                   a > 0 -> <:  a = a - 1; :>
                od;
                ^a;
                :>
                """;
		int result = (Integer) PLCLangExec.runCode(packageName, source, 0);
		assertEquals(0, result);
	}


	@Test
	void nestedDoStatements() throws Exception {
		String source = """
                int test(int a)
                <:
                do
                    a > 0 -> <:
                        a = a - 1;
                        do a>=1 -> <: a = a - 2; :> od;
                        a = a * 2;
                    :>
                od;
                ^a;
                :>
                """;
		int result = (Integer) PLCLangExec.runCode(packageName, source, 4);
		assertEquals(-2, result);
	}


	@Test
	void ifDoesNotRun() throws Exception {
		String source = """
                int f()
                <:
                int x = 3;
                if
                   x < 2 -> <: x=x+1; :>
                   fi;
                   ^x;
                :>
                """;
		int result = (Integer) PLCLangExec.runCode(packageName, source);
		assertEquals(3, result);
	}


	@Test
	void widthImplicitLoop() throws Exception {
		String source = """
                image checkerBoard(string url, int w, int h) <:
                   image[w,h] im0 = url;
                   im0[x,0] = RED;
                   ^im0;
                   :>
                   """;
		String url = testURL;
		int w = 200;
		int h = 300;
		BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
		BufferedImage expected = FileURLIO.readImage(url, w, h);
		for (int x = 0; x < w; x++) {
			expected.setRGB(x, 0, Color.red.getRGB());
		}
		compareImages(expected, output);
	}


	@Test
	void heightImplicitLoop() throws Exception {
		String source = """
                image checkerBoard(string url, int w, int h) <:
                   image[w,h] im0 = url;
                   im0[0,y] = RED;
                   ^im0;
                   :>
                   """;
		String url = testURL;
		int w = 200;
		int h = 300;
		BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
		BufferedImage expected = FileURLIO.readImage(url, w, h);
		for (int y = 0; y < h; y++) {
			expected.setRGB(0, y, Color.red.getRGB());
		}
		compareImages(expected, output);
	}


	@Test
	void noImplicitLoop() throws Exception {
		String source = """
                image checkerBoard(string url, int w, int h) <:
                   image[w,h] im0 = url;
                   im0[0,0] = RED;
                   ^im0;
                   :>
                   """;
		String url = testURL;
		int w = 200;
		int h = 300;
		BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
		BufferedImage expected = FileURLIO.readImage(url, w, h);
		expected.setRGB(0, 0, Color.red.getRGB());
		compareImages(expected, output);
	}


	@Test
	void assignUrlToImage() throws Exception {
		String source = """
                image test(string url, int w, int h) <:
                   image[w,h] im0;
                   im0 = url;
                   ^im0;
                   :>
                   """;
		String url = testURL;
		int w = 200;
		int h = 300;
		BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
		BufferedImage expected = FileURLIO.readImage(url, w, h);
		compareImages(expected, output);
	}


	// Note that I just updated this test. It was comparing against the wrong expected solution before, since I didn’t account for both shifts in dimensions.
	@Test
	void assignImageToImage() throws Exception {
		String source = """
                image test(string url, int w, int h) <:
                   image[w,h] im0 = url;
                   image[w/2,h/2] im1;
                   im1 = im0;
                   ^im1;
                   :>
                   """;
		String url = testURL;
		int w = 200;
		int h = 300;
		BufferedImage expected = FileURLIO.readImage(url, w, h);
		expected = ImageOps.copyAndResize(expected, w/2, h/2);
		BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
		compareImages(expected, output);
	}


	@Test
	void pixelsAreEqual() throws Exception {
		String source = """
                boolean test()<:
                pixel i = RED;
                pixel j = RED;
                           ^i == j;
                         :>
                """;
		Object result = PLCLangExec.runCode(packageName, source);
		assertEquals(true, (boolean) result);
	}


	@Test
	void pixelsAreNotEqual() throws Exception {
		String source = """
                boolean test()<:
                pixel i = RED;
                pixel j = BLUE;
                           ^i == j;
                         :>
                """;
		Object result = PLCLangExec.runCode(packageName, source);
		assertEquals(false, (boolean) result);
	}


	@Test
	void extractRedFromPixel() throws Exception {
		String source = """
                int example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im[0,0]:red;
                :>
                """;
		int w = 512;
		int h = 512;
		int result = (int) PLCLangExec.runCode(packageName, source, w, h);
		assertEquals(result, 50);
	}


	@Test
	void extractGreenFromPixel() throws Exception {
		String source = """
                int example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im[0,0]:green;
                :>
                """;
		int w = 512;
		int h = 512;
		int result = (int) PLCLangExec.runCode(packageName, source, w, h);
		assertEquals(result, 100);
	}


	@Test
	void extractBlueFromPixel() throws Exception {
		String source = """
                int example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im[0,0]:blue;
                :>
                """;
		int w = 512;
		int h = 512;
		int result = (int) PLCLangExec.runCode(packageName, source, w, h);
		assertEquals(result, 150);
	}


	@Test
	void setRedInPixel() throws Exception {
		String source = """
                int example() <:
                pixel p = RED;
                p:red = 0;
                ^p:red;
                :>
                """;
		int w = 512;
		int h = 512;
		int result = (int) PLCLangExec.runCode(packageName, source);
		assertEquals(result, 0);
	}


	@Test
	void setGreenInPixel() throws Exception {
		String source = """
                int example() <:
                pixel p = GREEN;
                p:green = 0;
                ^p:green;
                :>
                """;
		int w = 512;
		int h = 512;
		int result = (int) PLCLangExec.runCode(packageName, source);
		assertEquals(result, 0);
	}


	@Test
	void setBlueInPixel() throws Exception {
		String source = """
                int example() <:
                pixel p = BLUE;
                p:blue = 0;
                ^p:blue;
                :>
                """;
		int w = 512;
		int h = 512;
		int result = (int) PLCLangExec.runCode(packageName, source);
		assertEquals(result, 0);
	}


	@Test
	void extractRedFromImage() throws Exception {
		String source = """
                image example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im:red;
                :>
                """;
		int w = 512;
		int h = 512;
		BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, w, h);
		BufferedImage expected = ImageOps.makeImage(w, h);
		expected = ImageOps.setAllPixels(expected, PixelOps.pack(50, 0, 0));
		compareImages(expected, result);
	}


	// Although a function to do binary operations between images and pixels is given, type checker should not allow it.
	@Test
	void binaryImagePixelOp() throws Exception {
		String source = """
                image example(string url) <:
                image im = url;
                pixel p = [0, 0, 100];
                im = im - p;
                ^im;
                :>
                """;
		String url = testURL;
		TypeCheckException e = assertThrows(TypeCheckException.class, () -> PLCLangExec.runCode(packageName, source, url));
		System.out.println(e);
	}


	@Test
	void extractGreenFromImage() throws Exception {
		String source = """
                image example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im:green;
                :>
                """;
		int w = 512;
		int h = 512;
		BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, w, h);
		BufferedImage expected = ImageOps.makeImage(w, h);
		expected = ImageOps.setAllPixels(expected, PixelOps.pack(0, 100, 0));
		compareImages(expected, result);
	}


	@Test
	void extractBlueFromImage() throws Exception {
		String source = """
                image example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im:blue;
                :>
                """;
		int w = 512;
		int h = 512;
		BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, w, h);
		BufferedImage expected = ImageOps.makeImage(w, h);
		expected = ImageOps.setAllPixels(expected, PixelOps.pack(0, 0, 150));
		compareImages(expected, result);
	}


	@Test
	void binaryPackedPixelScalarOp() throws Exception {
		String source = """
                pixel test()<:
                           ^RED * 2;
                         :>


                """;
		Object result = PLCLangExec.runCode(packageName, source);
		int expected = ImageOps.binaryPackedPixelScalarOp(ImageOps.OP.TIMES, Color.RED.getRGB(), 2);
		assertEquals(expected, (int) result);
	}
	// #endregion Tests by Christina Castillo



}
