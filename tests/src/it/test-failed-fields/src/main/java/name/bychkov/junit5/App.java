package name.bychkov.junit5;

public class App
{
	@CheckFields(targetClass = SimpleDTO.class, values = { "variable3", "variable5", "variable27" })
	public static void main(String[] args)
	{
	}
}