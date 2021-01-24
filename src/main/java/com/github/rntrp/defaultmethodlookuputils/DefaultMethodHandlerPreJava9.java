package com.github.rntrp.defaultmethodlookuputils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class DefaultMethodHandlerPreJava9 implements IDefaultMethodHandler {
    private final Constructor<MethodHandles.Lookup> lookupConstructor;

    @SuppressWarnings("deprecation")
    DefaultMethodHandlerPreJava9() {
        try {
            lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
            if (!lookupConstructor.isAccessible()) {
                lookupConstructor.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public MethodHandle getDefaultMethodHandle(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            MethodHandles.Lookup lookup = lookupConstructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE);
            return lookup.unreflectSpecial(method, declaringClass);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalArgumentException(method.toString(), e);
        }
    }
}
