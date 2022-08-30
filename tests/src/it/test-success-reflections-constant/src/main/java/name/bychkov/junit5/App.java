package name.bychkov.junit5;

public class App
{
	@CheckField(targetClass = SimpleDTO.class)
	private static final String FIELD_CONSTANT = "field";

	@CheckMethod(targetClass = SimpleDTO.class)
	private static final String METHOD_CONSTANT = "getField";

}