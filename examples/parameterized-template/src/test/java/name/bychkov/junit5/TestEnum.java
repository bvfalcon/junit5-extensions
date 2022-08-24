package name.bychkov.junit5;

public enum TestEnum
{
	TEST1("test-value-enum-1"),
	TEST2("test-value-enum-2");
	
	private String testValue;
	
	private TestEnum(String testValue)
	{
		this.testValue = testValue;
	}
	
	public String getTestValue()
	{
		return testValue;
	}
}
