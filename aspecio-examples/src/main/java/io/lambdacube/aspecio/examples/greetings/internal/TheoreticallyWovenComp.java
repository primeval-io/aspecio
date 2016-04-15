package io.lambdacube.aspecio.examples.greetings.internal;

import java.io.PrintStream;
import java.lang.reflect.Method;

import io.lambdacube.aspecio.aspect.interceptor.Advice;
import io.lambdacube.aspecio.aspect.interceptor.Advice.ArgumentHook;
import io.lambdacube.aspecio.aspect.interceptor.Advice.CallReturn;
import io.lambdacube.aspecio.aspect.interceptor.Advice.SkipCall;
import io.lambdacube.aspecio.aspect.interceptor.Arguments;
import io.lambdacube.aspecio.aspect.interceptor.BeforeAction;
import io.lambdacube.aspecio.aspect.interceptor.CallContext;
import io.lambdacube.aspecio.examples.greetings.Goodbye;
import io.lambdacube.aspecio.examples.greetings.Hello;

public final class TheoreticallyWovenComp extends Woven implements Hello, Goodbye {

    private final static Method meth0 = null; // WovenUtils.getMethodUnchecked(HelloGoodbyeImpl.class, "hello");
    private final static CallContext cc0 = new CallContext(HelloGoodbyeImpl.class, meth0, meth0.getParameters());

    private final static Method meth1 =  null; // WovenUtils.getMethodUnchecked(HelloGoodbyeImpl.class, "test", PrintStream.class, int.class, byte.class, String.class);
    private final static CallContext cc1 = new CallContext(HelloGoodbyeImpl.class, meth1, meth1.getParameters());

    private final HelloGoodbyeImpl delegate;

    public TheoreticallyWovenComp(HelloGoodbyeImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public String hello() throws Throwable {
        Advice adv = interceptor.onCall(cc0);

        Arguments currentArgs = null;

        BeforeAction initialAction = adv.initialAction();
        switch (initialAction) {
        case SKIP_AND_RETURN: {
            return ((SkipCall) adv).skipCallAndReturnObject();
        }
        case REQUEST_ARGUMENTS: {
            Advice.ArgumentHook argumentHook = (ArgumentHook) adv;
            if (currentArgs == null) {
                currentArgs = Arguments.EMPTY_ARGUMENTS; // TODO gen Arguments
            }
            BeforeAction nextAction = argumentHook.visitArguments(currentArgs);
            switch (nextAction) {
            case SKIP_AND_RETURN:
                return ((SkipCall) adv).skipCallAndReturnObject();
            case UPDATE_ARGUMENTS_AND_PROCEED:
                currentArgs = argumentHook.updateArguments(currentArgs);
                break;
            default:
                break;
            }
        }
        default:
            break;
        }
        try {
            String returnVal = delegate.hello();

            if ((adv.hasPhase(Advice.CallReturn.PHASE))) {
                returnVal = ((CallReturn) adv).onObjectReturn(returnVal);
            }
            return returnVal;
        } catch (Throwable throwable) {
            if ((adv.hasPhase(Advice.Catch.PHASE))) {
                throwable = ((Advice.Catch) adv).reThrow(throwable);
            }
            throw throwable;
        } finally {
            if ((adv.hasPhase(Advice.Finally.PHASE))) {
                ((Advice.Finally) adv).runFinally();
            }
        }
    }

    @Override
    public void test(PrintStream ps, int i, byte b, String s) throws Throwable {
        Advice adv = interceptor.onCall(cc0);

        Arguments currentArgs = null;

        BeforeAction initialAction = adv.initialAction();
        switch (initialAction) {
        case SKIP_AND_RETURN: {
            ((SkipCall) adv).skipCallAndReturnVoid();
            return;
        }
        case REQUEST_ARGUMENTS: {
            Advice.ArgumentHook argumentHook = (ArgumentHook) adv;
            if (currentArgs == null) {
                currentArgs = Arguments.EMPTY_ARGUMENTS; // TODO gen Arguments
            }
            BeforeAction nextAction = argumentHook.visitArguments(currentArgs);
            switch (nextAction) {
            case SKIP_AND_RETURN:
                ((SkipCall) adv).skipCallAndReturnVoid();
                return;
            case UPDATE_ARGUMENTS_AND_PROCEED:
                currentArgs = argumentHook.updateArguments(currentArgs);
                break;
            default:
                break;
            }
        }
        default:
            break;
        }
        try {
            delegate.test(ps, i, b, s);

            if ((adv.hasPhase(Advice.CallReturn.PHASE))) {
                ((CallReturn) adv).onVoidReturn();
            }
            return;
        } catch (Throwable throwable) {
            if ((adv.hasPhase(Advice.Catch.PHASE))) {
                throwable = ((Advice.Catch) adv).reThrow(throwable);
            }
            throw throwable;
        } finally {
            if ((adv.hasPhase(Advice.Finally.PHASE))) {
                ((Advice.Finally) adv).runFinally();
            }
        }
    }

    @Override
    public double foo(double a, int[] b) throws Throwable {
        Advice adv = interceptor.onCall(cc0);

        Arguments currentArgs = null;

        BeforeAction initialAction = adv.initialAction();
        switch (initialAction) {
        case SKIP_AND_RETURN: {
            return ((SkipCall) adv).skipCallAndReturnDouble();
        }
        case REQUEST_ARGUMENTS: {
            Advice.ArgumentHook argumentHook = (ArgumentHook) adv;
            if (currentArgs == null) {
                currentArgs = Arguments.EMPTY_ARGUMENTS; // TODO gen Arguments
            }
            BeforeAction nextAction = argumentHook.visitArguments(currentArgs);
            switch (nextAction) {
            case SKIP_AND_RETURN:
                return ((SkipCall) adv).skipCallAndReturnDouble();
            case UPDATE_ARGUMENTS_AND_PROCEED:
                currentArgs = argumentHook.updateArguments(currentArgs);
                break;
            default:
                break;
            }
        }
        default:
            break;
        }
        try {
            double returnVal = delegate.foo(a, b);

            if ((adv.hasPhase(Advice.CallReturn.PHASE))) {
                returnVal = ((CallReturn) adv).onDoubleReturn(returnVal);
            }
            return returnVal;
        } catch (Throwable throwable) {
            if ((adv.hasPhase(Advice.Catch.PHASE))) {
                throwable = ((Advice.Catch) adv).reThrow(throwable);
            }
            throw throwable;
        } finally {
            if ((adv.hasPhase(Advice.Finally.PHASE))) {
                ((Advice.Finally) adv).runFinally();
            }
        }
    }

    @Override
    public String goodbye() {
        // PRE CODE
        try {
            return delegate.goodbye();
        } finally {
            // POST CODE
        }
    }

    public AssertionError newChecked() {
        throw new AssertionError();
    }

}
