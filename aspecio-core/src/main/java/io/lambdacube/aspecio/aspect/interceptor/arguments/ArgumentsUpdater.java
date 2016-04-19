package io.lambdacube.aspecio.aspect.interceptor.arguments;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;

public interface ArgumentsUpdater extends ArgLike {
    
    public static final ArgumentsUpdater EMPTY_ARGUMENTS_UPDATER = new ArgumentsUpdater() {

        public final IllegalArgumentException NO_ARG_EXCEPTION = new IllegalArgumentException("no argument");

        public List<Parameter> parameters() {
            return Collections.emptyList();
        };

        public Arguments update() {
            return Arguments.EMPTY_ARGUMENTS;
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
        }

        @Override
        public <T> ArgumentsUpdater setObjectArg(String argName, T newValue) {
            throw NO_ARG_EXCEPTION;
        }

        @Override
        public ArgumentsUpdater setIntArg(String argName, int newValue) {
            throw NO_ARG_EXCEPTION;
        }

        @Override
        public ArgumentsUpdater setShortArg(String argName, short newValue) {
            throw NO_ARG_EXCEPTION;
        }

        @Override
        public ArgumentsUpdater setLongArg(String argName, long newValue) {
            throw NO_ARG_EXCEPTION;
        }

        @Override
        public ArgumentsUpdater setByteArg(String argName, byte newValue) {
            throw NO_ARG_EXCEPTION;
        }

        @Override
        public ArgumentsUpdater setBooleanArg(String argName, boolean newValue) {
            throw NO_ARG_EXCEPTION;
        }

        @Override
        public ArgumentsUpdater setFloatArg(String argName, float newValue) {
            throw NO_ARG_EXCEPTION;
        }

        @Override
        public ArgumentsUpdater setDoubleArg(String argName, double newValue) {
            throw NO_ARG_EXCEPTION;
        }

        @Override
        public ArgumentsUpdater setCharArg(String argName, char newValue) {
            throw NO_ARG_EXCEPTION;
        };
    };
    
    Arguments update();

    <T> ArgumentsUpdater setObjectArg(String argName, T newValue);

    ArgumentsUpdater setIntArg(String argName, int newValue);

    ArgumentsUpdater setShortArg(String argName, short newValue);

    ArgumentsUpdater setLongArg(String argName, long newValue);

    ArgumentsUpdater setByteArg(String argName, byte newValue);

    ArgumentsUpdater setBooleanArg(String argName, boolean newValue);

    ArgumentsUpdater setFloatArg(String argName, float newValue);

    ArgumentsUpdater setDoubleArg(String argName, double newValue);

    ArgumentsUpdater setCharArg(String argName, char newValue);
    
}