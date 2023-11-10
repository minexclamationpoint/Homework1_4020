/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */

package edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute;

import edu.ufl.cise.cop4020fa23.ComponentFactory;
import edu.ufl.cise.cop4020fa23.ast.Program;

public class PLCLangExec {
	

	public static Object runCode(String packageName, String plcLanguageSource, Object...params) throws Exception {
		//Obtain AST from parser
		edu.ufl.cise.cop4020fa23.ast.AST ast = ComponentFactory.makeParser(plcLanguageSource).parse();
		//Type check and decorate AST with declaration and type info
		ast.visit(ComponentFactory.makeTypeChecker(), null);
		//Construct fully qualified class name		
		String className = ((Program)ast).getName();
		String fullyQualifiedName = packageName != "" ? packageName + '.' + className : className;
		//Generate Java code
		String javaCode = (String) ast.visit(ComponentFactory.makeCodeGenerator(), packageName);
		//Display generated code if VERBOSE is set
		System.out.println(javaCode);
		//Invoke Java compiler to obtain classfile 
		byte[] byteCode = DynamicCompiler.compile(fullyQualifiedName, javaCode);
		//Load generated classfile and execute its "apply" method.
		Object result = DynamicClassLoader.loadClassAndRunMethod(byteCode, fullyQualifiedName, "apply", params);
		return result;
	}
}
