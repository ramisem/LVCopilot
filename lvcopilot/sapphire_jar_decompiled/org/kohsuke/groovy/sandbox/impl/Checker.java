/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.Closure
 *  groovy.lang.GroovyRuntimeException
 *  groovy.lang.MetaClass
 *  groovy.lang.MetaClassImpl
 *  groovy.lang.MetaMethod
 *  groovy.lang.MissingMethodException
 *  groovy.lang.MissingPropertyException
 *  org.codehaus.groovy.runtime.InvokerHelper
 *  org.codehaus.groovy.runtime.MetaClassHelper
 *  org.codehaus.groovy.runtime.ScriptBytecodeAdapter
 *  org.codehaus.groovy.runtime.callsite.CallSite
 *  org.codehaus.groovy.runtime.callsite.CallSiteArray
 */
package org.kohsuke.groovy.sandbox.impl;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.CallSiteArray;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.kohsuke.groovy.sandbox.impl.ClosureSupport;
import org.kohsuke.groovy.sandbox.impl.Ops;
import org.kohsuke.groovy.sandbox.impl.SingleArgInvokerChain;
import org.kohsuke.groovy.sandbox.impl.Super;
import org.kohsuke.groovy.sandbox.impl.TwoArgInvokerChain;
import org.kohsuke.groovy.sandbox.impl.VarArgInvokerChain;
import org.kohsuke.groovy.sandbox.impl.ZeroArgInvokerChain;

public class Checker {
    private static CallSite fakeCallSite(String method) {
        CallSiteArray csa = new CallSiteArray(Checker.class, new String[]{method});
        return csa.array[0];
    }

