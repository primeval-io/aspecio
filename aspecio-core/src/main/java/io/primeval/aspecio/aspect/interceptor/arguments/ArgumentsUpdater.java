package io.primeval.aspecio.aspect.interceptor.arguments;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;

import io.primeval.aspecio.aspect.interceptor.Advice;

/**
 * <p>
 * This interface allows an {@link Advice} to update arguments before a method invocation.
 * </p>
 * <p>
 * {@link ArgumentsUpdater} instances generated Aspecio are mutable.
 * </p>
 */
public interface ArgumentsUpdater extends ArgumentsTrait {

    /**
     * An implementation of an {@link ArgumentsUpdater} for a parameter-less method.
     */
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

    /**
     * Build the {@link Arguments} object corresponding to the current state of this {@link ArgumentsUpdater}.<br>
     * An {@link Advice} should call this method in {@link Advice.ArgumentHook#updateArguments(Arguments)} once it is
     * done changing values.
     * 
     * @return the {@link Arguments} object.
     */
    Arguments update();

    /**
     * <p>
     * Set the object parameter named argName. Object parameters are all non-native parameters (extending {@link Object}
     * ). It is up to the aspect writer to make sure the object is assignable to type {@code T}.
     * </p>
     * 
     * @param <T>
     *            The type of the parameter named argName
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param newValue
     *            The new value, must be of a type assignable to the parameter named {@literal argName}
     * @throws IllegalArgumentException
     *             if no Object parameter named {@code argName} exists in this method.
     * @return This updated {@link ArgumentsUpdater} instance, for method chaining.
     */
    <T> ArgumentsUpdater setObjectArg(String argName, T newValue);

    /**
     * <p>
     * Set the int parameter named argName. int parameters are all method parameters strictly typed with primitive type
     * {@code int}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param newValue
     *            The new value, must be of a type assignable to the parameter named {@literal argName}
     * @throws IllegalArgumentException
     *             if no int parameter named {@code argName} exists in this method.
     * @return This updated {@link ArgumentsUpdater} instance, for method chaining.
     */
    ArgumentsUpdater setIntArg(String argName, int newValue);

    /**
     * <p>
     * Set the short parameter named argName. short parameters are all method parameters strictly typed with primitive
     * type {@code short}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param newValue
     *            The new value, must be of a type assignable to the parameter named {@literal argName}
     * @throws IllegalArgumentException
     *             if no short parameter named {@code argName} exists in this method.
     * @return This updated {@link ArgumentsUpdater} instance, for method chaining.
     */
    ArgumentsUpdater setShortArg(String argName, short newValue);

    /**
     * <p>
     * Set the long parameter named argName. long parameters are all method parameters strictly typed with primitive
     * type {@code long}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param newValue
     *            The new value, must be of a type assignable to the parameter named {@literal argName}
     * @throws IllegalArgumentException
     *             if no long parameter named {@code argName} exists in this method.
     * @return This updated {@link ArgumentsUpdater} instance, for method chaining.
     */
    ArgumentsUpdater setLongArg(String argName, long newValue);

    /**
     * <p>
     * Set the long parameter named argName. long parameters are all method parameters strictly typed with primitive
     * type {@code long}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param newValue
     *            The new value, must be of a type assignable to the parameter named {@literal argName}
     * @throws IllegalArgumentException
     *             if no long parameter named {@code argName} exists in this method.
     * @return This updated {@link ArgumentsUpdater} instance, for method chaining.
     */
    ArgumentsUpdater setByteArg(String argName, byte newValue);

    /**
     * <p>
     * Set the boolean parameter named argName. boolean parameters are all method parameters strictly typed with
     * primitive type {@code boolean}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param newValue
     *            The new value, must be of a type assignable to the parameter named {@literal argName}
     * @throws IllegalArgumentException
     *             if no boolean parameter named {@code argName} exists in this method.
     * @return This updated {@link ArgumentsUpdater} instance, for method chaining.
     */
    ArgumentsUpdater setBooleanArg(String argName, boolean newValue);

    /**
     * <p>
     * Set the float parameter named argName. float parameters are all method parameters strictly typed with primitive
     * type {@code float}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param newValue
     *            The new value, must be of a type assignable to the parameter named {@literal argName}
     * @throws IllegalArgumentException
     *             if no float parameter named {@code argName} exists in this method.
     * @return This updated {@link ArgumentsUpdater} instance, for method chaining.
     */
    ArgumentsUpdater setFloatArg(String argName, float newValue);

    /**
     * <p>
     * Set the double parameter named argName. double parameters are all method parameters strictly typed with primitive
     * type {@code double}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param newValue
     *            The new value, must be of a type assignable to the parameter named {@literal argName}
     * @throws IllegalArgumentException
     *             if no double parameter named {@code argName} exists in this method.
     * @return This updated {@link ArgumentsUpdater} instance, for method chaining.
     */
    ArgumentsUpdater setDoubleArg(String argName, double newValue);

    /**
     * <p>
     * Set the char parameter named argName. char parameters are all method parameters strictly typed with primitive
     * type {@code char}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param newValue
     *            The new value, must be of a type assignable to the parameter named {@literal argName}
     * @throws IllegalArgumentException
     *             if no char parameter named {@code argName} exists in this method.
     * @return This updated {@link ArgumentsUpdater} instance, for method chaining.
     */
    ArgumentsUpdater setCharArg(String argName, char newValue);

}