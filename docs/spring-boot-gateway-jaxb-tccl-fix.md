# Spring Boot Gateway: JAXB / RESTEasy error after switching from shade to spring-boot-maven-plugin

## Symptom
After switching the gateway packaging from `maven-shade-plugin` to the Spring Boot plugin (repackaged Boot JAR), some REST endpoints started failing in asynchronous processing.

Typical runtime error in the gateway logs:

- `jakarta.xml.bind.JAXBException: Implementation of Jakarta XML Binding-API has not been found on module path or classpath`
- `ClassNotFoundException: org.glassfish.jaxb.runtime.v2.ContextFactory`

This was observed, for example, when RESTEasy tried to marshal/unmarshal XML in async callbacks (e.g. in `ForkJoinPool.commonPool`).

## Root cause
Spring Boot starts the application with a dedicated Boot class loader (e.g. `org.springframework.boot.loader.launch.LaunchedClassLoader`).

JAXB discovers its runtime implementation using Java service discovery (`ServiceLoader`). Many libraries use the thread context class loader (TCCL) for that discovery.

In this gateway, async callbacks are executed on worker threads (typically `ForkJoinPool.commonPool`). These threads are not created by the Spring Boot launcher. Therefore their TCCL is not guaranteed to be the Boot class loader. If the TCCL is wrong, JAXB provider discovery can fail even though the required JARs are present inside `BOOT-INF/lib`.

## Verification steps
1. Verify the required JAXB/activation artifacts are packaged in the Boot jar:
   - Check `BOOT-INF/lib` contains `jakarta.xml.bind-api`, `jaxb-runtime`, and activation (`angus-activation` or `jakarta.activation-api`).

2. Add a startup self-test (logging-only) to confirm JAXB works with the Boot class loader:
   - Log current TCCL
   - Try locating `org.glassfish.jaxb.runtime.v2.ContextFactory`
   - Build a `JAXBContext` and log the implementation class

This confirms “JAXB is present”, but does not automatically guarantee the correct TCCL is used on async worker threads.

## Fix
### 1) Ensure JAXB runtime dependencies are available
Add (or ensure) the runtime deps are available in the affected gateway modules (example):
- `jakarta.xml.bind:jakarta.xml.bind-api`
- `org.glassfish.jaxb:jaxb-runtime` (runtime scope)
- `jakarta.activation:jakarta.activation-api` (runtime scope)

This makes sure the implementation exists in the packaged application.

### 2) Enforce Boot ClassLoader as TCCL for async callbacks
In `com.arvatosystems.t9t.jetty.impl.T9tRestProcessor` the gateway now captures the class loader once and wraps async callbacks so they execute with the Boot class loader as TCCL.

Implementation idea:
- Capture Boot class loader once: `private static final ClassLoader BOOT_CLASSLOADER = T9tRestProcessor.class.getClassLoader();`
- Wrap callbacks to:
  - read current thread TCCL
  - if different, set TCCL to `BOOT_CLASSLOADER`
  - execute callback
  - restore original TCCL in `finally`

This specifically targets code paths like:
- `CompletableFuture.thenAccept(...)`
- `CompletableFuture.exceptionally(...)`

Because RESTEasy’s JAXB provider and JAXB itself rely on service discovery through the TCCL.

## Why the fix works
- The Spring Boot “repackaged jar” uses a special class loader that can load nested JARs.
- Worker threads from a generic pool may not have that class loader as context.
- Setting/restoring the TCCL for callback execution ensures JAXB can discover its runtime implementation consistently.

## Notes / pitfalls
- Always restore the previous TCCL to avoid side effects on reused worker threads.
- The problem may only appear after switching packaging, because the class loading model changes (nested JARs + Boot loader).
- A startup JAXB self-test can be green while async callbacks still fail, because the test runs on the main thread (which has the correct TCCL).

## Files involved
- `t9t-jetty-resteasy/src/main/java/com/arvatosystems/t9t/jetty/impl/T9tRestProcessor.java`
- Gateway module `pom.xml` files (dependencies listed above)

## Outcome
- XML marshalling/unmarshalling via RESTEasy/JAXB works again under Spring Boot packaging.
- Async REST processing no longer fails with missing JAXB implementation errors.
