package io.lambdacube.aspecio.internal.weaving;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class TestUtils {

    public static final Comparator<Type> TYPE_COMPARATOR = new Comparator<Type>() {

        @Override
        public int compare(Type o1, Type o2) {
            if (TestUtils.typeEquals(o1, o2)) {
                return 0;
            } else {
                return -1;
            }
        }
    };
    

    public static final boolean typeEquals(Type o1, Type o2) {
        if (Objects.equals(o1, o2)) {
            return true;
        }
        if (o1.getClass() != o2.getClass()) {
            return false;
        }
        if (o1 instanceof TypeVariable<?>) {
            TypeVariable<?> t1 = (TypeVariable<?>) o1;
            TypeVariable<?> t2 = (TypeVariable<?>) o2;

            if (Objects.equals(t1.getName(), t2.getName()) && Arrays.equals(t1.getBounds(), t2.getBounds())) {
                return true;
            }

        } else if (o1 instanceof ParameterizedType) {
            ParameterizedType t1 = (ParameterizedType) o1;
            ParameterizedType t2 = (ParameterizedType) o2;

            Type[] args1 = t1.getActualTypeArguments();
            Type[] args2 = t2.getActualTypeArguments();

            if (!(t1.getRawType() == t2.getRawType() && args1.length == args2.length)) {
                return false;
            }
            for (int i = 0; i < args1.length; i++) {
                if (!typeEquals(args1[i], args2[i])) {
                    return false;
                }
            }
            return true;

        }
        return false;
    }

}
