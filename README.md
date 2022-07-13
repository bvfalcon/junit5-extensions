# junit5-extensions
Useful features for testing with JUnit 5

#### Minimum requirements to use
- Java 8
- JUnit 5.3

Accessible in [Maven Central Repository](https://search.maven.org/artifact/name.bychkov/junit5-extensions).

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

### Using in your project

You can use annotations `@CheckField`, `@CheckMethod` and `@CheckConstuctor` after these modifications in pom.xml:

```xml
<dependencies>
	...
	<!-- JUnit 5 dependencies -->
	...
	<dependency>
		<groupId>name.bychkov</groupId>
		<artifactId>junit5-extensions</artifactId>
		<version>1.0-SNAPSHOT</version>
		<scope>provided</scope>
	</dependency>
	<dependency>
		<groupId>name.bychkov</groupId>
		<artifactId>junit5-extensions</artifactId>
		<version>1.0-SNAPSHOT</version>
		<type>test-jar</type>
		<scope>test</scope>
	</dependency>
</dependencies>

<build>
	<plugins>
		...
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-surefire-plugin</artifactId>
			<version>3.0.0-M7</version>
			<configuration>
				<dependenciesToScan>
					<dependency>name.bychkov:junit5-extensions:test-jar</dependency>
				</dependenciesToScan>
			</configuration>
		</plugin>
	</plugins>
</build>
```

Some notices to the code above:

1) Dependency with annotations `@CheckConstructor`, `@CheckField` and `@CheckMethod`:

```xml
<dependency>
	<groupId>name.bychkov</groupId>
	<artifactId>junit5-extensions</artifactId>
	<version>1.0-SNAPSHOT</version>
	<scope>provided</scope>
</dependency>
```

2) Dependency with junit5-tests:

```xml
<dependency>
	<groupId>name.bychkov</groupId>
	<artifactId>junit5-extensions</artifactId>
	<version>1.0-SNAPSHOT</version>
	<type>test-jar</type>
	<scope>test</scope>
</dependency>
```

3) Configuration of Maven-plugin, that helps Maven to find junit-tests in 3d-party jars. Important things - plugin version (must be >=3.0.0-M4) and the row "name.bychkov:junit5-extensions:test-jar"

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>3.0.0-M7</version>
	<configuration>
		<dependenciesToScan>
			<dependency>name.bychkov:junit5-extensions:test-jar</dependency>
		</dependenciesToScan>
	</configuration>
</plugin>
```

### More samples

You can find yet another example of usage JUnit5-Extensions [here: bvfalcon/juni5-test](https://github.com/bvfalcon/junit5-test).
