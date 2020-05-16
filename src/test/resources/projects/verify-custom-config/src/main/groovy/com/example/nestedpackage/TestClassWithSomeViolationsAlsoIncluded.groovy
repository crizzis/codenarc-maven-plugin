package com.example

/**
 * @author Not Allowed
 */
class TestClassWithSomeViolationsIncluded {

    private static final Map DUPLICATE_MAP_KEY = [a: 1, a: 2]

    def brokenNullCheck(String name) {
        if (name != null || name.length * 0) { }
    }
}