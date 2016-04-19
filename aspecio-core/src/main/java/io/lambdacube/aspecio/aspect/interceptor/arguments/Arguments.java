package io.lambdacube.aspecio.aspect.interceptor.arguments;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;

public interface Arguments extends ArgLike {

    public static final Arguments EMPTY_ARGUMENTS = new Arguments() {

        public final IllegalArgumentException NO_ARG_EXCEPTION = new IllegalArgumentException("no argument");

        public List<Parameter> parameters() {
            return Collections.emptyList();
        };

        public ArgumentsUpdater updater() {
            return ArgumentsUpdater.EMPTY_ARGUMENTS_UPDATER;
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
    };

    ArgumentsUpdater updater();
}