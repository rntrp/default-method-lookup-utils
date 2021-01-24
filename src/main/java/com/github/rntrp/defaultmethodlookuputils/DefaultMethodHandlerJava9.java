package com.github.rntrp.defaultmethodlookuputils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

final class DefaultMethodHandlerJava9 implements IDefaultMethodHandler {
    @Override
    public MethodHandle getDefaultMethodHandle(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        try {
            return MethodHandles.lookup().findSpecial(declaringClass, method.getName(), methodType, declaringClass);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalArgumentException(method.toString(), e);
        }
    }
}
