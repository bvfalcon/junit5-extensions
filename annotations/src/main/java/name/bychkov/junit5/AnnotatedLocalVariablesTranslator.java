package name.bychkov.junit5;

import java.util.Vector;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

public class AnnotatedLocalVariablesTranslator extends TreeTranslator
{
	private Vector<JCTree.JCVariableDecl> localVariableDeclatations;
	
	public AnnotatedLocalVariablesTranslator(Vector<JCTree.JCVariableDecl> localVariableDeclatations)
	{
		this.localVariableDeclatations = localVariableDeclatations;
	}
	
	@Override
	public void visitVarDef(final JCTree.JCVariableDecl variableDeclaration)
	{
		super.visitVarDef(variableDeclaration);
		if (shouldBeMadeFinal(variableDeclaration, variableDeclaration.getModifiers()))
		{
			localVariableDeclatations.add(variableDeclaration);
		}
		this.result = variableDeclaration;
	}
	
	private boolean shouldBeMadeFinal(final JCTree.JCVariableDecl variableDeclaration,
			final JCTree.JCModifiers modifiers)
	{
		if (!isLocalVariable(variableDeclaration))
		{
			return false;
		}
		boolean result = isCheckConstructorAnnotation(modifiers);
		if (result)
		{
			System.out.println("variableDeclaration startPos: " + variableDeclaration.getType().getStartPosition());
			System.out.println("variableDeclaration name: " + variableDeclaration.getName());
		}
		return result;
	}
	
	private boolean isCheckConstructorAnnotation(JCTree.JCModifiers modifiers)
	{
		boolean result = modifiers.toString().contains("@" + CheckConstructor.class.getSimpleName());
		System.out.println("result: " + result);
		return result;
	}
	
	private boolean isLocalVariable(JCTree.JCVariableDecl variableDeclaration)
	{
		return variableDeclaration.sym == null;
	}
}