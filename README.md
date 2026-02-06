# Not Enough Buckets

## Overview
A simple rate limit library for Spring Boot applications.
Not Enough Buckets (NEB) offers configurable and customizable rate limits, with granularity.
Once it's set up, just add an annotation on your controller endpoint to configure a rate limit profile.
NEB handles the rest for you automagically.

## About
This library is a modified version of an internal tool used in some of our infrastructure applications.

## Dependencies
This library depends on `Bucket4J` for rate limiting.
For standalone in-memory configurations, `Caffeine` is used.
For Redis configurations, a `Jedis` client must be provided.


# Installation

## Gradle
To add NEB to your project, just add the following line to your `build.gradle`:
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "net.pyroneon:not-enough-buckets:1.0.1"
}
```
(Replace the version number with the latest version in releases)

## Maven
To add NEB to your project, just add the following line to your `pom.xml`:
```xml
<dependency>
    <groupId>net.pyroneon</groupId>
    <artifactId>not-enough-buckets</artifactId>
    <version>1.0.1</version>
</dependency>
```
(Replace the version number with the latest version in releases)

## JavaDocs
If you're developing in IntelliJ, you may be prompted to `install sources` when adding NEB to your project.
Our project is heavily documented with JavaDocs.
Click yes to be able to see our documentation while developing.


# Usage

## Definitions
- Bucket: A counter that slowly counts down to 0. Configured to leak at a certain speed (profile), and match certain requests (resolved property). Increases by 1 on each matching request.
- Profile (flow): The configuration for one bucket; i.e. how quickly it refills and what `RequestResolvers` it applies to.
- Container: The repository used to store and access buckets.
- To resolve: To get some attribute about a request that we want to rate limit by (e.g. IP address, User ID). Result is a resolved property.

## Setup

### Enabling
Now that you installed NEB, you need to enable it in your application.
First, choose between Redis storage or in-memory (standalone) storage. See the `Redis` and `Standalone` sections below.
Enable rate limiting by adding either of the annotations `@EnableRateLimitingRedis` and `@EnableRateLimitingStandalone` to a `Configuration` class.
Example:
```java
@Configuration
@EnableRateLimitingRedis
public class NEBConfig {
    /* ... */
}
```
Ensure both annotations are not active at the same time.
If you want to use both in your project, selectively enable them as separate mutually-exclusive Configuration classes.

### Create Rate Limits
To create a rate limit, annotate a controller class or method with a `@RateLimit` annotation, specifying the amount of traffic that method will allow (profile).
NEB comes with a few preconfigured rate limit profiles (flows) by default, `@LaxRateLimit` and `@StrictRateLimit`.
To use one of these profiles, just apply the annotation to a controller method or class.
Example (method):
```java
@RestController
public MyController {
    /* Apply the "lax" rate limit to this method specifically. */
    @LaxRateLimit
    @GetMapping(value = '/hello-world')
    public ResponseEntity<?> helloWorld(HttpServletRequest httpRequest) {
        /* This won't be reached if rate limit is exceeded ... */
    }
}
```
Example (class):
```java
@RestController
/* Apply the "lax" rate limit to every method in the class, unless they apply their own rate limit. */
@LaxRateLimit
public MyController {

    @GetMapping(value = '/hello-world')
    public ResponseEntity<?> helloWorld(HttpServletRequest httpRequest) {
        /* This won't be reached if rate limit is exceeded ... */
    }
}
```
See `RateLimitHandler` for details on rate limit priority when overlapping class and method-level rate limits.

### Handling Exceptions
When a rate limit is exceeded, the `RateLimitExceededException` is thrown.
This should be handled gracefully with a 429 (TOO MANY REQUESTS) error.
If a custom property can't be resolved for a given request (usually due to misconfiguration) the `RateLimitPropertyResolutionFailure` error is thrown.
This means that your custom `RequestResolver` marked a property as `isRequired=true` and the property didn't exist for a request.
You can handle this however you see fit. For example, return a 403 (FORBIDDEN).

## Customization

### Profiles (Flows)
To create a custom one-off rate limit profile, you can directly apply `@RateLimit` to a controller method or class.
Custom rate limits must have a unique `name` parameter.
Example (method):
```java
@RestController
public MyController {
    /* Apply a custom rate limit with custom params and a custom name. */
    @RateLimit(name = "helloWorld_ip", appliesTo = {IpAddressResolver.PROPERTY_NAME}, capacity = 50, refillAmount = 20, seconds = 60, isGreedy = true)
    @GetMapping(value = '/hello-world')
    public ResponseEntity<?> helloWorld(HttpServletRequest httpRequest) {
        /* This won't be reached if rate limit is exceeded ... */
    }
}
```
It's better to create a reusable custom rate limit profile to keep a single source-of-truth and avoid accidentally duplicating names.
Create a new custom rate limit annotation like so.
Example:
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RateLimit(name = "a_totally_unique_name", capacity = 10, refillAmount = 5, seconds = 30, isGreedy = true)
public @interface MyNewRateLimit {}
```
Recall that if you don't specify an `appliesTo` param in `@RateLimit`, it will apply this flow to all resolvers, including custom ones.
You can also create composite rate limits by applying multiple `@RateLimit` annotations on the same annotation.
This is useful if you have multiple `Resolvers` (see next section).
Example:
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
// Apply a more lenient rate limit by IP address.
@RateLimit(name = "strict_ip", appliesTo = {IpAddressResolver.PROPERTY_NAME},
        capacity = 20, refillAmount = 10, seconds = 60, isGreedy = true)
