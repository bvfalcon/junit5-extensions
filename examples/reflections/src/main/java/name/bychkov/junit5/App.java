package name.bychkov.junit5;

import java.util.function.Function;

@CheckConstructor(targetClass = SimpleDTO.class)
@CheckField(targetClass = ParentSimpleDTO.class, value = "CONSTANT_1")
@CheckConstructor(targetClass = ChildSimpleDTO.class)
@CheckMethod(targetClass = ChildSimpleDTO.class, value = "finalMethod4", returnType = void.class)
public class App
{
	@CheckMethod(targetClass = ParentSimpleDTO.class)
	private static final String METHOD_NAME = "staticMethod2";
	
	@CheckFields(targetClass = ParentSimpleDTO.class, values = { "variable3", "variable5" })
	@CheckField(targetClass = ParentSimpleDTO.class, value = "CONSTANT_5")
	@CheckConstructor(targetClass = ParentSimpleDTO.class)
	@CheckConstructor(targetClass = ParentSimpleDTO.class, parameters = { String.class, double[].class })
	@CheckMethod(targetClass = ParentSimpleDTO.class, value = "staticMethod3", returnType = void.class)
	private String field;
	
	@CheckField(targetClass = ParentSimpleDTO.class, value = "CONSTANT_1")
	@CheckConstructor(targetClass = ParentSimpleDTO.class, parameters = { String.class, double[].class })
	@CheckMethod(targetClass = ParentSimpleDTO.class, value = "method1WithParams", parameters = { Function.class, String.class, byte.class })
	@CheckField(targetClass = ChildSimpleDTO.class, value = "CONSTANT_5", type = Long.class)
	public App()
	{
	}
	
	@CheckField(targetClass = ChildSimpleDTO.class, value = "CONSTANT_5")
	@CheckMethod(targetClass = ChildSimpleDTO.class, value = "method4WithParams", returnType = String.class, parameters = { Function.class, String.class, byte.class })
	public static void main(String[] args) throws NoSuchMethodException, SecurityException
	{
	}
}