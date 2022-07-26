package name.bychkov.junit5;

public class App
{
	@CheckKeys(baseName = "Messages", values = { "title", "header", "content" })
	public static void main(String[] args)
	{
	}
}