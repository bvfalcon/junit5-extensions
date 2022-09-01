package name.bychkov.junit5.params;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.platform.commons.util.ReflectionUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class SubClassProducer<T> implements Opcodes
{
	private Method defineClass;
	private Class<T> superClass;
	
	SubClassProducer(Class<T> superClass)
	{
		this.superClass = superClass;
		try
		{
			defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
			defineClass.setAccessible(true);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private <E extends T> Class<E> createClassFromBytes(String name, byte[] bytes)
	{
		return (Class<E>) ReflectionUtils.invokeMethod(defineClass, this.getClass().getClassLoader(), name.replace('/', '.'), bytes, 0, bytes.length);
	}
	
	public <E extends T> Class<E> get(String nameSuffix, Class<?>[] params)
	{
		ClassNode superNode = createClassNode(superClass);
		Type[] paramTypes = Stream.of(params).map(Type::getType).toArray(Type[]::new);
		MethodNode constructor = findConstructor(superNode, paramTypes);
		String superClassInternalName = Type.getInternalName(superClass);
		String className = superClassInternalName + "_" + nameSuffix;
		
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, superClassInternalName, null);
		
		// Constructor
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, constructor.name, constructor.desc, null, null);
		int[] opcodes = createLoadOpcodes(constructor);
		for (int i = 0; i < opcodes.length; i++)
		{
			mv.visitVarInsn(opcodes[i], i);
		}
		mv.visitMethodInsn(INVOKESPECIAL, superClassInternalName, constructor.name, constructor.desc, false);
		mv.visitInsn(RETURN);
		int maxs = Stream.of(paramTypes).map(Type::getSize).reduce(1, Integer::sum).intValue();
		mv.visitMaxs(maxs, maxs);
		mv.visitEnd();
		
		byte[] byteCode = cw.toByteArray();
		return (Class<E>) createClassFromBytes(className, byteCode);
	}
	
	private static int[] createLoadOpcodes(MethodNode constructor)
	{
		Type[] argumentTypes = Type.getArgumentTypes(constructor.desc);
		
		int[] opcodes = new int[argumentTypes.length + 1];
		opcodes[0] = ALOAD;
		
		for (int i = 0; i < argumentTypes.length; i++)
		{
			opcodes[i + 1] = argumentTypes[i].getOpcode(ILOAD);
		}
		
		return opcodes;
	}
	
	private static MethodNode findConstructor(ClassNode node, Type[] paramTypes)
	{
		for (MethodNode method : node.methods)
		{
			if (method.name.equals("<init>"))
			{
				Type[] argumentTypes = Type.getArgumentTypes(method.desc);
				if (Arrays.equals(argumentTypes, paramTypes))
				{
					return method;
				}
			}
		}
		throw new RuntimeException(String.format("Constructor for class %s with params %s not found", node.name, Arrays.toString(paramTypes)));
	}
	
	private static ClassNode createClassNode(Class<?> clazz)
	{
		ClassNode node = new ClassNode();
		String fileName = Type.getInternalName(clazz) + ".class";
		try (InputStream classStream = clazz.getClassLoader().getResourceAsStream(fileName))
		{
			ClassReader reader = new ClassReader(classStream);
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
