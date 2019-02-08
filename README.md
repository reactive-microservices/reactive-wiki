# Reactive wiki microservice


## Unit tests.

```
mvn clean test
```

## Integration tests

```
mvn clean test -P integration
```

## Run locally

Using maven:
```
mvn compile exec:java
```
Using fat jar file:
```
java -jar target/reactive-wiki-1.0.0-SNAPSHOT-fat.jar -conf src/main/resources/wiki-config.json
```

Check service running:
```
curl localhost:8080
```

