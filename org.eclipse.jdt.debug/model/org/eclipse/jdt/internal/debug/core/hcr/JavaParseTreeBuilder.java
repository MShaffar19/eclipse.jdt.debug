package org.eclipse.jdt.internal.debug.core.hcr;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Stack;

import org.eclipse.jdt.internal.compiler.SourceElementRequestorAdapter;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

class JavaParseTreeBuilder extends SourceElementRequestorAdapter implements ICompilationUnit {
	
	private static final boolean SHOW_COMPILATIONUNIT= true;

	private char[] fBuffer;
	private JavaNode fImportContainer;
	private Stack fStack= new Stack();
	
	/**
	 * Parsing is performed on the given buffer and the resulting tree
	 * (if any) hangs below the given root.
	 */
	JavaParseTreeBuilder(JavaNode root, char[] buffer) {
		fImportContainer= null;
		fStack.clear();
		fStack.push(root);
		fBuffer= buffer;
	}
			
	//---- ICompilationUnit
	/* (non Java doc)
	 * @see ICompilationUnit#getContents
	 */
	public char[] getContents() {
		return fBuffer;
	}
	
	/* (non Java doc)
	 * @see ICompilationUnit#getFileName
	 */
	public char[] getFileName() {
		return new char[0];
	}
	
	/* (non Java doc)
	 * @see ICompilationUnit#getMainTypeName
	 */
	public char[] getMainTypeName() {
		return new char[0];
	}
	
	/* (non Java doc)
	 * @see ICompilationUnit#getMainTypeName
	 */
	public char[][] getPackageName() {
		return null;
	}
	
	//---- ISourceElementRequestor
	
	public void enterCompilationUnit() {
		if (SHOW_COMPILATIONUNIT)
			push(JavaNode.CU, null, 0);
	}
	
	public void exitCompilationUnit(int declarationEnd) {
		if (SHOW_COMPILATIONUNIT)
			pop(declarationEnd);
	}
	
	public void acceptPackage(int declarationStart, int declarationEnd, char[] p3) {
		push(JavaNode.PACKAGE, null, declarationStart);
		pop(declarationEnd);
	}
	
	public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand) {
		int length= declarationEnd-declarationStart+1;
		if (fImportContainer == null)
			fImportContainer= new JavaNode(getCurrentContainer(), JavaNode.IMPORT_CONTAINER, null, declarationStart, length);
		String nm= new String(name);
		if (onDemand)
			nm+= ".*"; //$NON-NLS-1$
		new JavaNode(fImportContainer, JavaNode.IMPORT, nm, declarationStart, length);
		fImportContainer.setLength(declarationEnd-fImportContainer.getStart()+1);
	}
	
	public void enterClass(int declarationStart, int p2, char[] name, int p4, int p5, char[] p6, char[][] p7) {
		push(JavaNode.CLASS, new String(name), declarationStart);
	}
	
	public void exitClass(int declarationEnd) {
		pop(declarationEnd);
	}

	public void enterInterface(int declarationStart, int p2, char[] name, int p4, int p5, char[][] p6) {
		push(JavaNode.INTERFACE, new String(name), declarationStart);
	}
	
	public void exitInterface(int declarationEnd) {
		pop(declarationEnd);
	}
	
	public void acceptInitializer(int modifiers, int declarationSourceStart, int declarationSourceEnd) {
		push(JavaNode.INIT, getCurrentContainer().getInitializerCount(), declarationSourceStart);
		pop(declarationSourceEnd);
	}
	
	public void enterConstructor(int declarationStart, int p2, char[] name, int p4, int p5, char[][] parameterTypes, char[][] p7, char[][] p8) {
		push(JavaNode.CONSTRUCTOR, getSignature(name, parameterTypes), declarationStart);
	}
	
	public void exitConstructor(int declarationEnd) {
		pop(declarationEnd);
	}
	
	public void enterMethod(int declarationStart, int p2, char[] p3, char[] name, int p5, int p6, char[][] parameterTypes, char[][] p8, char[][] p9){
		push(JavaNode.METHOD, getSignature(name, parameterTypes), declarationStart);
	}
	
	public void exitMethod(int declarationEnd) {
		pop(declarationEnd);
	}
	
	public void enterField(int declarationStart, int p2, char[] p3, char[] name, int p5, int p6) {
		push(JavaNode.FIELD, new String(name), declarationStart);
	}
	
	public void exitField(int initializationStart, int declarationEnd) {
		pop(declarationEnd);
	}

	private JavaNode getCurrentContainer() {
		return (JavaNode) fStack.peek();
	}
	
	/**
	 * Adds a new JavaNode with the given type and name to the current container.
	 */
	private void push(int type, String name, int declarationStart) {
						
		while (declarationStart > 0) {
			char c= fBuffer[declarationStart-1];
			if (c != ' ' && c != '\t')
				break;
			declarationStart--;
		}
					
		fStack.push(new JavaNode(getCurrentContainer(), type, name, declarationStart, 0));
	}
	
	/**
	 * Closes the current Java node by setting its end position
	 * and pops it off the stack.
	 */
	private void pop(int declarationEnd) {
		JavaNode current= getCurrentContainer();
		current.setLength(declarationEnd - current.getStart() + 1);
		fStack.pop();
	}
	
	/**
	 * Builds a signature string from the given name and parameter types.
	 */
	public String getSignature(char[] name, char[][] parameterTypes) {
		StringBuffer buffer= new StringBuffer();
		buffer.append(name);
		buffer.append('(');
		if (parameterTypes != null) {
			for (int p= 0; p < parameterTypes.length; p++) {
				String parameterType= new String(parameterTypes[p]);
				
				// PR 1GF9WH7: ITPJUI:WINNT - Cannot replace main from local history
				// we only use the last component of a type name
				int pos= parameterType.lastIndexOf('.');
				if (pos >= 0)
					parameterType= parameterType.substring(pos+1);
				// end fix
				
				buffer.append(parameterType);
				if (p < parameterTypes.length-1)
					buffer.append(", "); //$NON-NLS-1$
			}
		}
		buffer.append(')');
		return buffer.toString();
	}
}
