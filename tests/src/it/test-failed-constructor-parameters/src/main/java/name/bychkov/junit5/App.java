package name.bychkov.junit5;

public class App
{
	@CheckConstructor(targetClass = SimpleDTO.class, parameters = { String.class, double.class })
	public static void main(String[] args)
	{
	}
}