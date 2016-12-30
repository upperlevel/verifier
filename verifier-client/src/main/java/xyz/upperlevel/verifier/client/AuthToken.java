package xyz.upperlevel.verifier.client;

import lombok.Data;

@Data
public class AuthToken {
    private final String clazz, username, password;
}