    public static Object checkedCall(Object _receiver, boolean safe, boolean spread, String _method, Object[] _args) throws Throwable {
        MetaClassImpl mci;
        MetaMethod m;
        MetaClass mc;
        if (safe && _receiver == null) {
            return null;
        }
        _args = Checker.fixNull(_args);
        if (spread) {
            ArrayList<Object> r = new ArrayList<Object>();
            Iterator itr = InvokerHelper.asIterator((Object)_receiver);
            while (itr.hasNext()) {
                Object it = itr.next();
                if (it == null) continue;
                r.add(Checker.checkedCall(it, true, false, _method, _args));
            }
            return r;
        }
        if (_receiver instanceof Class && (mc = InvokerHelper.getMetaClass((Class)((Class)_receiver))) instanceof MetaClassImpl && (m = (mci = (MetaClassImpl)mc).retrieveStaticMethod(_method, _args)) != null && m.isStatic()) {
            if (m.getDeclaringClass().getTheClass() == Class.class) {
                return Checker.checkedStaticCall(Class.class, _method, _args);
            }
            return Checker.checkedStaticCall((Class)_receiver, _method, _args);
        }
        if (_receiver instanceof Closure) {
            MetaMethod m2;
            if (_method.equals("invokeMethod") && Checker.isInvokingMethodOnClosure(_receiver, _method, _args)) {
                _method = _args[0].toString();
                _args = (Object[])_args[1];
            }
            if ((m2 = InvokerHelper.getMetaClass((Object)_receiver).pickMethod(_method, MetaClassHelper.convertToTypeArray((Object[])_args))) == null) {
                List<Object> targets = ClosureSupport.targetsOf((Closure)_receiver);
                Class[] argTypes = MetaClassHelper.convertToTypeArray((Object[])_args);
                for (Object candidate : targets) {
                    if (InvokerHelper.getMetaClass((Object)candidate).pickMethod(_method, argTypes) == null) continue;
                    return Checker.checkedCall(candidate, false, false, _method, _args);
                }
                for (Object candidate : targets) {
                    try {
                        return Checker.checkedCall(candidate, false, false, "invokeMethod", new Object[]{_method, _args});
                    }
                    catch (MissingMethodException missingMethodException) {
                    }
                }
            }
        }
        return new VarArgInvokerChain(_receiver){

            @Override
            public Object call(Object receiver, String method, Object ... args) throws Throwable {
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onMethodCall(this, receiver, method, args);
                }
                return Checker.fakeCallSite(method).call(receiver, args);
            }
        }.call(_receiver, _method, _args);
    }

    private static boolean isInvokingMethodOnClosure(Object receiver, String method, Object ... args) {
        MetaMethod m;
        return receiver instanceof Closure && (m = InvokerHelper.getMetaClass((Object)receiver).pickMethod(method, MetaClassHelper.convertToTypeArray((Object[])args))) != null && m.getDeclaringClass().isAssignableFrom(Closure.class);
    }

    public static Object checkedStaticCall(Class _receiver, String _method, Object[] _args) throws Throwable {
        return new VarArgInvokerChain(_receiver){

            @Override
            public Object call(Object receiver, String method, Object ... args) throws Throwable {
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onStaticCall(this, (Class)receiver, method, args);
                }
                return Checker.fakeCallSite(method).callStatic((Class)receiver, args);
            }
        }.call((Object)_receiver, _method, Checker.fixNull(_args));
    }

    public static Object checkedConstructor(Class _type, Object[] _args) throws Throwable {
        return new VarArgInvokerChain(_type){

            @Override
            public Object call(Object receiver, String method, Object ... args) throws Throwable {
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onNewInstance(this, (Class)receiver, args);
                }
                return Checker.fakeCallSite("<init>").callConstructor((Object)((Class)receiver), args);
            }
        }.call((Object)_type, (String)null, Checker.fixNull(_args));
    }

    public static Object checkedSuperCall(Class _senderType, Object _receiver, String _method, Object[] _args) throws Throwable {
        Super s = new Super(_senderType, _receiver);
        return new VarArgInvokerChain(s){

            @Override
            public Object call(Object _s, String method, Object ... args) throws Throwable {
                Super s = (Super)_s;
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onSuperCall(this, s.senderType, s.receiver, method, args);
                }
                MetaClass mc = InvokerHelper.getMetaClass(s.receiver.getClass());
                return mc.invokeMethod(s.senderType.getSuperclass(), s.receiver, method, args, true, true);
            }
        }.call((Object)s, _method, Checker.fixNull(_args));
    }

    public static SuperConstructorWrapper checkedSuperConstructor(final Class<?> superClass, Object[] args) throws Throwable {
        new VarArgInvokerChain(superClass){

            @Override
            public Object call(Object receiver, String method, Object ... args) throws Throwable {
                if (this.chain.hasNext()) {
                    ((GroovyInterceptor)this.chain.next()).onSuperConstructor(this, superClass, args);
                }
                return null;
            }
        }.call((Object)superClass, (String)null, Checker.fixNull(args));
        return new SuperConstructorWrapper(args);
    }

    public static Object checkedGetProperty(Object _receiver, boolean safe, boolean spread, Object _property) throws Throwable {
        if (safe && _receiver == null) {
            return null;
        }
        if (spread) {
            ArrayList<Object> r = new ArrayList<Object>();
            Iterator itr = InvokerHelper.asIterator((Object)_receiver);
            while (itr.hasNext()) {
                Object it = itr.next();
                if (it == null) continue;
                r.add(Checker.checkedGetProperty(it, true, false, _property));
            }
            return r;
        }
        if (Checker.isInvokingMethodOnClosure(_receiver, "getProperty", _property) && !ClosureSupport.BUILTIN_PROPERTIES.contains(_property)) {
            MissingPropertyException x = null;
            for (Object candidate : ClosureSupport.targetsOf((Closure)_receiver)) {
                try {
                    return Checker.checkedGetProperty(candidate, false, false, _property);
                }
                catch (MissingPropertyException e) {
                    x = e;
                }
            }
            if (x != null) {
                throw x;
            }
            throw new MissingPropertyException(_property.toString(), _receiver.getClass());
        }
        if (_receiver instanceof Map) {
            return Checker.checkedCall(_receiver, false, false, "get", new Object[]{_property});
        }
        return new ZeroArgInvokerChain(_receiver){

            @Override
            public Object call(Object receiver, String property) throws Throwable {
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onGetProperty(this, receiver, property);
                }
                return ScriptBytecodeAdapter.getProperty(null, (Object)receiver, (String)property);
            }
        }.call(_receiver, _property.toString());
    }

    public static Object checkedSetProperty(Object _receiver, Object _property, boolean safe, boolean spread, int op, Object _value) throws Throwable {
        if (op != 100) {
            Object v = Checker.checkedGetProperty(_receiver, safe, spread, _property);
            return Checker.checkedSetProperty(_receiver, _property, safe, spread, 100, Checker.checkedBinaryOp(v, Ops.compoundAssignmentToBinaryOperator(op), _value));
        }
        if (safe && _receiver == null) {
            return _value;
        }
        if (spread) {
            Iterator itr = InvokerHelper.asIterator((Object)_receiver);
            while (itr.hasNext()) {
                Object it = itr.next();
                if (it == null) continue;
                Checker.checkedSetProperty(it, _property, true, false, op, _value);
            }
            return _value;
        }
        if (Checker.isInvokingMethodOnClosure(_receiver, "setProperty", _property, _value) && !ClosureSupport.BUILTIN_PROPERTIES.contains(_property)) {
            GroovyRuntimeException x = null;
            for (Object candidate : ClosureSupport.targetsOf((Closure)_receiver)) {
                try {
                    return Checker.checkedSetProperty(candidate, _property, false, false, op, _value);
                }
                catch (GroovyRuntimeException e) {
                    x = e;
                }
            }
            if (x != null) {
                throw x;
            }
            throw new MissingPropertyException(_property.toString(), _receiver.getClass());
        }
        if (_receiver instanceof Map) {
            Checker.checkedCall(_receiver, false, false, "put", new Object[]{_property, _value});
            return _value;
        }
        return new SingleArgInvokerChain(_receiver){

            @Override
            public Object call(Object receiver, String property, Object value) throws Throwable {
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onSetProperty(this, receiver, property, value);
                }
                ScriptBytecodeAdapter.setProperty((Object)value, null, (Object)receiver, (String)property);
                return value;
            }
        }.call(_receiver, _property.toString(), _value);
    }

    public static Object checkedGetAttribute(Object _receiver, boolean safe, boolean spread, Object _property) throws Throwable {
        if (safe && _receiver == null) {
            return null;
        }
        if (spread) {
            ArrayList<Object> r = new ArrayList<Object>();
            Iterator itr = InvokerHelper.asIterator((Object)_receiver);
            while (itr.hasNext()) {
                Object it = itr.next();
                if (it == null) continue;
                r.add(Checker.checkedGetAttribute(it, true, false, _property));
            }
            return r;
        }
        return new ZeroArgInvokerChain(_receiver){

            @Override
            public Object call(Object receiver, String property) throws Throwable {
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onGetAttribute(this, receiver, property);
                }
                return ScriptBytecodeAdapter.getField(null, (Object)receiver, (String)property);
            }
        }.call(_receiver, _property.toString());
    }

    public static Object checkedSetAttribute(Object _receiver, Object _property, boolean safe, boolean spread, int op, Object _value) throws Throwable {
        if (op != 100) {
            Object v = Checker.checkedGetAttribute(_receiver, safe, spread, _property);
            return Checker.checkedSetAttribute(_receiver, _property, safe, spread, 100, Checker.checkedBinaryOp(v, Ops.compoundAssignmentToBinaryOperator(op), _value));
        }
        if (safe && _receiver == null) {
            return _value;
        }
        if (spread) {
            Iterator itr = InvokerHelper.asIterator((Object)_receiver);
            while (itr.hasNext()) {
                Object it = itr.next();
                if (it == null) continue;
                Checker.checkedSetAttribute(it, _property, true, false, op, _value);
            }
        } else {
            return new SingleArgInvokerChain(_receiver){

                @Override
                public Object call(Object receiver, String property, Object value) throws Throwable {
                    if (this.chain.hasNext()) {
                        return ((GroovyInterceptor)this.chain.next()).onSetAttribute(this, receiver, property, value);
                    }
                    ScriptBytecodeAdapter.setField((Object)value, null, (Object)receiver, (String)property);
                    return value;
                }
            }.call(_receiver, _property.toString(), _value);
        }
        return _value;
    }

    public static Object checkedGetArray(Object _receiver, Object _index) throws Throwable {
        return new SingleArgInvokerChain(_receiver){

            @Override
            public Object call(Object receiver, String method, Object index) throws Throwable {
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onGetArray(this, receiver, index);
                }
                return Checker.fakeCallSite("getAt").call(receiver, index);
            }
        }.call(_receiver, (String)null, _index);
    }

    public static Object checkedSetArray(Object _receiver, Object _index, int op, Object _value) throws Throwable {
        if (op != 100) {
            Object v = Checker.checkedGetArray(_receiver, _index);
            return Checker.checkedSetArray(_receiver, _index, 100, Checker.checkedBinaryOp(v, Ops.compoundAssignmentToBinaryOperator(op), _value));
        }
        return new TwoArgInvokerChain(_receiver){

            @Override
            public Object call(Object receiver, String method, Object index, Object value) throws Throwable {
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onSetArray(this, receiver, index, value);
                }
                Checker.fakeCallSite("putAt").call(receiver, index, value);
                return value;
            }
        }.call(_receiver, (String)null, _index, _value);
    }

    public static Object checkedPostfixArray(Object r, Object i, String op) throws Throwable {
        Object o = Checker.checkedGetArray(r, i);
        Object n = Checker.checkedCall(o, false, false, op, new Object[0]);
        Checker.checkedSetArray(r, i, 100, n);
        return o;
    }

    public static Object checkedPrefixArray(Object r, Object i, String op) throws Throwable {
        Object o = Checker.checkedGetArray(r, i);
        Object n = Checker.checkedCall(o, false, false, op, new Object[0]);
        Checker.checkedSetArray(r, i, 100, n);
        return n;
    }

    public static Object checkedPostfixProperty(Object receiver, Object property, boolean safe, boolean spread, String op) throws Throwable {
        Object o = Checker.checkedGetProperty(receiver, safe, spread, property);
        Object n = Checker.checkedCall(o, false, false, op, new Object[0]);
        Checker.checkedSetProperty(receiver, property, safe, spread, 100, n);
        return o;
    }

    public static Object checkedPrefixProperty(Object receiver, Object property, boolean safe, boolean spread, String op) throws Throwable {
        Object o = Checker.checkedGetProperty(receiver, safe, spread, property);
        Object n = Checker.checkedCall(o, false, false, op, new Object[0]);
        Checker.checkedSetProperty(receiver, property, safe, spread, 100, n);
        return n;
    }

    public static Object checkedBinaryOp(Object lhs, int op, Object rhs) throws Throwable {
        return Checker.checkedCall(lhs, false, false, Ops.binaryOperatorMethods(op), new Object[]{rhs});
    }

    public static Object checkedComparison(Object lhs, final int op, Object rhs) throws Throwable {
        if (lhs == null) {
            return InvokerHelper.invokeStaticMethod(ScriptBytecodeAdapter.class, (String)Ops.binaryOperatorMethods(op), (Object)new Object[]{lhs, rhs});
        }
        return new SingleArgInvokerChain(lhs){

            @Override
            public Object call(Object lhs, String method, Object rhs) throws Throwable {
                if (this.chain.hasNext()) {
                    return ((GroovyInterceptor)this.chain.next()).onMethodCall(this, lhs, lhs instanceof Comparable ? "compareTo" : "equals", rhs);
                }
                return InvokerHelper.invokeStaticMethod(ScriptBytecodeAdapter.class, (String)Ops.binaryOperatorMethods(op), (Object)new Object[]{lhs, rhs});
            }
        }.call(lhs, (String)null, rhs);
    }

    public static Object checkedCast(Class<?> clazz, Object exp, boolean ignoreAutoboxing, boolean coerce, boolean strict) throws Throwable {
        if (!(!coerce || exp == null || Collection.class.isAssignableFrom(clazz) && clazz.getPackage().getName().equals("java.util"))) {
            if (clazz.isInterface()) {
                for (Method m : clazz.getMethods()) {
                    Object[] args = new Object[m.getParameterTypes().length];
                    for (int i = 0; i < args.length; ++i) {
                        args[i] = Checker.getDefaultValue(m.getParameterTypes()[i]);
                    }
                    new VarArgInvokerChain(exp){

                        @Override
                        public Object call(Object receiver, String method, Object ... args) throws Throwable {
                            if (this.chain.hasNext()) {
                                if (receiver instanceof Class) {
                                    return ((GroovyInterceptor)this.chain.next()).onStaticCall(this, (Class)receiver, method, args);
                                }
                                return ((GroovyInterceptor)this.chain.next()).onMethodCall(this, receiver, method, args);
                            }
                            return null;
                        }
                    }.call(exp, m.getName(), args);
                }
            } else if (!clazz.isArray() && clazz != Object.class && !Modifier.isAbstract(clazz.getModifiers()) && (exp instanceof Collection || exp.getClass().isArray() || exp instanceof Map)) {
                Object[] args = null;
                if (exp instanceof Collection) {
                    args = ((Collection)exp).toArray();
                } else if (exp instanceof Map) {
                    args = new Object[]{exp};
                } else {
                    throw new UnsupportedOperationException("casting arrays to types via constructor is not yet supported");
                }
                if (args != null) {
                    new VarArgInvokerChain(clazz){

                        @Override
                        public Object call(Object receiver, String method, Object ... args) throws Throwable {
                            if (this.chain.hasNext()) {
                                return ((GroovyInterceptor)this.chain.next()).onNewInstance(this, (Class)receiver, args);
                            }
                            return null;
                        }
                    }.call((Object)clazz, (String)null, args);
                }
            }
        }
        return strict ? clazz.cast(exp) : (coerce ? ScriptBytecodeAdapter.asType((Object)exp, clazz) : ScriptBytecodeAdapter.castToType((Object)exp, clazz));
    }

    private static <T> T getDefaultValue(Class<T> clazz) {
        return (T)Array.get(Array.newInstance(clazz, 1), 0);
    }

    private static Object[] fixNull(Object[] args) {
        return args == null ? new Object[1] : args;
    }

    public static class SuperConstructorWrapper {
        private final Object[] args;

        SuperConstructorWrapper(Object[] args) {
            this.args = args;
        }

        public Object arg(int idx) {
            return this.args[idx];
        }
    }
}

