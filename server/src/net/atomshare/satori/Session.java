package net.atomshare.satori;

import net.atomshare.satori.thrift.gen.User;

public class Session {
    private final String token;

    public String getToken() {
        return token;
    }

    public Session(String token) {
        this.token = token;
    }

    public Session authenticate() {
        return null;
    }

    public boolean isValid() {
        return token != null && token.length() > 0;
    }
}
