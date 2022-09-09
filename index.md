# JUnit5-Extensions
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/name.bychkov/junit5-extensions/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/name.bychkov/junit5-extensions)

Useful features for testing with JUnit 5: autogenerating tests with test templates, for reflections, resource bundles, serializable.

## Table fo contents
  * [Minimum requirements](#minimum-requirements)
* [Using in your project](#using-in-your-project)
* [Features](#features)
  * [Parameterized constructors](#parameterized-constructors)
  * [Safely work with reflections](#safely-work-with-reflections)
  * [Safely work with resource bundles](#safely-work-with-resource-bundles)
  * [Check classes for Serializable](#check-classes-for-serializable)

### Minimum requirements
- Java 8
- Maven 3.2.5
- JUnit 5.3

# Using in your project

Add in your pom.xml these modifications

```xml
<dependencies>
	...
	<!-- other dependencies -->
	...
	<dependency>
		<groupId>org.junit.jupiter</groupId>
		<artifactId>junit-jupiter</artifactId>
		<version>5.9.0</version>
		<scope>test</scope>
	</dependency>
	<dependency>
		<groupId>name.bychkov</groupId>
		<artifactId>junit5-annotations</artifactId>
		<version>0.5.1</version>
		<scope>provided</scope>
	</dependency>
	<dependency>
		<groupId>name.bychkov</groupId>
		<artifactId>junit5-tests</artifactId>
		<version>0.5.1</version>
		<scope>test</scope>
	</dependency>
</dependencies>

<build>
	<plugins>
		...
		<plugin>
			<artifactId>maven-surefire-plugin</artifactId>
			<version>2.22.2</version>
			<configuration>
				<dependenciesToScan>
					<!-- run junit-tests from dependency -->
					<dependency>name.bychkov:junit5-tests</dependency>
				</dependenciesToScan>
			</configuration>
		</plugin>
	</plugins>
</build>
```

Notes:
1. maven-surefire-plugin must have version >= 2.22.0

# Features

**Important common note**: all annotations *are defined with [RetentionPolicy SOURCE](https://docs.oracle.com/javase/8/docs/api/java/lang/annotation/RetentionPolicy.html#SOURCE)* and used in compile-time. After using they are **discarded** by compiler and **absent** in compiled (*.class) code.

## Parameterized Constructors

One of the most used feature of JUnit 5 - [parameterized tests](https://junit.org/junit5/docs/5.9.0/user-guide/#writing-tests-parameterized-tests). There are many ways to specify parameters for such test and check with one test many conditions. Powerful thing. But `@ParameterizedTest` and all `@*Source` annotations can be applied only to methods, not to constructors. `@ParameterizedConstructor` and a set of `@*Source` annotations eliminate this inconvenience. With `@ParameterizedConstructor` you can create test-classes and use it as templates for tests.

Example

```java
public abstract ServerTest extends org.glassfish.jersey.test.JerseyTest {

	@ParameterizedConstructor
	@MethodSource(value="testFactories")
	public ServerTest(TestContainerFactory testContainerFactory) {
		super(testContainerFactory);
	}
	
	public static Collection<TestContainerFactory> testFactories() {
		return Arrays.asList(new JacksonJsonTestProvider(), new MoxyJsonTestProvider(), new JettisonMappedJsonTestProvider());
	}
	
	@Test
	public void test1() {
		...
	}
	
	@Test
	public void test2() {
		...
	}
}
```

During executing this test class will be instantiated for each parameter value and ([default behaviour](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle)) for each test-method. In the example above ServerTest will be instantiated 3 (parameters count) * 2 (test-methods count) = 6 times. But this default behaviour can be changed with `@TestInstance(Lifecycle.PER_CLASS)` applied to class declaration or with engine configuration. In this case ServerTest will be instantiated 3 (parameters count) times.

Notes to usage `@ParameterizedConstructor`:

1) test-class must be defined with `abstract` modifier. Thanks to this test-methods of this class are not considered as usual test-methods. Without default constructor (constructor without parameters) attempt to their execution as usual tests will be ended with error.

2) some annotations (but not all) of usual tests are applicable to tests with parameterized constructor. They are `@Disabled`, `@TestInstance` and methods annotated with `@BeforeAll`, `@BeforeEach`, `@AfterEach` and `@AfterAll`.

### More samples

You can find yet another example of usage this annotation [here](https://github.com/bvfalcon/junit5-extensions/tree/0.5.1/examples/parameterized-constructor).

## Safely work with reflections

In current version JUnit5-Extensions supports automatically creation of JUnit5 test for controlling existence/accessability of constructors/fields/methods of Java-classes through Reflection.

### Problem description

Suppose you have a code in your project

```java
void clearCachedSettings() throws Exception {
	Field mavenSettings = MavenSettings.class.getDeclaredField("mavenSettings");
	mavenSettings.setAccessible(true);
	mavenSettings.set(null, null);
}
```

How can you be sure, that in next version `MavenSettings` yet contains the field with name `mavenSettings`?

This code is very fragile. Smells bad, do you agree? Yes, of course, this is excellent example of anti-pattern. But in some situations you cannot avoid Java Reflections.

### Solution

What can you do? Modify sample code so:

```java
@CheckField(targetClass=MavenSettings.class, value="mavenSettings")
void clearCachedSettings() throws Exception {
	Field mavenSettings = MavenSettings.class.getDeclaredField("mavenSettings");
	mavenSettings.setAccessible(true);
	mavenSettings.set(null, null);
}
```

Next time when you will assembly your project with annotation creates JUnit5-test that will control existence of field with name `mavenSettings` in class `MavenSettings`. If field not exists, JUnit5-test fails.

You can use annotations `@CheckField`, `@CheckFields`, `@CheckMethod` and `@CheckConstructor` to autocreate junit-tests.

### More samples

You can find yet another example of usage this annotations of JUnit5-Extensions [here: examples/reflections](https://github.com/bvfalcon/junit5-extensions/tree/0.5.1/examples/reflections).

## Safely work with resource bundles

### Problem description

If your application supports multiple languages, you know about Resource Bundles. Localized strings are language-specific and stored in \*.properties (since Java 9 in \*.xml too) files and have a view

```properties
key1=value1
key2=value2
key3=value3
...
```

In java code these localized strings are used usually as

```java
private ResourceBundle bundle = ResourceBundle.getBundle("Messages", Locale.ENGLISH);

public String getLocalizedKey1() {
	return bundle.getString(key1);
}
```

All will be good when you developing an application. But after several redesign nobody can already say, that localiyed strings are used and that are obsolete and can be removed.

### Solution

```java
@CheckResourceBundle(baseName="Messages", locales={"en","de"})
private ResourceBundle bundle = ResourceBundle.getBundle("Messages", Locale.ENGLISH);

@CheckKey(baseName="Messages", value="key1", locale="en")
public String getLocalizedKey1() {
	return bundle.getString("key1");
}
```

With annotations `@CheckKey`, `@CheckKeys` and `@CheckResourceBundle` you can be always sure, that key exists in default locale resource bundle and all keys in specified locales of specified resource bundle are synchronized.

### More samples

Full example you can find [here: examples/resource-bundle](https://github.com/bvfalcon/junit5-extensions/tree/0.5.1/examples/resource-bundle).

## Check classes for Serializable

### Problem description

In some cases some of your classes must be serializable (must implement java.io.Serializable). If class has no such interface, in most of cases java-compiler says nothing and an error in runtime can be acquired. 

### Solution

in java-package with serializable classes add file `package-info.java` with such contents:

```java
@CheckSerializable
package your.package.name;
``` 

When added, this annotation forced to run unit-test for check all classes of `your.package.name` and fails if any class not implements java.io.Serializable.

### More samples

[Here](https://github.com/bvfalcon/junit5-extensions/tree/0.5.1/examples/serializable/) you can see full examples of usage JUnit5-Extensions annotation `@CheckSerializable`.
