package io.lambdacube.aspecio.aspect.interceptor;

public interface Arguments {
    final Arguments EMPTY_ARGUMENTS = new Arguments() {

        @Override
        public Object objectNamed(String argName) {
            return null;
        }

        @Override
        public Object objectAt(int position) {
            return null;
        }
    };

    Object objectAt(int position);

    Object objectNamed(String argName);
}