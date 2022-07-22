package name.bychkov.junit5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.BiFunction;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

public class ResourceBundleTests extends AbstractTests
{
	private static final Logger LOG = LoggerFactory.getLogger(ResourceBundleTests.class);
	
	private static final BiFunction<Throwable, CheckAnnotationProcessor.CheckKeyObject, AssertionFailedError> keyExceptionProducer = (e, keyObject) ->
			createAssertionFailedError(keyObject.message, e, "Annotation @%s on %s warns: ResourceBundle with baseName %s has no key %s",
			CheckKey.class.getSimpleName(), keyObject.annotatedElement, keyObject.baseName, keyObject.value);
	
	@TestFactory
	public Collection<DynamicTest> testResourceBundles()
	{
		Collection<DynamicTest> tests = new ArrayList<>();
		Collection<Serializable> annotationClasses = readFile();
		
		for (Serializable item : annotationClasses)
		{
			DynamicTest test = null;
			if (item instanceof CheckAnnotationProcessor.CheckKeyObject)
			{
				CheckAnnotationProcessor.CheckKeyObject keyObject = (CheckAnnotationProcessor.CheckKeyObject) item;
				test = getDynamicKeyTest(keyObject);
			}
			else if (item instanceof CheckAnnotationProcessor.CheckResourceBundleObject)
			{
				CheckAnnotationProcessor.CheckResourceBundleObject resourceBundleObject = (CheckAnnotationProcessor.CheckResourceBundleObject) item;
				test = getDynamicResourceBundleTest(resourceBundleObject);
			}
			if (test != null)
			{
				tests.add(test);
			}
		}
		
		return tests;
	}
	
	private DynamicTest getDynamicKeyTest(CheckAnnotationProcessor.CheckKeyObject keyObject)
	{
		return DynamicTest.dynamicTest("testKey", () ->
		{
			try
			{
				ResourceBundle bundle = ResourceBundle.getBundle(keyObject.baseName);
				Object value = bundle.getObject(keyObject.value);
				if (value == null)
				{
					throw keyExceptionProducer.apply(null, keyObject);
				}
			}
			catch (Throwable e)
			{
				throw keyExceptionProducer.apply(e, keyObject);
			}
		});
	}
	
	private DynamicTest getDynamicResourceBundleTest(CheckAnnotationProcessor.CheckResourceBundleObject resourceBundleObject)
	{
		return DynamicTest.dynamicTest("testResourceBundle", () ->
		{
			Set<String> localeNames = new HashSet<>(Arrays.asList(resourceBundleObject.locales));
			if (localeNames.size() <= 1)
			{
				String message = String.format("Annotation @%s on %s informs: Attribute 'locales' must have more than 1 unique locales for testing. With %s locales testing is not possible",
						CheckResourceBundle.class.getSimpleName(), resourceBundleObject.annotatedElement, localeNames.size());
				LOG.warn(() -> message);
				throw new TestAbortedException(message);
			}
			// get keys
			Map<String, Set<String>> keys = new HashMap<>();
			List<String> missingResourceBundles = new ArrayList<>();
			for (String localeStr : localeNames)
			{
				try
				{
					Locale locale = Locale.forLanguageTag(localeStr);
					ResourceBundle resourceBundle = ResourceBundle.getBundle(resourceBundleObject.baseName, locale);
					if (!Objects.equals(localeStr, resourceBundle.getLocale().toString()))
					{
						missingResourceBundles.add(localeStr);
						continue;
					}
					keys.put(localeStr, new HashSet<>(Collections.list(resourceBundle.getKeys())));
				}
				catch (MissingResourceException e)
				{
					missingResourceBundles.add(localeStr);
				}
			}
			if (!missingResourceBundles.isEmpty())
			{
				throw createAssertionFailedError(resourceBundleObject.message, null, "Annotation @%s on %s warns: ResourceBundles for baseName %s with locales %s was not found",
						CheckResourceBundle.class.getSimpleName(), resourceBundleObject.annotatedElement, resourceBundleObject.baseName, String.join(", ", missingResourceBundles));
			}
			
			// check each key
			Set<String> allKeys = new HashSet<>();
			keys.values().forEach(allKeys::addAll);
			Map<String, List<String>> absentKeys = new HashMap<>();
			for (String currentKey : allKeys)
			{
				for (Map.Entry<String, Set<String>> entry : keys.entrySet())
				{
					boolean contains = entry.getValue().contains(currentKey);
					if (!contains)
					{
						if (absentKeys.get(entry.getKey()) == null)
						{
							absentKeys.put(entry.getKey(), new ArrayList<>());
						}
						absentKeys.get(entry.getKey()).add(currentKey);
					}
				}
			}
			if (!absentKeys.isEmpty())
			{
				StringBuilder sb = new StringBuilder(System.lineSeparator());
				for (Map.Entry<String, List<String>> entry : absentKeys.entrySet())
				{
					sb.append("\t").append(entry.getKey()).append(": [").append(String.join(", ", entry.getValue())).append("]").append(System.lineSeparator());
				}
				throw createAssertionFailedError(resourceBundleObject.message, null, "Annotation @%s on %s warns: ResourceBundles for baseName %s has absent keys in locales:%s",
						CheckResourceBundle.class.getSimpleName(), resourceBundleObject.annotatedElement, resourceBundleObject.baseName, sb);
			}
		});
	}
}
