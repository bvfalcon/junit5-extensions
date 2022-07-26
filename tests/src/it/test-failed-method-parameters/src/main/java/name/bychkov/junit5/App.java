package name.bychkov.junit5;

public class App
{
	@CheckMethod(targetClass = SimpleDTO.class, value = "staticMethod2", parameters = { String.class, double.class })
	public static void main(String[] args)
	{
	}
}