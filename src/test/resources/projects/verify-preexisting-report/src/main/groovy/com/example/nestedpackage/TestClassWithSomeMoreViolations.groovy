package com.example

class TestClassWithSomeMoreViolations {

    int deadCode() {
        return 0
        return 1
    }

    def emptyIfStatement(int a) {
        if (a % 2 == 0) {}
    }
}