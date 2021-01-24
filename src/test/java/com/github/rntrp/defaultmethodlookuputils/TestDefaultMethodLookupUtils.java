package com.github.rntrp.defaultmethodlookuputils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestDefaultMethodLookupUtils {
    private static ITestDefaultMethod initProxy() {
        return (ITestDefaultMethod) Proxy.newProxyInstance( //
                ClassLoader.getSystemClassLoader(), //
                new Class[]{ITestDefaultMethod.class}, //
                (proxy, method, args) -> method.isDefault() //
                        ? DefaultMethodLookupUtils.invokeDefaultMethod(proxy, method, args) //
                        : "test");
    }

    @Test
    void testProxyDefaultMethodWithNoArgs() {
        ITestDefaultMethod proxy = initProxy();
        assertEquals("default", proxy.getDefaultString());
        assertEquals("test", proxy.getTestString());
    }

    @Test
    void testProxyDefaultMethodWithArgs() {
        ITestDefaultMethod proxy = initProxy();
        assertEquals("default12", proxy.getDefaultString("1", "2"));
    }

    @Test
    void testProxyDefaultMethodCallingNonDefaultMethod() {
        ITestDefaultMethod proxy = initProxy();
        assertEquals("test1", proxy.getDefaultStringByGetString("1"));
    }

    @Test
    void testProxyDefaultMethodCallingDefaultMethod() {
        ITestDefaultMethod proxy = initProxy();
        assertEquals("default21", proxy.getDefaultStringByGetDefaultString("1", "2"));
    }

    private interface ITestDefaultMethod {
        default String getDefaultString() {
            return "default";
        }

        default String getDefaultString(String arg1, String arg2) {
            return "default" + arg1 + arg2;
        }

        default String getDefaultStringByGetString(String arg1) {
            return getTestString() + arg1;
        }

        default String getDefaultStringByGetDefaultString(String arg1, String arg2) {
            return getDefaultString(arg2, arg1);
        }

        String getTestString();
    }
}
