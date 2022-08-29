package name.bychkov.junit5;

public class App
{
	@CheckKey(baseName = "Messages", locale = "de")
	private static final String KEY_CONSTANT = "title";
	
	@CheckKey(value = "title", locale = "de")
	@CheckKeys(values = { "title", "header" }, locale = "de")
	@CheckResourceBundle(locales = { "", "de" })
	private static final String RESOURCE_BUNDLE_CONSTANT = "Messages";
}