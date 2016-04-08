package io.lambdacube.aspecio.aspect.interceptor;

public interface AdviceAdapter extends Advice, Advice.SkipCall, Advice.ArgumentHook,
        Advice.CallReturn, Advice.Catch, Advice.Finally {
    
    // @formatter:off
    default BeforeAction visitArguments(Arguments arguments) { return BeforeAction.PROCEED; }

    default Arguments updateArguments(Arguments arguments) { return arguments; }
    
    default <T> T skipCallAndReturnObject() { return null; }
    default int skipCallAndReturnInt() { return 0; }
    default void skipCallAndReturnVoid() { }
    default short skipCallAndReturnShort() { return 0; }
    default double skipCallAndReturnDouble() { return 0; }
    default float skipCallAndReturnFloat() { return 0; }
    default char skipCallAndReturnChar() { return 0; }
    default long skipCallAndReturnLong() { return 0; }
    default byte skipCallAndReturnByte() { return 0; }
    default boolean skipCallAndReturnBoolean() { return false; }
    
    
    // We don't get the return value here, 
    // because we don't deal with boxed types.
    // This is just an utilitary hook.
    default void onSuccessfulReturn() { };
    
    default <T> T onObjectReturn(T result) { onSuccessfulReturn(); return result ; }
    default void onVoidReturn() { onSuccessfulReturn(); }
    default short onShortReturn(short result) { onSuccessfulReturn(); return result; }
    default int onIntReturn(int result) { onSuccessfulReturn(); return result; }
    default double onDoubleReturn(double result) { onSuccessfulReturn(); return result; }
    default float onFloatReturn(float result) { onSuccessfulReturn(); return result; }
    default char onCharReturn(char result) { onSuccessfulReturn(); return result; }
    default long onLongReturn(long result) { onSuccessfulReturn(); return result; }
    default byte onByteReturn(byte result) { onSuccessfulReturn(); return result; }
    default boolean onBooleanReturn(boolean result) { onSuccessfulReturn(); return result; }
    
    @Override
    default Throwable reThrow(Throwable t) { return t; }
    
    @Override
    default void runFinally() { }
    
    default public BeforeAction initialAction() { return BeforeAction.PROCEED; };
    
    @Override
    default int afterPhases() { return 0;}
    
    // @formatter:on

}