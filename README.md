# default-method-lookup-utils

A small utility class for invoking default interface methods.

Imagine, you have an interface doing something like this:

```java
interface Dog {
    default void woof() {
        System.out.println("woof");
    }
}
```

The utility class allows you to

1. invoke the overridden default method on an instance (yes, it's possible)
2. delegate the invocation call to the default method when using `java.lang.reflect.Proxy`

## Invoking overridden default methods

First, we implement the interface, e.g. by instantiating an anonymous class:

```java
Dog dog = new Dog() {
    @Override
    public void woof() {
        System.out.println("meow");
    }
};
```

By now our dog will only meow when we command it to woof:

```java
dog.woof(); // Obviously, meow

Method woof = Dog.class.getMethod("woof");
woof.invoke(dog); // Also meow
```

Can we make our dog woof again? Now that's where our utility class comes in:

```java
DefaultMethodLookupUtils.invokeDefaultMethod(dog, woof); // woof!
```

This case somewhat defies the common understanding of Java polymorphism, as invoking superclass methods from an instance
is not allowed under normal circumstances. Java 7 introduced the `java.lang.invoke.MethodHandle` infrastructure, which
allows for more intricate reflective access. `DefaultMethodLookupUtils` makes use of this API to overcome the
limitations regarding the superclass method invocation on objects.

## Default methods & `java.lang.reflect.Proxy`

IMHO, that's the main use case for the utility class. The idea behind `java.lang.reflect.Proxy` is to act as an extra
layer of program logic when calling object methods. It may forward method calls to some kind of delegate object, or it
may execute its own logic behind curtains. This is actually how the famous "Spring magic" works. Also, most Java mock
frameworks are basically designed around reflection proxies.

Sometimes, when you create a proxy object for an interface, you might also want to invoke default methods _as is_, and
do some generic stuff for all other methods. Example:
```java
Dog dog = (Dog) Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        new Class[]{Dog.class},
        (proxy, method, args) -> method.isDefault()
            ? method.invoke(proxy) // call the default method
            : null); // for other methods simply return null

dog.woof(); // will it woof?
```

Our dog doesn't woof 🤨. Instead, it throws an everlong stacktrace of pretty
vague `InvocationTargetException`s at us.

Let's try it again, but this time with the `DefaultMethodLookupUtils`:
```java
Dog dog = (Dog) Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        new Class[]{Dog.class},
        (proxy, method, args) -> method.isDefault()
            ? DefaultMethodLookupUtils.invokeDefaultMethod(proxy, method, args)
            : null);

dog.woof(); // woof, at last
```

This time around our default method will be called, and we can finally lean back.

## Under the Hood

Implementation is based on the great DZone
article ["Correct Reflective Access to Interface Default Methods in Java 8, 9, 10"](https://dzone.com/articles/correct-reflective-access-to-interface-default-methods).
Kudos to [Lukas Eder](https://github.com/lukaseder) for pointing out the differences between Java versions and
providing a working solution for newer JREs.

Even though the `MethodHandle` API is present since Java 7, its behaviour depends on the runtime version:

* Java 7 and 8 must call a private constructor of `MethodHandles.Lookup`. Obviously, it's no good, if the program runs
  under a `SecurityManager` with draconian security policy, so you may want to loosen these restrictions. Since default
  methods were introduced in Java 8, on Java 7 the utility class functionality is limited to calling the overridden
  default methods. This approach throws an `IllegalAccessException` with later Java versions.
* With Java 9 onwards there is no need in calling any private constructor, since `MethodHandles.lookup()` is now capable
  of finding default interface methods. However, this method doesn't work with Java 7 and 8, but the method itself is
  present.

`DefaultMethodLookupUtils` implements both approaches. It checks Java version at runtime and chooses the right one
automatically. Code can be compiled once with JDK 7 and will work on all JREs starting with 7.