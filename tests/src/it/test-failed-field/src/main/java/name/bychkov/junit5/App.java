package name.bychkov.junit5;

public class App
{
	@CheckField(targetClass = SimpleDTO.class, value = "CONSTANT_5")
	private String field;
	
	public static void main(String[] args)
	{
	}
}