package io.lambdacube.aspecio.aspect.interceptor;

import io.lambdacube.aspecio.aspect.interceptor.arguments.Arguments;

public interface Advice {
    // @formatter:off
    interface ArgumentHook {
        BeforeAction visitArguments(Arguments arguments);

        Arguments updateArguments(Arguments arguments);
    }
    
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

    interface Catch {
        public static final int PHASE = CallReturn.PHASE << 1;
        
        // You should only use this to propagate exceptions already thrown,
        // checked exceptions declared in the method's `throws` clause
        // or RuntimeExceptions.
        Throwable reThrow(Throwable t);
    }

    interface Finally {
        public static final int PHASE = Catch.PHASE << 1;

        void runFinally();
    }
    // @formatter:on

    
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

    BeforeAction initialAction();

    int afterPhases();
    
    default boolean hasPhase(int phase) {
        return (phase & afterPhases()) != 0;
    }
    
}