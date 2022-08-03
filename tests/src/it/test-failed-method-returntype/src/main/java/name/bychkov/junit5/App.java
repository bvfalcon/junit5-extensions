package name.bychkov.junit5;

public class App
{
	@CheckMethod(targetClass = SimpleDTO.class, value = "staticMethod2", returnType = double.class)
	public static void main(String[] args)
	{
	}
}