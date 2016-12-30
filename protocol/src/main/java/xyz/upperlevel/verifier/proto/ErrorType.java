package xyz.upperlevel.verifier.proto;

public enum ErrorType {
    LOGIN_BAD_USER, LOGIN_BAD_PASSWORD, ASSIGNMENT, TEST_TYPE, MISC, NOT_LOGGED_ID;

    private final static ErrorType[] types = values();

    public static ErrorType get(int id) {
        return types[id];
    }

    public int getId() {
        return this.ordinal();
    }
}
