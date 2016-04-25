package io.lambdacube.aspecio.aspect.interceptor;

import io.lambdacube.aspecio.aspect.interceptor.arguments.Arguments;

/**
 * An Advice composed of several Advices. The components are called by iteration in a flat-way.
 */
public final class CompositeAdvice implements AdviceAdapter {

    private final Advice[] advices;

    public CompositeAdvice(Advice[] advices) {
        super();
        this.advices = advices;
    }

    private static BeforeAction narrow(BeforeAction b1, BeforeAction b2) {
        return b1.compareTo(b2) < 0 ? b1 : b2;
    }

    @Override
    public BeforeAction initialAction() {
        BeforeAction beforeAction = BeforeAction.PROCEED;
        for (Advice adv : advices) {
            beforeAction = narrow(beforeAction, adv.initialAction());
        }
        return beforeAction;
    }

    @Override
    public int afterPhases() {
        int phases = 0;
        for (Advice adv : advices) {
            phases |= adv.afterPhases();
        }
        return phases;
    }

    @Override
    public BeforeAction visitArguments(Arguments arguments) {
        BeforeAction beforeAction = BeforeAction.PROCEED;
        for (Advice adv : advices) {
            if (adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS) {
                ArgumentHook ah = (ArgumentHook) adv;
                beforeAction = narrow(beforeAction, ah.visitArguments(arguments));
            }
        }
        return beforeAction;
    }

    @Override
    public Arguments updateArguments(Arguments arguments) {
        Arguments args = arguments;
        for (Advice adv : advices) {
            if (adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.UPDATE_ARGUMENTS_AND_PROCEED) {
                ArgumentHook ah = (ArgumentHook) adv;
                args = ah.updateArguments(arguments);
            }
        }
        return args;
    }

    @Override
    public <T> T skipCallAndReturnObject() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnObject();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public int skipCallAndReturnInt() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnInt();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public void skipCallAndReturnVoid() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                skipCall.skipCallAndReturnVoid();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public short skipCallAndReturnShort() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnShort();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public double skipCallAndReturnDouble() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnDouble();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public float skipCallAndReturnFloat() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnFloat();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public char skipCallAndReturnChar() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnChar();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public long skipCallAndReturnLong() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnLong();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public byte skipCallAndReturnByte() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnByte();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public boolean skipCallAndReturnBoolean() {
        for (Advice adv : advices) {
            if ((adv.initialAction() == BeforeAction.REQUEST_ARGUMENTS || adv.initialAction() == BeforeAction.SKIP_AND_RETURN)
                    && (adv instanceof SkipCall)) {
                SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnBoolean();
            }
        }
        throw new AssertionError("one aspect should have returned.");
    }

    @Override
    public <T> T onObjectReturn(T result) {
        T res = result;
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                res = callReturn.onObjectReturn(res);
            }
        }
        return res;
    }

    @Override
    public void onVoidReturn() {
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                callReturn.onVoidReturn();
            }
        }
    }

    @Override
    public short onShortReturn(short result) {
        short res = result;
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                res = callReturn.onShortReturn(res);
            }
        }
        return res;
    }

    @Override
    public int onIntReturn(int result) {
        int res = result;
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                res = callReturn.onIntReturn(res);
            }
        }
        return res;
    }

    @Override
    public double onDoubleReturn(double result) {
        double res = result;
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                res = callReturn.onDoubleReturn(res);
            }
        }
        return res;
    }

    @Override
    public float onFloatReturn(float result) {
        float res = result;
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                res = callReturn.onFloatReturn(res);
            }
        }
        return res;
    }

    @Override
    public char onCharReturn(char result) {
        char res = result;
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                res = callReturn.onCharReturn(res);
            }
        }
        return res;
    }

    @Override
    public long onLongReturn(long result) {
        long res = result;
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                res = callReturn.onLongReturn(res);
            }
        }
        return res;
    }

    @Override
    public byte onByteReturn(byte result) {
        byte res = result;
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                res = callReturn.onByteReturn(res);
            }
        }
        return res;
    }

    @Override
    public boolean onBooleanReturn(boolean result) {
        boolean res = result;
        for (Advice adv : advices) {
            if (adv.hasPhase(CallReturn.PHASE)) {
                CallReturn callReturn = (CallReturn) adv;
                res = callReturn.onBooleanReturn(res);
            }
        }
        return res;
    }

    @Override
    public Throwable reThrow(Throwable t) {
        Throwable res = t;
        for (Advice adv : advices) {
            if (adv.hasPhase(Catch.PHASE)) {
                Catch catch_ = (Catch) adv;
                res = catch_.reThrow(res);
            }
        }
        return res;
    }

    @Override
    public void runFinally() {
        for (Advice adv : advices) {
            if (adv.hasPhase(Finally.PHASE)) {
                Finally fin = (Finally) adv;
                fin.runFinally();
            }
        }
    }

}
