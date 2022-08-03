package name.bychkov.junit5;

public class App
{
	@CheckField(targetClass = SimpleDTO.class, value = "CONSTANT_1", type = double.class)
	private String field;
	
	public static void main(String[] args)
	{
	}
}