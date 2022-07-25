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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
			createAssertionFailedError(keyObject.message, e, "Annotation @%s on %s warns: ResourceBundle with base name %s has no key %s",
			CheckKey.class.getSimpleName(), keyObject.annotatedElement, keyObject.baseName, keyObject.value);
	
	private static final BiFunction<Throwable, CheckKeysObject, AssertionFailedError> keysExceptionProducer = (e, keysObject) ->
			createAssertionFailedError(keysObject.message, e, "Annotation @%s on %s warns: ResourceBundle with base name %s has no keys %s",
			CheckKeys.class.getSimpleName(), keysObject.annotatedElement, keysObject.baseName, 
			Optional.ofNullable(keysObject.failureValues).map(Arrays::asList).map(List::stream).orElseGet(Stream::empty).collect(Collectors.joining(", ")));
	
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
			else if (item instanceof CheckAnnotationProcessor.CheckKeysObject)
			{
				CheckAnnotationProcessor.CheckKeysObject keysObject = (CheckAnnotationProcessor.CheckKeysObject) item;
				test = getDynamicKeysTest(keysObject);
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
				Locale locale = Locale.forLanguageTag(Objects.toString(keyObject.locale, ""));
				ResourceBundle bundle = ResourceBundle.getBundle(keyObject.baseName, locale);
				if (!bundle.containsKey(keyObject.value))
				{
					throw keyExceptionProducer.apply(null, keyObject);
				}
			}
			catch(MissingResourceException e)
			{
				throw createAssertionFailedError(keyObject.message, e, "Annotation @%s on %s warns: ResourceBundle with base name %s not found",
						CheckKey.class.getSimpleName(), keyObject.annotatedElement, keyObject.baseName);
			}
			catch (Throwable e)
			{
				throw keyExceptionProducer.apply(e, keyObject);
			}
		});
	}
	
	static class CheckKeysObject extends CheckAnnotationProcessor.CheckKeysObject
	{
		private static final long serialVersionUID = -3470371836634303128L;
		
		String[] failureValues;
		
		public CheckKeysObject(CheckAnnotationProcessor.CheckKeysObject parentObject, String[] failureValues)
		{
			this.annotatedElement = parentObject.annotatedElement;
			this.baseName = parentObject.baseName;
			this.locale = parentObject.locale;
			this.message = parentObject.message;
			this.values = parentObject.values;
			this.failureValues = failureValues;
		}
	}
	
	private DynamicTest getDynamicKeysTest(CheckAnnotationProcessor.CheckKeysObject keysObject)
	{
		return DynamicTest.dynamicTest("testKeys", () ->
		{
			try
			{
				Locale locale = Locale.forLanguageTag(Objects.toString(keysObject.locale, ""));
				ResourceBundle bundle = ResourceBundle.getBundle(keysObject.baseName, locale);
				
				List<String> failureKeys = new ArrayList<>();
				for (String key : keysObject.values)
				{
					if (!bundle.containsKey(key))
					{
						failureKeys.add(key);
					}
				}
				if (!failureKeys.isEmpty())
				{
					CheckKeysObject newKeysObject = new CheckKeysObject(keysObject, failureKeys.toArray(new String[failureKeys.size()]));
					throw keysExceptionProducer.apply(null, newKeysObject);
				}
			}
			catch(MissingResourceException e)
			{
				throw createAssertionFailedError(keysObject.message, e, "Annotation @%s on %s warns: ResourceBundle with base name %s not found",
						CheckKeys.class.getSimpleName(), keysObject.annotatedElement, keysObject.baseName);
			}
			catch (Throwable e)
			{
				throw keysExceptionProducer.apply(e, new CheckKeysObject(keysObject, new String[0]));
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
				throw createAssertionFailedError(resourceBundleObject.message, null, "Annotation @%s on %s warns: ResourceBundles for base name %s with locales %s was not found",
						CheckResourceBundle.class.getSimpleName(), resourceBundleObject.annotatedElement, resourceBundleObject.baseName, joinLocales(missingResourceBundles));
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
					sb.append("\t").append(entry.getKey()).append(": [").append(joinLocales(entry.getValue())).append("]").append(System.lineSeparator());
				}
				throw createAssertionFailedError(resourceBundleObject.message, null, "Annotation @%s on %s warns: ResourceBundles for baseName %s has absent keys in locales:%s",
						CheckResourceBundle.class.getSimpleName(), resourceBundleObject.annotatedElement, resourceBundleObject.baseName, sb);
			}
		});
	}
	
	private String joinLocales(List<String> localeNames)
	{
		return localeNames.stream().map(o -> "'" + o + "'").collect(Collectors.joining(", "));
	}
}
