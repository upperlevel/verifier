package me.upperlevel.verifier.proto;

public enum ErrorType {
    LOGIN, ASSIGNMENT, TEST_TYPE, MISC;

    private final static ErrorType[] types = values();

    public static ErrorType get(int id) {
        return types[id];
    }

    public int getId() {
        return this.ordinal();
    }
}
