package com.example

class TestClassWithSomeMoreViolationsExcluded {

    int deadCode() {
        return 0
        return 1
    }

    def emptyIfStatement(int a) {
        if (a % 2 == 0) {}
    }
}