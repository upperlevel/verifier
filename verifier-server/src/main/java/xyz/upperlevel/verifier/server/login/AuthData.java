package xyz.upperlevel.verifier.server.login;

import lombok.Getter;
import lombok.Setter;

public class AuthData {

    @Getter
    private final String username;
    @Getter
    private final String clazz;
    @Getter
    private char[] password;

    @Getter
    @Setter
    private boolean logged = false;

    public AuthData(String clazz, String username, char[] password) {
        this.clazz = clazz;
        this.username = username.toLowerCase();
        this.password = password;
    }

    public void setPassword(char[] password) {
        System.out.println("[WARNING]changed password of \"" + username + "\"!");
        this.password = password;
    }
}
