package io.primeval.aspecio.internal.weaving.theory;

import java.io.PrintStream;
import java.lang.reflect.Parameter;
import java.util.List;

import io.primeval.aspecio.aspect.interceptor.arguments.Arguments;
import io.primeval.aspecio.aspect.interceptor.arguments.ArgumentsUpdater;

public final class M1ArgsUpdater implements ArgumentsUpdater {

    public final List<Parameter> parameters;

    public PrintStream ps;

    public int i;

    public byte b;

    public String s;

    public M1ArgsUpdater(List<Parameter> parameters, PrintStream ps, int i, byte b, String s) {
        this.parameters = parameters;
        this.ps = ps;
        this.i = i;
        this.b = b;
        this.s = s;
    }

    @Override
    public List<Parameter> parameters() {
        return parameters;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T objectArg(String argName) {
        if (argName.equals("ps")) {
            return (T) ps;
        } else if (argName.equals("s")) {
            return (T) s;
        } else {
            throw new IllegalArgumentException("No object parameter named " + argName);
        }
    }

    @Override
    public int intArg(String argName) {
        switch (argName) {
        default:
            throw new IllegalArgumentException("No int parameter named " + argName);
        }
    }

    @Override
    public short shortArg(String argName) {
        throw new IllegalArgumentException("Bad type");
    }

    @Override
    public long longArg(String argName) {
        throw new IllegalArgumentException("No object parameter named " + argName);
    }

    @Override
    public byte byteArg(String argName) {
        switch (argName) {
        case "b":
            return b;
        default:
            throw new IllegalArgumentException("No byte parameter named " + argName);
        }
    }

    @Override
    public boolean booleanArg(String argName) {
        throw new IllegalArgumentException("Bad type");
    }

    @Override
    public float floatArg(String argName) {
        throw new IllegalArgumentException("Bad type");
    }

    @Override
    public double doubleArg(String argName) {
        throw new IllegalArgumentException("Bad type");
    }

    @Override
    public char charArg(String argName) {
        throw new IllegalArgumentException("Bad type");
    }

    @Override
    public Arguments update() {
        return new M1Args(parameters, ps, i, b, s);
    }

    @Override
    public <T> ArgumentsUpdater setObjectArg(String argName, T newValue) {
        if (argName.equals("ps")) {
            ps = (PrintStream) newValue;
            return this;
        } else if (argName.equals("s")) {
            s = (String) newValue;
            return this;
        } else {
            throw new IllegalArgumentException("No object parameter named " + argName);
        }
    }

    @Override
    public ArgumentsUpdater setIntArg(String argName, int newValue) {
        if (argName.equals("i")) {
            i = newValue;
            return this;
        } else {
            throw new IllegalArgumentException("No object parameter named " + argName);
        }
    }

    @Override
    public ArgumentsUpdater setShortArg(String argName, short newValue) {
        return this;
    }

    @Override
    public ArgumentsUpdater setLongArg(String argName, long newValue) {
        return this;
    }

    @Override
    public ArgumentsUpdater setByteArg(String argName, byte newValue) {
        return this;
    }

    @Override
    public ArgumentsUpdater setBooleanArg(String argName, boolean newValue) {
        return this;
    }

    @Override
    public ArgumentsUpdater setFloatArg(String argName, float newValue) {
        return this;
    }

    @Override
    public ArgumentsUpdater setDoubleArg(String argName, double newValue) {
        return this;
    }

    @Override
    public ArgumentsUpdater setCharArg(String argName, char newValue) {
        return this;
    }

}
