
package net.atomshare.satori;

import spark.*;

import java.net.URLEncoder;

public class Server extends BaseServer {
    public static void main(String[] args) {
        Spark.port(7000);
        //Spark.staticFileLocation("/");
        Spark.externalStaticFileLocation("res");

        Server server = new Server();
        //Spark.get("/", server::index);
        Spark.get("/login", server::login);
    }

    public String login(Request request, Response response) {
        return "login";
    }

    public String index(Request request, Response response) {
        return "hello";
    }

    private Session getSession(Request request) {
        String token = request.cookie("satori_token");
        return new Session(token);
    }
}