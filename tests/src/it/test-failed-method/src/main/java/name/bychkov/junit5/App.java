package name.bychkov.junit5;

public class App
{
	@CheckMethod(targetClass = SimpleDTO.class, value = "staticMethod8")
	public static void main(String[] args)
	{
	}
}