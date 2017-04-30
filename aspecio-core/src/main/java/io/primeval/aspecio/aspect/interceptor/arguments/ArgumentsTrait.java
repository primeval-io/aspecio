package io.primeval.aspecio.aspect.interceptor.arguments;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * A trait shared by {@link Arguments} and {@link ArgumentsUpdater}, that provides access to the arguments of a specific
 * intercepted method invocation.
 *
 */
public interface ArgumentsTrait {

    /**
     * The parameter of the method. You may need to test them to retrieve the proper type of each argument. Use
     * {@link Parameter#getName()} as the argument name that you may use in other methods in this interface.<br>
     * If you build your code with Java 8's {@code -parameters} javac argument, the parameter names will be those from
     * the original code in the woven service. Otherwise, they will be synthetic arguments {@code arg0 } to {@code argN}
     * 
     * 
     * @return the parameters corresponding to this method invocation
     * @throws UnsupportedOperationException
     *             if you try to modify the list. The parameters are immutable.
     */
    List<Parameter> parameters();

    /**
     * <p>
     * The object parameter named argName. Object parameters are all non-native parameters (extending {@link Object}).
     * It is up to the aspect writer to make sure the object is assignable to type {@code T}.
     * </p>
     * 
     * @param <T>
     *            The type of the parameter named argName
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @throws IllegalArgumentException
     *             if no Object parameter named {@code argName} exists in this method.
     * @return The value of the argument, for this specific method interception.
     */
    <T> T objectArg(String argName);

    /**
     * <p>
     * The object parameter named argName. Object parameters are all non-native parameters (extending {@link Object}).
     * It is up to the aspect writer to make sure the object is assignable to type {@code T}.
     * </p>
     * <p>
     * This is an helper method to disambiguate calls to objectArg, however it is up to the developer to make sure the
     * object under the argument {@code argName} is assignable to class {@code clazz}
     * </p>
     * 
     * @param <T>
     *            The type of the parameter named argName
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @param clazz
     *            A superclass of the object passed as parameter {@code argName} for this method invocation.
     * 
     * @return The value of the argument, for this specific method interception.
     */
    @SuppressWarnings("unchecked")
    default <T> T objectArg(String argName, Class<T> clazz) {
        return (T) objectArg(argName);
    }

    /**
     * <p>
     * The int parameter named argName. int parameters are all method parameters strictly typed with primitive type
     * {@code int}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @throws IllegalArgumentException
     *             if no int parameter named {@code argName} exists in this method.
     * @return The value of the argument, for this specific method interception.
     */
    int intArg(String argName);

    /**
     * <p>
     * The short parameter named argName. short parameters are all method parameters strictly typed with primitive type
     * {@code short}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @throws IllegalArgumentException
     *             if no short parameter named {@code argName} exists in this method.
     * @return The value of the argument, for this specific method interception.
     */
    short shortArg(String argName);

    /**
     * <p>
     * The long parameter named argName. long parameters are all method parameters strictly typed with primitive type
     * {@code long}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @throws IllegalArgumentException
     *             if no long parameter named {@code argName} exists in this method.
     * @return The value of the argument, for this specific method interception.
     */
    long longArg(String argName);

    /**
     * <p>
     * The byte parameter named argName. byte parameters are all method parameters strictly typed with primitive type
     * {@code byte}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @throws IllegalArgumentException
     *             if no byte parameter named {@code argName} exists in this method.
     * @return The value of the argument, for this specific method interception.
     */
    byte byteArg(String argName);

    /**
     * <p>
     * The boolean parameter named argName. boolean parameters are all method parameters strictly typed with primitive
     * type {@code boolean}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @throws IllegalArgumentException
     *             if no boolean parameter named {@code argName} exists in this method.
     * @return The value of the argument, for this specific method interception.
     */
    boolean booleanArg(String argName);

    /**
     * <p>
     * The float parameter named argName. float parameters are all method parameters strictly typed with primitive type
     * {@code float}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @throws IllegalArgumentException
     *             if no float parameter named {@code argName} exists in this method.
     * @return The value of the argument, for this specific method interception.
     */
    float floatArg(String argName);

    /**
     * <p>
     * The double parameter named argName. double parameters are all method parameters strictly typed with primitive
     * type {@code double}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @throws IllegalArgumentException
     *             if no double parameter named {@code argName} exists in this method.
     * @return The value of the argument, for this specific method interception.
     */
    double doubleArg(String argName);

    /**
     * <p>
     * The char parameter named argName. char parameters are all method parameters strictly typed with primitive type
     * {@code char}.
     * </p>
     * 
     * @param argName
     *            The argument name, as provided by the {@code parameters()} method.
     * @throws IllegalArgumentException
     *             if no char parameter named {@code argName} exists in this method.
     * @return The value of the argument, for this specific method interception.
     */
    char charArg(String argName);

}