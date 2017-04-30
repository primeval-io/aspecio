package io.primeval.aspecio.internal.weaving;

import java.util.Arrays;

public final class MethodIdentifier {

    public final String name;
    public final Class<?>[] parameterTypes;

    public MethodIdentifier(String name, Class<?>... parameterTypes) {
        super();
        this.name = name;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + Arrays.hashCode(parameterTypes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodIdentifier other = (MethodIdentifier) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (!Arrays.equals(parameterTypes, other.parameterTypes))
            return false;
        return true;
    }

}