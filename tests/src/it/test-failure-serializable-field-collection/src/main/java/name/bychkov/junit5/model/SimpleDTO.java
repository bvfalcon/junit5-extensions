package name.bychkov.junit5.model;

import java.io.Serializable;
import java.util.Collection;
import name.bychkov.junit5.UnserializableDTO;

public class SimpleDTO implements java.io.Serializable
{
	private Collection<UnserializableDTO> field;
}