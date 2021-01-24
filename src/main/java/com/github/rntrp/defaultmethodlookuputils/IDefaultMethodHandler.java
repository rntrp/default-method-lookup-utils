package com.github.rntrp.defaultmethodlookuputils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

interface IDefaultMethodHandler {
    MethodHandle getDefaultMethodHandle(Method method);
}