// Apply a strict rate limit per user.
@RateLimit(name = "strict_user", appliesTo = {UserIdResolver.PROPERTY_NAME},
        capacity = 10, refillAmount = 5, seconds = 120, isGreedy = true)
public @interface StrictRateLimitByUserAndIp {}
```
In this example, requests can count towards the bucket for requests made by this IP address, as well as requests made by this user ID.
Since many people could be using the same IP, we'll make it more lenient.
However, only one person should be using the same user ID, so we make it strict.
Note that if either rate limit is exceeded, the request is refused.
So if one user is spamming a request, the entire building won't be rate limited!


### Resolvers
By default, you can rate limit by request IP address with the property `ip_address` (See `IpAddressResolver`).
First, implement your own `RequestResolver` like so.
Example:
```java
public class UserIdResolver implements RequestResolver {
    public static final String PROPERTY_NAME = "user_id";

    @Override
    public @NotNull String getPropertyName() {
        return PROPERTY_NAME;
    }

    @Override
    public @Nullable String resolve(HttpServletRequest request) {
        /* Perform some custom logic to get a user ID from a request. */
        String userId = doSomething(request);
        /* Return the user ID.  */
        return userId;
    }

    @Override
    public boolean isRequired() {
        return false;
    }
}
```
Then register it like so in your configuration.
Example:
```java
@Configuration
@EnableRateLimitingRedis
public class NEBConfig {
    @Autowired
    public NEBConfig(RequestResolverManager resolverManager) {
        // Register a user ID resolver with the manager.
        resolverManager.addResolver(new UserIdResolver());
    }
}
```
Once its registered, you can use this resolver in your rate limits like so.
Example:
```java
@RestController
public MyController {
    /* Create a rate limit with the custom resolvers property name and parameters. */
    @RateLimit(name = "strict_user", appliesTo = {UserIdResolver.PROPERTY_NAME}, capacity = 10, refillAmount = 5, seconds = 120, isGreedy = true)
    @PostMapping(value = '/api/something-really-expensive')
    public ResponseEntity<?> somethingReallyExpensive(HttpServletRequest httpRequest) {
        /* This won't be reached if rate limit is exceeded ... */
    }
}
```

# Help

## How NEB Works

### BucketContainer
When NEB is enabled, one of the default `BucketContainer` repository implementations is provided for you.
A `BucketContainer` is just a map of keys (as a String) to a `Bucket`.
A reusable supplier tells the container how to configure a new bucket when one must be created.
Since different endpoints can have their own bucket configs, this must be passed every time.

### CaffeineBucketContainer
This is a simple implementation of `BucketContainer` that uses `Caffeine` internally to store buckets in a `Cache` (`ConcurrentHashMap`).
Since this is in-memory, it won't sync across multiple nodes!

### RedisBucketContainer
This is an implementation of `BucketContainer` that stores `Bucket` objects in Redis through a `ProxyManager`.
The `ProxyManager` yields buckets that are linked to an actual entry in Redis, so operations on the bucket are reflected in Redis automatically.

### RequestResolver
Given an incoming request, this class will try to get some attribute about it and "resolve" (return) it.
Each `RequestResolver` has its own unique property name. Do not assign the same property name to multiple resolvers.

### IpAddressResolver
This is an implementation of `RequestResolver` that tries to get the IP address of an incoming request.
This implementation will first try to get the Cloudflare `CF-Connecting-IP` header value.
If no value is assigned, it falls back on the `X-Forwarded-For` header value.
If that value isn't assigned, it falls back on the remote address.

### @RateLimit
Rate limits are applied by the `RateLimitHandler`. 
See its javadocs for a comprehensive explanation of which rate limits take precedence, what works and won't work, and scope.

## Redis Storage
For load-balanced applications with multiple nodes, you'll need a single shared Redis server to keep track of rate limits.
Otherwise, requests could hit a rate limit on one node but still be allowed to request from a different node!
Also, if Redis is run in persistent mode, your rate limits will save even upon restarts or crashes.

## Standalone Storage
The in-memory (standalone) solution is meant for testing or for simple apps.
See the previous section for edge cases this solution won't cover.

## Should I Use Redis?
If you're already using redis in your project, then you should configure NEB to use redis.
If you plan to run multiple instances of your application, you should strongly consider using redis.
If you have very long-lived rate-limit timeouts (like >30 mins), you may want to consider using a persistent Redis server.

## FAQ

### IP addresses get resolved to the same value!
This can be an issue if your application is behind a reverse proxy.
If you're using the default `IpAddressResolver`, refer to its documentation.
This resolver will try a few headers before falling back to the remote address of the request.
In a reverse-proxy, the remote address may be wrong.
Configure your reverse-proxy (like nginx) to pass an `X-Forwarded-For` header along with each request.