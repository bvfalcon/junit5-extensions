package name.bychkov.junit5.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ParentSimpleDTO implements Serializable
{
	private static final long serialVersionUID = 3852683847047209754L;

	private Integer intField;
	
	private List<String> listField;
	
	private List<List<String>> list2Field;
	
	private Map<Long, Double> mapField;
	
	private byte byteField;
	
	private List<ChildSimpleDTO> children;
}