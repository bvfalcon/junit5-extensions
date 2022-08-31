package name.bychkov.junit5.params;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

// Original here: https://github.com/cubex2/customstuff4/blob/master/src/main/java/cubex2/cs4/util/AsmHelper.java
public class AsmHelper implements Opcodes
{
	private static Method defineClass;
	
	private AsmHelper()
	{
	}
	
	private static Class<?> createClassFromBytes(String name, byte[] bytes)
	{
		if (defineClass == null)
		{
			defineClass = getDefineClassMethod();
		}
		
		try
		{
			return (Class<?>) defineClass.invoke(AsmHelper.class.getClassLoader(), name.replace('/', '.'), bytes, 0, bytes.length);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static Method getDefineClassMethod()
	{
		Method defineClass = null;
		
		try
		{
			defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
			defineClass.setAccessible(true);
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		
		return defineClass;
	}
	
	public static <T, E extends T> Class<E> createSubClass(Class<T> superClass, String nameSuffix, Class<?>[] params)
	{
		ClassNode superNode = createClassNode(superClass);
		MethodNode constructor = findConstructor(superNode, params);
		String className = superClass.getName().replace('.', '/') + "_" + nameSuffix.replace(":", "_");
		
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, Type.getInternalName(superClass), null);
		
		// Constructor
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, constructor.name, constructor.desc, null, null);
		int[] opcodes = createLoadOpcodes(constructor);
		for (int i = 0; i < opcodes.length; i++)
		{
			mv.visitVarInsn(opcodes[i], i);
		}
		mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(superClass), constructor.name, constructor.desc, false);
		mv.visitInsn(RETURN);
		int doubleAndLongArgumentsCount = get2PlacesArgumentsCount(params);
		mv.visitMaxs(params.length + doubleAndLongArgumentsCount + 1, params.length + doubleAndLongArgumentsCount + 1);
		mv.visitEnd();
		
		byte[] byteCode = cw.toByteArray();
		return (Class<E>) createClassFromBytes(className, byteCode);
	}
	
	private static int get2PlacesArgumentsCount(Class<?>[] params)
	{
		// types double and long take 2 place in method/constructor parameters
		int result = 0;
		for (Class<?> param : params)
		{
			if (double.class.equals(param) || long.class.equals(param))
			{
				result++;
			}
		}
		return result;
	}
	
	private static int[] createLoadOpcodes(MethodNode method)
	{
		Type[] argumentTypes = Type.getArgumentTypes(method.desc);
		
		int[] opcodes = new int[argumentTypes.length + 1];
		opcodes[0] = ALOAD;
		
		for (int i = 0; i < argumentTypes.length; i++)
		{
			opcodes[i + 1] = argumentTypes[i].getOpcode(ILOAD);
		}
		
		return opcodes;
	}
	
	private static MethodNode findConstructor(ClassNode node, Class<?>[] params)
	{
		Type[] paramTypes = Stream.of(params).map(Type::getType).toArray(Type[]::new);
		for (MethodNode method : node.methods)
		{
			Type[] argumentTypes = Type.getArgumentTypes(method.desc);
			if (method.name.equals("<init>") && Arrays.equals(argumentTypes, paramTypes))
			{
				return method;
			}
		}
		
		return null;
	}
	
	private static ClassNode createClassNode(Class<?> clazz)
	{
		ClassNode node = new ClassNode();
		try
		{
			String fileName = clazz.getName().replace('.', '/') + ".class";
			ClassReader reader = new ClassReader(clazz.getClassLoader().getResourceAsStream(fileName));
			reader.accept(node, 0);
			
			return node;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		throw new RuntimeException("Couldn't create ClassNode for class " + clazz.getName());
	}
}
