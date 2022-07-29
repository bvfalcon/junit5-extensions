useful commands

install local
```
mvn clean install -pl .,annotations && mvn clean install -pl tests -Pjava.mail && mvn clean install -pl tests -Pjakarta.mail
```

run examples
```
mvn clean test -f examples/reflections/pom.xml && mvn clean test -f examples/fakesmtp-javamail/pom.xml && mvn clean test -f examples/fakesmtp-jakartamail/pom.xml && mvn clean test -f examples/resource-bundle/pom.xml && mvn clean test -f examples/serializable/pom.xml
```

publish
```
mvn clean deploy -pl .,annotations -Ppublish && mvn clean deploy -pl tests -Pjava.mail,publish && mvn clean deploy -pl tests -Pjakarta.mail,publish
```