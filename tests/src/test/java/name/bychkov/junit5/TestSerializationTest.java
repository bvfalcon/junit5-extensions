package name.bychkov.junit5;

import static name.bychkov.junit5.SerializationTest.getGenericArgumentClass;
import static name.bychkov.junit5.SerializationTest.hasInterface;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;

import javax.naming.CompositeName;

import org.junit.jupiter.api.Test;

public class TestSerializationTest
{
	@Test
	public void testHasInterface()
	{
		assertTrue(hasInterface(Serializable.class, Serializable.class));
		assertTrue(hasInterface(HashMap.class, Serializable.class));
		assertTrue(hasInterface(CompositeName.class, Serializable.class));
		assertTrue(hasInterface(Long.class, Serializable.class));
		assertTrue(hasInterface(HashSet.class, Iterable.class));
		
		assertFalse(hasInterface(Object.class, Serializable.class));
		assertFalse(hasInterface(ServiceLoader.class, Serializable.class));
	}

	private static class TestGeneric<T extends Serializable, E extends Serializable & Cloneable>
	{
		List field1;
		List<?> field2;
		List<String> field3;
		List<T> field4;
		List<E> field5;
		
		List<List> field6;
		List<List<?>> field7;
		List<List<String>> field8;
		List<List<T>> field9;
		List<List<E>> field10;
	}
	
	@Test
	public void testGetGenericArgumentClass() throws NoSuchFieldException, SecurityException
	{
		assertNull(getGenericArgumentClass(getType("field1"), 0));
		assertNull(getGenericArgumentClass(getType("field2"), 0));
		assertEquals(String.class, getGenericArgumentClass(getType("field3"), 0));
		assertNull(getGenericArgumentClass(getType("field4"), 0));
		assertNull(getGenericArgumentClass(getType("field5"), 0));
		assertEquals(List.class, getGenericArgumentClass(getType("field6"), 0));
		assertEquals(List.class, getGenericArgumentClass(getType("field7"), 0));
		assertEquals(List.class, getGenericArgumentClass(getType("field8"), 0));
		assertEquals(List.class, getGenericArgumentClass(getType("field9"), 0));
		assertEquals(List.class, getGenericArgumentClass(getType("field10"), 0));
	}
	
	private Type getType(String fieldName) throws NoSuchFieldException, SecurityException
	{
		return TestGeneric.class.getDeclaredField(fieldName).getGenericType();
	}
}