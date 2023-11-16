package edu.ufl.cise.cop4020fa23;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.DynamicClassLoader;
import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.DynamicCompiler;
import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.PLCLangExec;
import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.Program;
import edu.ufl.cise.cop4020fa23.ast.ASTVisitor;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;

class CodeGenTest_starter {
	

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
	
	/**This is a test of dynamic compilation and execution.  We hard code a legal Java class. **/
	@Test
	void test() throws Exception {
		String code = """
				public class Class1 {
				   public static int f(int x){
				     return x+1;
				   }
				 }
				""";
		String name = "Class1";
		byte[] byteCode = DynamicCompiler.compile(name, code);
		//Load generated classfile and execute its apply method.
		Object[] params = {3};
		Object result = (int) DynamicClassLoader.loadClassAndRunMethod(byteCode, name, "f", params);
		show(result);
		assertEquals(4, (int)result);
	}
	
	@Test
	void cg0() throws Exception {
		System.out.println("meowwwwww");
		String input = "void f()<::>";
		Object result = PLCLangExec.runCode(packageName, input);
		show(result);
		assertNull(result);
	}
	
	@Test
	void cg1() throws Exception {
		String input = """
				int f()<: ^ 3;  :>
				""";
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(3,(int)result);
	}
	
	
	@Test
	void cg2() throws Exception {
		String input = """
				boolean f(boolean false) ##false is an identifier
				<: ^ false; 
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,input,  false);
		assertEquals(false, (boolean)result);	
	}	
	
	@Test
	void cg3() throws Exception {
		String input = """
				boolean f(boolean false)
				<: ^ false; 
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,input,  true);
		assertEquals(true, (boolean)result);	
	}	
	

	@Test
	void cg4() throws Exception {
		String input = """
				string f(int a, string Hello, boolean b)
				<: 
				write a;
				write Hello;
				write b;
				^ Hello;
				:>
				""";
		Object[] params = {4,"hello",true};
		Object result = PLCLangExec.runCode(packageName,input, 4, "hello", true);
		show(result);
		assertEquals("hello", result);		
	}	
	
	
	@Test
	void cg5() throws Exception {
		String input = """
				int f(int a)
				<: 
				write a;
				^a+1;
				:>
				""";
		Object result =  PLCLangExec.runCode(packageName,input, 4);
		assertEquals(5,(int)result);		
	}	
	
	@Test
	void cg6() throws Exception {
		String input = """
				int f(int a, int b)
				<:
				^ a ** b;
				:>
				""";
		Object result =  PLCLangExec.runCode(packageName,input, 3, 2);
		show(result);
		assertEquals(9,(int)result);			
	}
	
	String packageName = "edu.ufl.cise.cop4020fa23";
	@Test
	void cg7() throws Exception {
		String input = """
				int Example(int x, int y)
				<: 
				^x+y;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName,input, 4,5);
		show(result);
		assertEquals(9,(int) result);	

	}	
	
	@Test 
	void cg8() throws Exception {
		String source = """
				int f(int a)
				<:
				^ -a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, 10);
		show(result);
		assertEquals(-10,(int)result);
	}
	
	@Test 
	void cg9() throws Exception {
		String source = """
				int f(int a)
				<:
				^ -a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, -10);
		show(result);
		assertEquals(10,(int)result);
	}
		
	@Test 
	void cg10() throws Exception {
		String source = """
				int f(int a)
				<:
				^ --a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, 10);
		show(result);
		assertEquals(10,(int)result);
	}
	
	@Test 
	void cg11() throws Exception {
		String source = """
				boolean f(boolean a)
				<:
				^ !a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, true);
		show(result);
		assertEquals(false,(boolean)result);
	}
	
	@Test 
	void cg12() throws Exception {
		String source = """
				boolean f(boolean a)
				<:
				^ !!a;
				:>
				""";
		Object result = PLCLangExec.runCode(packageName, source, true);
		show(result);
		assertEquals(true,(boolean)result);
	}
	
	@Test
	void cg13() throws Exception {
		String source = """
				int a(int i)
				<:
				  int r = ? i>0 -> i , -i;
				  ^r;
				  :>
				  """;
		Object result = PLCLangExec.runCode(packageName, source, 42);
		show(result);
		assertEquals(42,(int)result);
	}
	
	@Test
	void cg14() throws Exception {
		String source = """
				int a(int i)
				<:
				  int r = ? i>0 -> i , -i;
				  ^r;
				  :>
				  """;
		Object result = PLCLangExec.runCode(packageName, source, -42);
		show(result);
		assertEquals(42,(int)result);
}
	
	@Test
	void cg15() throws Exception {
		String source = """
				int f(int a)
				<:
				int b;
				b = a;
				^b;
				:>
				""";
		int val = 34;
		Object result =  PLCLangExec.runCode(packageName,source, val);
		show(result);
		assertEquals(val,(int)result);
	}
	
	@Test
	void cg16() throws Exception {
		String source = """
				int f(int a)
				<:
				int b;
				b = -a;
				^b;
				:>
				""";
		Object result =  PLCLangExec.runCode(packageName,source, 22);
		show(result);
		assertEquals(-22,(int)result);
	}
		
	@Test
	void cg17() throws Exception {
		String source = """
				boolean f(boolean a)
				<:
				boolean b;
				b = !a;
				^b;
				:>
				""";
		boolean val = true;
		Object result = PLCLangExec.runCode(packageName,source, val);
		show(result);
		assertEquals(!val, (boolean)result);
	}
	
	@Test
	void cg18() throws Exception {
		String source = """
				int f()
				<:
				  int a = 1;
				  int b;
				  <: 
				     int a = 2;
				     <: 
				         int a = 3;
				         b=a;
				     :>;
				  :>;
				  ^b;
				:>

				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(3, (int)result);
	}
	
	@Test
	void cg19() throws Exception {
		String source = """
				int f()
				<:
				  int a = 1;
				  int b;
				  <: 
				     int a = 2;
				     <: 
				         int a = 3;
				        
				     :>;
				      b=a;
				  :>;
				  ^b;
				:>

				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(2, (int)result);
	}
		
	@Test
	void cg20() throws Exception {
		String source = """
				int f()
				<:
				  int a = 1;
				  int b;
				  <: 
				     int a = 2;
				     <: 
				         int a = 3;
                    :>;			      
				  :>;
				  b=a;
				  ^b;
				:>

				""";
		Object result = PLCLangExec.runCode(packageName,source);
		show(result);
		assertEquals(1, (int)result);
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
	void unitTestBooleanLit() throws Exception {
		String input = """
                boolean func()
                <: ^ FALSE;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(false, (boolean)result);
	}


	@Test
	void unitTestLogicOr() throws Exception {
		String input = """
                boolean func()
                <: ^ FALSE || TRUE;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(true, (boolean)result);
	}


	@Test
	void unitTestLogicAnd() throws Exception {
		String input = """
                boolean func()
                <: ^ FALSE && TRUE;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(false, (boolean)result);
	}


	@Test
	void unitTestNestedExpression() throws Exception {
		String input = """
                int func()
                <: ^ (2 + 4)  * 3;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(18, (int)result);
	}


	@Test
	void unitTestExponentialExpression() throws Exception {
		String input = """
                int func()
                <: ^ 2 ** 3;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(8, (int)result);
	}


	@Test
	void unitTestStringEquals() throws Exception {
		String input = """
                boolean func(string greeting)
                <: ^ greeting == "hello";
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input, "hello");
		assertEquals(true, (boolean)result);
	}


	@Test
	void unitTestNumLitEquals() throws Exception {
		String input = """
                boolean func(int i)
                <: ^ i == 2;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input, 2);
		assertEquals(true, (boolean)result);
	}




	@Test
	void unitTestDivisionExpression() throws Exception{
		String input = """
                int func()
                <: ^ 4 / 2;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(2, (int)result);
	}


	@Test
	void unitTestModExpression() throws Exception{
		String input = """
                int func()
                <: ^ 4 % 2;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(0, (int)result);
	}


	@Test
	void unitTestComparisonExpression() throws Exception{
		String input = """
                boolean func()
                <: ^ 4 > 2;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(true, (boolean)result);
	}


	@Test
	void unitTestComparisonExpression2() throws Exception{
		String input = """
                boolean func()
                <: ^ 4 < 2;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(false, (boolean)result);
	}


	@Test
	void unitTestComparisonExpression3() throws Exception{
		String input = """
                boolean func()
                <: ^ 4 >= 2;
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(true, (boolean)result);
	}


	@Test
	void unitTestComparisonExpression4() throws Exception{
		String input = """
                boolean func()
                <: ^ 4 <= 2; 
                :>
                """;
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(false, (boolean)result);
	}

	@Test
	void nestedComparisonExpressions() throws Exception{
		String input = """
				boolean func()
				<: ^ ((2 < 1) == (3 < 2));
				:> 
				""";
		Object result = PLCLangExec.runCode(packageName,input);
		assertEquals(true, (boolean)result);
	}
}
