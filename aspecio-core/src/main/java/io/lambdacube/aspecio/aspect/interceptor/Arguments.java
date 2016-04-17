package io.lambdacube.aspecio.aspect.interceptor;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;

public interface Arguments {

    interface Builder {

        <T> void setObjectArg(String argName, T newValue);

        void setIntArg(String argName, int newValue);

        void setShortArg(String argName, short newValue);

        void setLongArg(String argName, long newValue);

        void setByteArg(String argName, byte newValue);

        void setBooleanArg(String argName, boolean newValue);

        void setFloatArg(String argName, float newValue);

        void setDoubleArg(String argName, double newValue);

        void setCharArg(String argName, char newValue);

        Arguments build();
    }

    public static final Arguments EMPTY_ARGUMENTS = new Arguments() {

        public final IllegalArgumentException NO_ARG_EXCEPTION = new IllegalArgumentException("no argument");

        public List<Parameter> parameters() {
            return Collections.emptyList();
        };

        public <T> T objectArg(String argName) {
            throw NO_ARG_EXCEPTION;
        }

        public int intArg(String argName) {
            throw NO_ARG_EXCEPTION;
        };

        public short shortArg(String argName) {
            throw NO_ARG_EXCEPTION;
        };

        public long longArg(String argName) {
            throw NO_ARG_EXCEPTION;
        };

        public byte byteArg(String argName) {
            throw NO_ARG_EXCEPTION;
        };

        public boolean booleanArg(String argName) {
            throw NO_ARG_EXCEPTION;
        };

        public float floatArg(String argName) {
            throw NO_ARG_EXCEPTION;
        };

        public double doubleArg(String argName) {
            throw NO_ARG_EXCEPTION;
        };

        public char charArg(String argName) {
            throw NO_ARG_EXCEPTION;
        };
    };

    List<Parameter> parameters();

    <T> T objectArg(String argName);

    int intArg(String argName);

    short shortArg(String argName);

    long longArg(String argName);

    byte byteArg(String argName);

    boolean booleanArg(String argName);

    float floatArg(String argName);

    double doubleArg(String argName);

    char charArg(String argName);

}