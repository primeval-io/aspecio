package io.lambdacube.aspecio.examples.greetings.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.lambdacube.aspecio.aspect.interceptor.Advice;
import io.lambdacube.aspecio.aspect.interceptor.Advice.ArgumentHook;
import io.lambdacube.aspecio.aspect.interceptor.Advice.CallReturn;
import io.lambdacube.aspecio.aspect.interceptor.Advice.SkipCall;
import io.lambdacube.aspecio.aspect.interceptor.Advices;
import io.lambdacube.aspecio.aspect.interceptor.Arguments;
import io.lambdacube.aspecio.aspect.interceptor.BeforeAction;
import io.lambdacube.aspecio.aspect.interceptor.CallContext;
import io.lambdacube.aspecio.aspect.interceptor.Interceptor;
import io.lambdacube.aspecio.examples.greetings.Goodbye;
import io.lambdacube.aspecio.examples.greetings.Hello;

public final class TheoreticallyWovenComp implements Hello, Goodbye {

    private final HelloGoodbyeImpl delegate;
    private final List<Interceptor> aspectProviders = new ArrayList<>();

    public TheoreticallyWovenComp(HelloGoodbyeImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public String hello() throws Throwable {
        Method m = getClass().getMethod("hello");
        CallContext cc = new CallContext(getClass(), m, m.getParameters());
        // PRE CODE
        List<Advice> advices = new ArrayList<>();
        for (Interceptor aspectProvider : aspectProviders) {
            Advice advice = aspectProvider.onCall(cc);
            advices.add(advice);
        }
        Advice adv = Advices.compose(advices);

        Arguments currentArgs = null;

        BeforeAction initialAction = adv.initialAction();
        switch (initialAction) {
        case SKIP_AND_RETURN: {
            Advice.SkipCall skipCall = (SkipCall) adv;
            return skipCall.skipCallAndReturnObject();
        }
        case REQUEST_ARGUMENTS: {
            Advice.ArgumentHook argumentHook = (ArgumentHook) adv;
            if (currentArgs == null) {
                currentArgs = null; // TODO gen Arguments
            }
            BeforeAction nextAction = argumentHook.visitArguments(currentArgs);
            switch (nextAction) {
            case SKIP_AND_RETURN:
                Advice.SkipCall skipCall = (SkipCall) adv;
                return skipCall.skipCallAndReturnObject();
            case UPDATE_ARGUMENTS_AND_PROCEED:
                currentArgs = argumentHook.updateArguments(null);
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
                Advice.CallReturn callReturn = (CallReturn) adv;
                returnVal = callReturn.onObjectReturn(returnVal);
            }
            return returnVal;
        } catch (Throwable throwable) {
            if ((adv.hasPhase(Advice.Catch.PHASE))) {
                Advice.Catch catch1 = (Advice.Catch) adv;
                throwable = catch1.reThrow(throwable);
            }
            throw throwable;
        } finally {
            if ((adv.hasPhase(Advice.Finally.PHASE))) {
                Advice.Finally finally1 = (Advice.Finally) adv;
                finally1.runFinally();
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

}
