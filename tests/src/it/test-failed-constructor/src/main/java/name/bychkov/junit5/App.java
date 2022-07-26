package name.bychkov.junit5;

public class App
{
	@CheckConstructor(targetClass = SimpleDTO.class)
	private String field;
	
	public static void main(String[] args)
	{
	}
}