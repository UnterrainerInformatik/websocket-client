# Log4j Migration Plan: websocket-client

**Layer**: 7 (Application) — update after `oauth-token-manager` and `serialization`.

## Before Starting

Prompt the user for the following version numbers before making any changes:

| Variable | Question |
|----------|----------|
| `NEW_PARENT_POM_VERSION` | What is the new `parent-pom` version? |
| `NEW_OAUTH_TOKEN_MANAGER_VERSION` | What is the new `oauth-token-manager` version? |
| `NEW_SERIALIZATION_VERSION` | What is the new `serialization` version? |
| `OWN_NEW_VERSION` | What version should `websocket-client` be bumped to? (currently `1.0.16`) |

## Context

Part of a migration from Log4j 1.x to Log4j 2.25.3 across all libraries. Has 2 classes using `@Slf4j` — no code changes needed (SLF4J API is unchanged). Test config has a custom DEBUG logger for `WebsocketConnection`.

> **IMPORTANT for execution**: This plan should be executed by actually making the file changes described below — create the new `log4j2.xml` / `log4j2-test.xml` files with the content provided, and delete the old `log4j.properties` files. Do not leave the config migration as a manual step.

## Current State

- **Artifact**: `info.unterrainer.commons:websocket-client`
- **Parent**: `parent-pom:1.0.2`
- **In-house dependencies**:
  - `oauth-token-manager:1.0.11` — bump to new version
  - `serialization:1.0.1` — bump to new version
- **log4j.properties**: YES (main + test, DIFFERENT content)
- **@Slf4j usage**: 2 classes

### Current `log4j.properties` content (main):
```properties
log4j.rootLogger=DEBUG, A1
log4j.appender.A1=org.apache.log4j.ConsoleAppender
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.charset=UTF-8
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n
log4j.logger.io.netty=WARN
log4j.logger.org.eclipse.milo=WARN
```

### Current `log4j.properties` content (test):
```properties
log4j.rootLogger=DEBUG, A1
log4j.appender.A1=org.apache.log4j.ConsoleAppender
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.charset=UTF-8
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n
log4j.logger.io.netty=WARN
log4j.logger.org.eclipse.milo=WARN
log4j.logger.info.unterrainer.websocketclient.WebsocketConnection=DEBUG
```

## Steps

### 1. Update parent version in `pom.xml`

Change the parent version (line 8) to the new parent-pom version:

```xml
<parent>
    <groupId>info.unterrainer.commons</groupId>
    <artifactId>parent-pom</artifactId>
    <version>NEW_PARENT_POM_VERSION</version>
</parent>
```

### 2. Update in-house dependency versions in `pom.xml`

```xml
<dependency>
    <groupId>info.unterrainer.commons</groupId>
    <artifactId>oauth-token-manager</artifactId>
    <version>NEW_OAUTH_TOKEN_MANAGER_VERSION</version>
</dependency>
<dependency>
    <groupId>info.unterrainer.commons</groupId>
    <artifactId>serialization</artifactId>
    <version>NEW_SERIALIZATION_VERSION</version>
</dependency>
```

### 3. Bump own version

Increment `<version>` (line 13, currently `1.0.16`).

### 4. Migrate logging config: create `src/main/resources/log4j2.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout charset="UTF-8"
                           pattern="%-4r [%t] %-5p %c %x - %m%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Logger name="io.netty" level="WARN"/>
        <Logger name="org.eclipse.milo" level="WARN"/>
        <Root level="DEBUG">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

### 5. Migrate logging config: create `src/test/resources/log4j2-test.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout charset="UTF-8"
                           pattern="%-4r [%t] %-5p %c %x - %m%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Logger name="io.netty" level="WARN"/>
        <Logger name="org.eclipse.milo" level="WARN"/>
        <Logger name="info.unterrainer.websocketclient.WebsocketConnection" level="DEBUG"/>
        <Root level="DEBUG">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

### 6. Delete old config files

- Delete `src/main/resources/log4j.properties`
- Delete `src/test/resources/log4j.properties`

### 7. Build, test, install

```bash
mvn clean install
```

## Files Changed

| File | Action |
|------|--------|
| `pom.xml` | Update parent version, update oauth-token-manager + serialization versions, bump own version |
| `src/main/resources/log4j2.xml` | Create (migrated from log4j.properties) |
| `src/test/resources/log4j2-test.xml` | Create (migrated from log4j.properties — includes WebsocketConnection=DEBUG) |
| `src/main/resources/log4j.properties` | Delete |
| `src/test/resources/log4j.properties` | Delete |
