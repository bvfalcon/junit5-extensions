package name.bychkov.junit5;

import java.io.Serializable;
import java.util.Locale;
import java.util.function.Function;

public class ParentSimpleDTO implements Serializable
{
	private static final String CONSTANT_1 = "test constant 1";
	protected static final String CONSTANT_2 = "test constant 2";
	static final String CONSTANT_3 = "test constant 3";
	public final String CONSTANT_4 = "test constant 4";
	
	private static Long CONSTANT_5 = 76L;
	protected static String CONSTANT_6 = "test constant 6";
	static String CONSTANT_7 = "test constant 7";
	public String CONSTANT_8 = "test constant 8";
	
	private final String variable1 = "test constant 9";
	protected final String variable2 = "test constant 10";
	final String variable3 = "test constant 11";
	public final String variable4 = "test constant 12";
	
	private String variable5 = "test constant 9";
	protected String variable6 = "test constant 10";
	String variable7 = "test constant 11";
	public String variable8 = "test constant 12";
	
	private static final void staticMethod1()
	{
	}
	
	protected static final String staticMethod2()
	{
		return "";
	}
	
	static final void staticMethod3()
	{
	}
	
	public static final void staticMethod4()
	{
	}
	
	private static void staticMethod5()
	{
	}
	
	protected static void staticMethod6()
	{
	}
	
	static void staticMethod7()
	{
	}
	
	public static void staticMethod8()
	{
	}
	
	private final void finalMethod1()
	{
	}
	
	protected final void finalMethod2()
	{
	}
	
	final Locale finalMethod3()
	{
		return Locale.getDefault();
	}
	
	public final void finalMethod4()
	{
	}
	
	private void method1()
	{
	}
	
	protected ParentSimpleDTO method2()
	{
		return this;
	}
	
	void method3()
	{
	}
	
	public void method4()
	{
	}
	
	private void method1WithParams(Function<String, String> param1, String param2, byte param3)
	{
	}
	
	protected void method2WithParams(Function<String, String> param1, String param2, byte param3)
	{
	}
	
	void method3WithParams(Function<String, String> param1, String param2, byte param3)
	{
	}
	
	public String method4WithParams(Function<String, String> param1, String param2, byte param3)
	{
		return "";
	}
	
	public ParentSimpleDTO()
	{
	}
	
	private ParentSimpleDTO(String param1)
	{
	}
	
	protected ParentSimpleDTO(String param1, float param2)
	{
	}
	
	ParentSimpleDTO(String param1, double[] param2)
	{
	}
}