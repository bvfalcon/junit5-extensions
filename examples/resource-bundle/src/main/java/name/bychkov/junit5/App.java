package name.bychkov.junit5;

import java.util.ResourceBundle;

@CheckResourceBundle(baseName = "Messages", locales = { "", "de" })
public class App
{
	private static final String RESOURCE_BUNDLE_BASE_NAME = "Messages";
	private static final String RESOURCE_BUNDLE_BODY_KEY = "body";
	
	@CheckKey(baseName = RESOURCE_BUNDLE_BASE_NAME, value = RESOURCE_BUNDLE_BODY_KEY)
	public static void main(String[] args)
	{
		String body = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME).getString(RESOURCE_BUNDLE_BODY_KEY);
		System.out.println(body);
	}
}