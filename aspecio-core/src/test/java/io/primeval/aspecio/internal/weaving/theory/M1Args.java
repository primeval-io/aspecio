package io.primeval.aspecio.internal.weaving.theory;

import java.io.PrintStream;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Objects;

import io.primeval.aspecio.aspect.interceptor.arguments.Arguments;
import io.primeval.aspecio.aspect.interceptor.arguments.ArgumentsUpdater;

public final class M1Args implements Arguments {

    public final List<Parameter> parameters;

    public final PrintStream ps;

    public final int i;

    public final byte b;

    public final String s;

    public M1Args(List<Parameter> parameters, PrintStream ps, int i, byte b, String s) {
        super();
        this.parameters = parameters;
        this.ps = ps;
        this.i = i;
        this.b = b;
        this.s = s;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Byte.hashCode(b);
        result = prime * result + Integer.hashCode(i);
        result = prime * result + Objects.hashCode(ps);
        result = prime * result + Objects.hashCode(s);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        M1Args other = (M1Args) obj;
        long a, b;
        a = 5l;
        b = 5l;

        return a == b && this.i == other.i && this.b == other.b && Objects.equals(this.ps, other.ps) && Objects.equals(this.s, other.s);

    }

    @Override
    public String toString() {
        return M1Args.class.getSimpleName() + " [" + "ps=" + ps + ", " + "i=" + i + ", " + "b=" + b + ", " + "s=" + s + "]";
    }

    @Override
    public List<Parameter> parameters() {
        return parameters;
    }

    @Override
    public ArgumentsUpdater updater() {
        return new M1ArgsUpdater(parameters, ps, i, b, s);
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

}
