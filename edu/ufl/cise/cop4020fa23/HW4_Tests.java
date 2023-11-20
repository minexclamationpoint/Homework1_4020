package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.DynamicClassLoader;
import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.DynamicCompiler;
import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.PLCLangExec;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import edu.ufl.cise.cop4020fa23.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;


class HW4_Tests {
	

	@AfterEach
	public void separatingLine(){
		show("----------------------------------------------");
	}

	// makes it easy to turn output on and off (and less typing than System.out.println)
	static final boolean VERBOSE = true;


	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}

	@Test
	void cg0() throws Exception {
		String input = "void test()<: int a = 2;:>";
		Object result = PLCLangExec.runCode(packageName, input);
		show(result);
		assertNull(result);
	}
	
	@Test
	void cg1() throws Exception {
		String input = """
				string test()<: ^ "hello";  :>
				""";
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals("hello",(String)result);
	}
	
	
	@Test
	void cg2() throws Exception {
		String input = """
				boolean test(boolean true) ##false is an identifier
				<: ^ true; 
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,input,  true);
		assertEquals(true, (boolean)result);
	}	
	
	@Test
	void cg3() throws Exception {
		String input = """
				boolean test(boolean true)
				<: ^ true; 
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,input,  false);
		assertEquals(false, (boolean)result);
	}	
	

	@Test
	void cg4() throws Exception {
		String input = """
				int test(int a, string Hello, boolean b)
				<: 
				write a;
				write Hello;
				write b;
				^ a;
				:>
				""";
		Object[] params = {4,"hello",true};
		Object result = PLCLangExec.runCode(packageName,input, 4, "hello", true);
		show(result);
		assertEquals(4, result);
	}	
	
	
	@Test
	void cg5() throws Exception {
		String input = """
				int test(int b)
				<: 
				write b;
				^b+3;
				:>
				""";
		Object result =  PLCLangExec.runCode(packageName,input, 7);
		assertEquals(10,(int)result);
	}	
	
	@Test
	void cg6() throws Exception {
		String input = """
				int test(int one, int two)
				<:
				^ two ** one;
				:>
				""";
		Object result =  PLCLangExec.runCode(packageName,input, 3, 2);
		show(result);
		assertEquals(8,(int)result);
	}
	
	String packageName = "edu.ufl.cise.cop4020fa23";
	@Test
	void cg7() throws Exception {
		String input = """
				string test(string x, string y)
				<: 
				^x+y;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,input, "4","5");
		show(result);
		assertEquals("45",result);

	}	
	
	@Test 
	void cg8() throws Exception {
		String source = """
				int test(int b)
				<:
				^ -b;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, -20);
		show(result);
		assertEquals(20,(int)result);
	}
	
	@Test 
	void cg9() throws Exception {
		String source = """
				int f(int a)
				<:
				^ -(a+10);
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, -10);
		show(result);
		assertEquals(0,(int)result);
	}
		
	@Test 
	void cg10() throws Exception {
		String source = """
				int test(int b)
				<:
				^ -(-b-10);
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, 10);
		show(result);
		assertEquals(20,(int)result);
	}
	
	@Test 
	void cg11() throws Exception {
		String source = """
				boolean test(boolean b)
				<:
				^ !b;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, false);
		show(result);
		assertEquals(true,(boolean)result);
	}
	
	@Test 
	void cg12() throws Exception {
		String source = """
				boolean test(boolean b)
				<:
				^ !!b;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, false);
		show(result);
		assertEquals(false,(boolean)result);
	}
	
	@Test
	void cg13() throws Exception {
		String source = """
				int test(int i)
				<:
				  int r = ? i>0 -> i , -i;
				  ^r;
				  :>
				  """;
		Object result = PLCLangExec.runCode(packageName, source, -45);
		show(result);
		assertEquals(45,(int)result);
	}
	
	@Test
	void cg14() throws Exception {
		String source = """
				string a(int i)
				<:
				  string r = ? i>0 -> "positive" , "negative";
				  ^r;
				  :>
				  """;
		Object result = PLCLangExec.runCode(packageName, source, -42);
		show(result);
		assertEquals("negative",result);
}
	
	@Test
	void cg15() throws Exception {
		String source = """
				int test(int i)
				<:
				int j;
				j = i + 5;
				i = j;
				^i;
				:>
				""";
		int val = 34;
		Object result =  PLCLangExec.runCode(packageName,source, val);
		show(result);
		assertEquals(39,(int)result);
	}
	
	@Test
	void cg16() throws Exception {
		String source = """
				boolean test(boolean a)
				<:
				boolean b;
				b = !a;
				^b;
				:>
				""";
		Object result =  PLCLangExec.runCode(packageName,source, true);
		show(result);
		assertEquals(false,result);
	}
		
	@Test
	void cg17() throws Exception {
		String source = """
				boolean f()
				<:
				boolean b = TRUE;
				b = !b;
				^b;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(false, (boolean)result);
	}
	
	@Test
	void cg18() throws Exception {
		String source = """
				int test()
				<:
				  int i = 1;
				  int j;
				  <: 
				     int i = 2;
				     <: 
				         int i = 3;
				         j=i;
				     :>;
				     j = i;
				  :>;
				  j = i;
				  ^j;
				:>

				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(1, (int)result);
	}
	
	@Test
	void cg19() throws Exception {
		String source = """
				int f()
				<:
				  int i = 1;
				  int j;
				  <: 
				     int i = 2;
				     <: 
				         i = 3;
				        
				     :>;
				      j=i;
				  :>;
				  ^j;
				:>

				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(3, (int)result);
	}
		
	@Test
	void cg20() throws Exception {
		String source = """
				int f()
				<:
				  int i = 1;
				  int j;
				  <: 
				     int i = 2;
				     <: 
				         int i = 3 * i;
				         j = i;
                    :>;
                    j = i * j;			      
				  :>;
				  ^j;
				:>

				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(12, (int)result);
	}
		
	@Test
	void cg21() throws Exception {
		String source = """
				string concatWithSpace(string a, string b)
				<:
				^ a + " " + b;
				:>
				""";
		String a = "Go";
	    String b = "Gators!";
		Object result = PLCLangExec.runCode(packageName,source,a,b);
		show(result);
		assertEquals(a + " " + b, result);		
	}

	@Test
	void cg22() throws Exception {
		String source = """
				void output() <:
				write "hello";
				:>
				""";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream test = new PrintStream(baos);
		ConsoleIO.setConsole(test);
		Object result = PLCLangExec.runCode(packageName, source);
		show(result);
		String output = baos.toString();
		assertEquals(null, result);
		assertTrue(output.equals("hello\n") || output.equals("hello\r\n"));
	}

	@Test
	void cg23() throws Exception {
		String source = """
				void output() <:
				write 2;
				:>
				""";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream test = new PrintStream(baos);
		ConsoleIO.setConsole(test);
		Object result = PLCLangExec.runCode(packageName, source);
		show(result);
		String output = baos.toString();
		assertEquals(null, result);
		assertTrue(output.equals("2\n") || output.equals("2\r\n"));
	}

	@Test
	void cg24() throws Exception {
		String source = """
				void output(string a, string b) <:
				string c = a + b;
				write c;
				:>
				""";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream test = new PrintStream(baos);
		ConsoleIO.setConsole(test);
		Object result = PLCLangExec.runCode(packageName, source, "hello ", "world");
		show(result);
		String output = baos.toString();
		assertEquals(null, result);
		assertTrue(output.equals("hello world\n") || output.equals("hello world\r\n"));
	}

	@Test
	void cg25() throws Exception {
		String source = """
				boolean test(boolean a, boolean b) <:
				boolean c = a || b;
				^ c;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, true, false);
		show(result);
		assertEquals(true, result);
	}

	@Test
	void cg26() throws Exception {
		String source = """
				boolean test(string a, string b) <:
				boolean c = a == b;
				^ c;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, "hello", "hello");
		show(result);
		assertEquals(true, result);
	}

	@Test
	void cg27() throws Exception {
		String source = """
				boolean test(int a, int b) <:
				boolean c = a < b;
				^ c;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, 5, 6);
		show(result);
		assertEquals(true, result);
	}

	@Test
	void cg28() throws Exception {
		String source = """
				boolean test(int a, int b) <:
				boolean c = a <= b;
				^ c;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, 5, 5);
		show(result);
		assertEquals(true, result);
	}

	@Test
	void cg29() throws Exception {
		String source = """
				boolean test(int a, int b) <:
				boolean c = a == b;
				^ c;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, 5, 5);
		show(result);
		assertEquals(true, result);
	}
}
