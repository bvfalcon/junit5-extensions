# JUnit5-Extensions
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/name.bychkov/junit5-extensions/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/name.bychkov/junit5-extensions)

Useful features for testing with JUnit 5

- [JUnit5-Extensions](#junit5-extensions)
      - [Minimum requirements](#minimum-requirements-to-use)
  * [Safely work with reflections](#safely-work-with-reflections)
    + [Problem description](#problem-description)
    + [Solution](#solution)
    + [Using in your project](#using-in-your-project)
    + [More samples](#more-samples)
  * [Unit-testing with fake smtp-server](#unit-testing-with-fake-smtp-server)
    + [Problem description](#problem-description-1)
    + [Solution](#solution-1)
    + [Using in your project](#using-in-your-project-1)
    + [More samples](#more-samples-1)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with markdown-toc</a></i></small>

#### Minimum requirements
- Java 8
- Maven 3.2.5
- JUnit 5.3

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
		<artifactId>junit5-annotations</artifactId>
		<version>1.0-SNAPSHOT</version>
		<scope>provided</scope>
	</dependency>
	<dependency>
		<groupId>name.bychkov</groupId>
		<artifactId>junit5-tests</artifactId>
		<version>1.0-SNAPSHOT</version>
		<scope>test</scope>
	</dependency>
</dependencies>

<build>
	<plugins>
		...
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-surefire-plugin</artifactId>
			<version>2.22.0</version>
			<configuration>
				<dependenciesToScan>
					<dependency>name.bychkov:junit5-tests</dependency>
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
	<artifactId>junit5-annotations</artifactId>
	<version>1.0-SNAPSHOT</version>
	<scope>provided</scope>
</dependency>
```

2) Dependency with junit5-tests:

```xml
<dependency>
	<groupId>name.bychkov</groupId>
	<artifactId>junit5-tests</artifactId>
	<version>1.0-SNAPSHOT</version>
	<scope>test</scope>
</dependency>
```

3) Configuration of Maven-plugin, that helps Maven to find junit-tests in 3d-party jars. Important things: plugin version (must be >= 2.22.0) and the row "name.bychkov:junit5-tests"

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>2.22.0</version>
	<configuration>
		<dependenciesToScan>
			<dependency>name.bychkov:junit5-tests</dependency>
		</dependenciesToScan>
	</configuration>
</plugin>
```

### More samples

You can find yet another example of usage JUnit5-Extensions [here: examples/annotations](./examples/annotations).

## Unit-testing with fake smtp-server

### Problem description

Suppose, your application sends emails to users with such or similar code:

```java
public class SendEmailService {

	public void sendMessage(String email, String subject, String body) throws MessagingException {
		Properties props = System.getProperties();
		props.put("mail.smtp.host", "localhost");
		props.put("mail.smtp.port", "25");
		Session session = Session.getInstance(props, null);
		
		Message simpleMail = new MimeMessage(session);
	
		simpleMail.setSubject(subject);
		simpleMail.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
	
		MimeMultipart mailContent = new MimeMultipart();
	
		MimeBodyPart mailMessage = new MimeBodyPart();
		mailMessage.setContent(body, "text/html; charset=utf-8");
		mailContent.addBodyPart(mailMessage);
	
		simpleMail.setContent(mailContent);
	
		Transport.send(simpleMail);
	}
}
```

You must be sure this functionality will work orrect in future and not break, while the code changes. How can you do this? Every time you can start simple smpt-server locally. After tests runned, see messages and prove it. For small projects this is only uncomfortable, for large - impossible.

### Solution

These actions can be performed automatically. Use in code of your unit-test special extension and smtp-server will start and stop automatically:

```java
	@RegisterExtension
	static FakeSmtpJUnitExcension fakeSmtp = new FakeSmtpJUnitExcension();
	
	@Test
	public void testSendMessage() {
		String expectedReceiver = "test-email-" + new Random().nextInt(Integer.MAX_VALUE) + "@example.com";
		String expectedSubject = "test-subject-" + new Random().nextInt(Integer.MAX_VALUE);
		
		try {
			SendEmailService testedService = new SendEmailService();
			testedService.sendMessage(expectedReceiver, expectedSubject, "text of body");
			
			Assertions.assertEquals(1, fakeSmtp.getMessages().size());
			EMailMessage actualMail = fakeSmtp.getMessages().iterator().next();
			Assertions.assertEquals(expectedReceiver, actualMail.getReceiver());
			Assertions.assertEquals(expectedSubject, actualMail.getSubject());
		} catch (MessagingException e) {
			Assertions.fail(e);
		}
	}
```

### Using in your project

Add in pom.xml of your project this modifications:

```xml
<dependencies>
	...
	<!-- JUnit 5 dependencies -->
	...
	<dependency>
		<groupId>name.bychkov</groupId>
		<artifactId>junit5-tests</artifactId>
		<version>1.0-SNAPSHOT</version>
		<scope>test</scope>
	</dependency>
</dependencies>

<build>
	<plugins>
		...
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-surefire-plugin</artifactId>
			<version>2.22.0</version>
		</plugin>
	</plugins>
</build>
```

Some notices to the code above:

1) Dependency with junit5-tests:

```xml
<dependency>
	<groupId>name.bychkov</groupId>
	<artifactId>junit5-tests</artifactId>
	<version>1.0-SNAPSHOT</version>
	<scope>test</scope>
</dependency>
```

By default, implementation uses JavaMail realization (namespaces `javax.mail.`). If you use Jakarta Mail (namespaces `jakarta.mail.`), add dependecy with classifier `jakarta`:

```xml
<dependency>
	<groupId>name.bychkov</groupId>
	<artifactId>junit5-tests</artifactId>
	<version>1.0-SNAPSHOT</version>
	<classifier>jakarta</classifier>
	<scope>test</scope>
</dependency>
```

2) Version of Maven-plugin, that runs junit-tests, must be >= 2.22.0

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>2.22.0</version>
</plugin>
```

### More samples

You can see full examples of usage JUnit5-Extensions FakeSMTP with [JavaMail](./examples/fakesmtp-javamail/) and [Jakarta Mail](./examples/fakesmtp-jakartamail/).