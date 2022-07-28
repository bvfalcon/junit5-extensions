package name.bychkov.junit5.model;

import java.util.Collection;
import java.util.Set;

public class ChildSimpleDTO extends ParentSimpleDTO
{
	private static final long serialVersionUID = 5944957859242597676L;
	
	private ParentSimpleDTO parent;
	
	private Collection<?> collectionField;
	
	private Set setField;
}