package com.logicalbias.dropwizard.testing.extension.utils;

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

}
