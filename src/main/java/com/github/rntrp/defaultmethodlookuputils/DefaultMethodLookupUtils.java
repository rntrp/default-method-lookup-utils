package com.github.rntrp.defaultmethodlookuputils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

public final class DefaultMethodLookupUtils {
    private static final IDefaultMethodHandler DEFAULT_METHOD_HANDLER;

    static {
        if (System.getProperty("java.version").startsWith("1.")) {
            DEFAULT_METHOD_HANDLER = new DefaultMethodHandlerPreJava9();
        } else {
            DEFAULT_METHOD_HANDLER = new DefaultMethodHandlerJava9();
        }
    }

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private DefaultMethodLookupUtils() {
        // Suppresses default constructor, ensuring non-instantiability.
    }

    public static MethodHandle getDefaultMethodHandle(Method method) {
        return DEFAULT_METHOD_HANDLER.getDefaultMethodHandle(method);
    }

    public static MethodHandle getDefaultBoundMethodHandle(Method method, Object instance) {
        return getDefaultMethodHandle(method).bindTo(instance);
    }

    /**
     * @param instance the proxy instance that the default method was invoked on.
     * @param method   the {@code Method} instance corresponding to the default method invoked on the proxy instance. Note, that no check whether the method is a default method is performed by the method.
     * @param args     arguments passed to the default method, empty array or {@code null}. Internally, {@link MethodHandle#invokeWithArguments(Object...)} is called, which may be referred to as how to call a method.
     * @param <T>      generic return type for convenience reasons (no type checking is done within the method itself).
     * @return method result, {@code null} if method return type is {@code Void}.
     * @throws Throwable any {@code Error} or {@code Exception} thrown by the invocation.
     * @see MethodHandle#invokeWithArguments(Object...)
     * @see Method#isDefault()
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeDefaultMethod(Object instance, Method method, Object... args) throws Throwable {
        return (T) getDefaultBoundMethodHandle(method, instance).invokeWithArguments(args);
    }
}
