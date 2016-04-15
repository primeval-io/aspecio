package io.lambdacube.aspecio.aspect.interceptor;

public interface Interceptor {

    public final static Interceptor NOOP = new Interceptor() {
        @Override
        public Advice onCall(CallContext callContext) {
            return Advice.DEFAULT;
        }
        
        public String toString() {
            return "NOOP Interceptor";
        };
    };

    Advice onCall(CallContext callContext);
    
}
