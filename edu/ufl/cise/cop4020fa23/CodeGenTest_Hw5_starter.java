package edu.ufl.cise.cop4020fa23;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.PLCLangExec;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;

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
	static final boolean WAIT_FOR_INPUT = true;

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
	 * static int apply(){ int p0$2=0xff0000ff; int p1$2=0xff00ff00; return
	 * (ImageOps.binaryPackedPixelPixelOp(ImageOps.OP.PLUS,p0$2,p1$2)); } }
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
	 * ConsoleIO.writePixel(pred$2); pred$2=PixelOps.setGreen(pred$2,255);
	 * ConsoleIO.write("after assignment to green channel");
	 * ConsoleIO.writePixel(pred$2); return pred$2; } }
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
	 * package edu.ufl.cise.cop4020fa23; import java.awt.image.BufferedImage; import
	 * edu.ufl.cise.cop4020fa23.runtime.PixelOps; import
	 * edu.ufl.cise.cop4020fa23.runtime.ImageOps; public class example{ public
	 * static BufferedImage apply(int w$1, int h$1){ final BufferedImage
	 * im$2=ImageOps.makeImage(w$1,h$1); for (int x$3=0; x$3<im$2.getWidth();x$3++){
	 * for (int y$3=0; y$3<im$2.getHeight();y$3++){
	 * ImageOps.setRGB(im$2,x$3,y$3,PixelOps.pack(x$3,y$3,255)); } }; return im$2; }
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

}
