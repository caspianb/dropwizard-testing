package com.logicalbias.dropwizard.testing.extension.utils;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TestHelpers {

    private TestHelpers() {
    }

    /**
     * Splits a string in the format key=value into an array with exactly two elements.
     *
     * @throws IllegalArgumentException if the string does not follow the key=value format.
     */
    public static String[] splitProperty(String property) {
        var props = property.split("=");
        if (props.length != 2) {
            throw new IllegalArgumentException("Invalid property detected: " + property);
        }
        return props;
    }

    /**
     * Splits a string in the format key=value into an array with exactly two elements. Any
     * variables in the string in the form of ${token} will be replaced with the token value.
     *
     * @throws IllegalArgumentException if the string does not follow the key=value format.
     */
    public static String[] splitPropertyWithVariable(String property, String token, String tokenValue) {
        var props = property.split("=");
        if (props.length != 2) {
            throw new IllegalArgumentException("Invalid property detected: " + property);
        }

        // Look for and replace any ${variable} values on right side with the value
        props[1] = props[1].replace("${" + token + "}", tokenValue);
        return props;
    }

    /**
     * Returns all instances of the specified annotation starting from the provided
     * class and going up the class hierarchy in order.
     */
    public static <T extends Annotation> List<T> findAnnotations(final Class<?> classType, Class<T> annotation) {
        return buildClassInheritanceTree(classType).stream()
                .map(type -> type.getDeclaredAnnotation(annotation))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Generates a list of all inherited types up to Object for the specified class.
     * The list will be in BFS ordering (types closer to definition of the specified
     * type are first in the list).
     */
    public static List<Class<?>> buildClassInheritanceTree(final Class<?> classType) {
        // The inheritance tree may contain duplicate references; store in LinkedHashSet to
        // ensure ordering and distinctiveness of tree is maintained
        var inheritedTypes = new LinkedHashSet<Class<?>>();
        inheritedTypes.add(classType);

        var bfsQueue = new ArrayDeque<Class<?>>();
        bfsQueue.add(classType);

        while (!bfsQueue.isEmpty()) {
            var current = bfsQueue.pop();
            if (current == Object.class) continue;

            // Add current type to our list
            inheritedTypes.add(current);

            var superClass = current.getSuperclass();
            if (superClass != null) {
                bfsQueue.add(superClass);
            }

            bfsQueue.addAll(List.of(current.getInterfaces()));
        }

        return List.copyOf(inheritedTypes);
    }
}
