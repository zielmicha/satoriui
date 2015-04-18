
package net.atomshare.satori;

import spark.*;

public class Server {
    public static void main(String[] args) {
        Spark.get("/", (request, response)
                -> "User: username=test, email=test@test.net");
    }
}