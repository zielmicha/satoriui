package net.atomshare.satori;

public class Session {
    private final String token;

    public Session(String token) {
        this.token = token;
    }

    public boolean isValid() {
        return token != null && token.length() > 0;
    }
}
