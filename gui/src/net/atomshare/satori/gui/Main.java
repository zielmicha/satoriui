package net.atomshare.satori.gui;

import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        Map<String, String> env = System.getenv();
        if(env.get("RUNSERVER") != null) {
            net.atomshare.satori.Server.main(args);
        } else {
            LoginScreen.main(args);
        }
    }
}
