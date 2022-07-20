# JUnit5-Extensions
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/name.bychkov/junit5-extensions/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/name.bychkov/junit5-extensions)

Useful features for testing with JUnit 5

### Table fo contents
  * [Minimum requirements](#minimum-requirements)
* [Using in your project](#using-in-your-project)
* [Features](#features)
  * [Safely work with reflections](#safely-work-with-reflections)
    * [Problem description](#problem-description)
    * [Solution](#solution)
    * [More samples](#more-samples)
  * [Unit-testing with fake smtp-server](#unit-testing-with-fake-smtp-server)
    * [Problem description](#problem-description-1)
    * [Solution](#solution-1)
    * [JavaMail and Jakarta Mail](#javamail-and-jakarta-mail)
    * [More samples](#more-samples-1)

#### Minimum requirements
- Java 8
- Maven 3.2.5
- JUnit 5.3

## Using in your project

Add in your pom.xml these modifications

```xml
<dependencies>
	...
	<!-- other dependencies -->
	<!-- JUnit 5 dependencies -->
	...
	<!-- Annotations @CheckConstructor, @CheckField and @CheckMethod -->
	<dependency>
		<groupId>name.bychkov</groupId>
		<artifactId>junit5-annotations</artifactId>
		<version>0.2.0</version>
		<scope>provided</scope>
	</dependency>
	<!-- Reflection testing and FakeSMTP -->
	<dependency>
		<groupId>name.bychkov</groupId>
		<artifactId>junit5-tests</artifactId>
		<version>0.2.0</version>
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

Notes:

1) maven-surefire-plugin must have version >= 2.22.0

## Features

### Safely work with reflections

In current version JUnit5-Extensions supports automatically creation of JUnit5 test for controlling existence/accessability of constructors/fields/methods of Java-classes through Reflection.

#### Problem description

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

#### Solution

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

You can use annotations `@CheckField` `@CheckMethod` and `@CheckConstructor` to autocreate junit-tests.

#### More samples

You can find yet another example of usage annotations of JUnit5-Extensions [here: examples/annotations](./examples/annotations).

### Unit-testing with fake smtp-server

#### Problem description

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

#### Solution

These actions can be performed automatically. Use in code of your unit-test special extension and smtp-server will start and stop automatically:

```java
	@RegisterExtension
	static FakeSmtpJUnitExtension fakeSmtp = new FakeSmtpJUnitExtension();
	
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

#### JavaMail and Jakarta Mail

By default, implementation uses JavaMail realization (namespaces `javax.mail.`). If you use Jakarta Mail (namespaces `jakarta.mail.`), use dependency with classifier `jakarta`:

```xml
<dependency>
	<groupId>name.bychkov</groupId>
	<artifactId>junit5-tests</artifactId>
	<version>0.2.0</version>
	<classifier>jakarta</classifier>
	<scope>test</scope>
</dependency>
```

#### More samples

You can see full examples of usage JUnit5-Extensions FakeSMTP with [JavaMail](./examples/fakesmtp-javamail/) and [Jakarta Mail](./examples/fakesmtp-jakartamail/).

