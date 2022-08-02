useful commands

install local
```
mvn clean install
```

run examples
```
mvn clean test -f examples/reflections/pom.xml && mvn clean test -f examples/resource-bundle/pom.xml && mvn clean test -f examples/serializable/pom.xml
```

publish
```
mvn clean deploy -Ppublish
```
