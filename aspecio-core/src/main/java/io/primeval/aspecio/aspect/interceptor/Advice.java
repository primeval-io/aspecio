package io.primeval.aspecio.aspect.interceptor;

import java.lang.reflect.Method;

import io.primeval.aspecio.aspect.interceptor.arguments.Arguments;

/**
 * <p>
 * Advices are Aspecio's way to describe an Aspect's behavior.
 * </p>
 * <p>
 * The Advice API is designed to let the Aspect programmer minimize the impact of Aspects on the execution, by allowing
 * to specify precisely the callbacks she is interested in. To that end, the Advice API is using a <i>mixin</i>
 * approach: composable interfaces for the different events in the interception lifecycle.
 * </p>
 * <p>
 * Aspecio interception is split in two:
 * </p>
 * <ul>
 * <li>The <b>Before</b> phase, that takes place <i>before</i> the intercepted method call. <br>
 * There are two mixins in the Before phase:
 * <ul>
 * <li>The {@link ArgumentHook} mixin, that lets the Advice access the method call arguments and optionally update them
 * before the call ;</li>
 * <li>and the {@link SkipCall} mixin, that lets the Advice return a value even before the method call took place, thus
 * completely skipping it.</li>
 * </ul>
 * </li>
 * <li>The <b>After</b> phase, that takes place <i>after</i> the intercepted method call. <br>
 * There are three mixins in the After phase:
 * <ul>
 * <li>The {@link CallReturn} mixin, that lets the Advice access the method's return value and optionally change them ;
 * </li>
 * <li>the {@link Catch} mixin, that lets the Advice catch exceptions thrown by the method call, and optionally change
 * them, but never completely swallow them ;</li>
 * <li>the {@link Finally} mixin, that lets the Advice run code that happens after the method execution in all cases.
 * </li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * Advices specify the events they are interested in the <i>Before</i> phase by returning {@link BeforeAction} in the
 * appropriate method.<br>
 * For an Advice, the entry point of the Interception lifecycle is its method {@link Advice#initialAction()}.
 * </p>
 * <ul>
 * <li>Returning {@link BeforeAction#PROCEED} will jump straight to the intercepted method call ;</li>
 * <li>Returning {@link BeforeAction#SKIP_AND_RETURN} will tell Aspecio to call the {@link SkipCall} trait in the Advice
 * mixin, so {@link SkipCall} <b>must</b> be implemented. The actual method of {@link SkipCall} that will be called
 * depends on the return type of the intercepted method call. You may find that type using {@link CallContext#method}
 * and {@link Method#getReturnType()}.</li>
 * <li>Returning {@link BeforeAction#REQUEST_ARGUMENTS} will tell Aspecio to call
 * {@link ArgumentHook#visitArguments(Arguments)}, so {@link ArgumentHook} must be implemented.
 * {@link ArgumentHook#visitArguments(Arguments)} may itself return a {@link BeforeAction} to give Aspecio the next
 * Before action to execute. The accepted values are {@link BeforeAction#PROCEED} to go straight to the method call ;
 * {@link BeforeAction#SKIP_AND_RETURN} to skip the intercepted call and return a value based on the visited arguments ;
 * and finally {@link BeforeAction#UPDATE_ARGUMENTS_AND_PROCEED} to update the arguments passed to the method call
 * before proceeding.</li>
 * </ul>
 * <p>
 * Advices specify the events they are interested in the <i>After</i> phase by defining a <i>bitmask</i>
 * {@link Advice#afterPhases()}. The different masks available, for each After phase, are {@link CallReturn#PHASE},
 * {@link Catch#PHASE} and {@link Finally#PHASE}. If an Advice requests an After phase through a mask, it <b>must</b>
 * implement the corresponding phase trait.<br>
 * For instance, an Advice interested in the {@link CallReturn} and {@link Catch} phases could define
 * {@link Advice#afterPhases()} with the following code:
 * </p>
 * 
 * <pre>
 * {@code
 * public int aferPhases() {
 *     return CallReturn.PHASE + Catch.PHASE;
 * }
 * }
 * </pre>
 * 
 * <p>
 * However, {@link Advice#afterPhases()} is free to return different values depending on the {@link CallContext}: the
 * result is never cached by Aspecio.
 * </p>
 * <p>
 * It is often preferable to implement {@link AdviceAdapter} rather than {@link Advice} as it makes it easier to define
 * dynamic Advices, that is Advices that can behave differently depending on the {@link CallContext}.
 * </p>
 *
 */
public interface Advice {
    // @formatter:off
    
    /**
     * A trait to receive an intercepted method call's {@link Arguments}.
     * @see Advice
     */
    interface ArgumentHook {
        BeforeAction visitArguments(Arguments arguments);

        Arguments updateArguments(Arguments arguments);
    }
    
    /**
     * A trait to ask Aspecio to return early, before the intercepted call is executed.
     * @see Advice
     */
    interface SkipCall {
        <T> T skipCallAndReturnObject();
        int skipCallAndReturnInt();
        void skipCallAndReturnVoid();
        short skipCallAndReturnShort();
        double skipCallAndReturnDouble();
        float skipCallAndReturnFloat();
        char skipCallAndReturnChar();
        long skipCallAndReturnLong();
        byte skipCallAndReturnByte();
        boolean skipCallAndReturnBoolean();
    }
    
    /**
     * A trait to receive the intercepted method return value from Aspecio, as long as {@link Advice#afterPhases()} requests the phase {@link CallReturn#PHASE}.
     * @see Advice
     */
    interface CallReturn {
        public static final int PHASE = 1;
        
        <T> T onObjectReturn(T result);
        void onVoidReturn();
        short onShortReturn(short result);
        int onIntReturn(int result);
        double onDoubleReturn(double result);
        float onFloatReturn(float result);
        char onCharReturn(char result);
        long onLongReturn(long result);
        byte onByteReturn(byte result);
        boolean onBooleanReturn(boolean result);
    }

    /**
     * A trait to receive the intercepted method exceptions from Aspecio, as long as {@link Advice#afterPhases()} requests the phase {@link Catch#PHASE}.
     * @see Advice
     */
    interface Catch {
        public static final int PHASE = CallReturn.PHASE << 1;
        
        // You should only use this to propagate exceptions already thrown,
        // checked exceptions declared in the method's `throws` clause
        // or RuntimeExceptions.
        Throwable reThrow(Throwable t);
    }

    /**
     * A trait to run code after the method call in all conditions, as long as {@link Advice#afterPhases()} requests the phase {@link Finally#PHASE}.
     * @see Advice
     */
    interface Finally {
        public static final int PHASE = Catch.PHASE << 1;

        void runFinally();
    }
    // @formatter:on

    /**
     * An Advice doing nothing.
     */
    public static final Advice DEFAULT = new Advice() {

        @Override
        public BeforeAction initialAction() {
            return BeforeAction.PROCEED;
        }

        @Override
        public int afterPhases() {
            return 0;
        }
    };

    /**
     * The first action to tell Aspecio to do when this Advice intercepts a method.
     * @see Advice
     * @return The initial action.
     */
    BeforeAction initialAction();

    /**
     * The After phases this Advice will take part in.
     * @see Advice
     * @return the bitmask for the requested After phases.
     */
    int afterPhases();


    default boolean hasPhase(int phase) {
        return (phase & afterPhases()) != 0;
    }

}