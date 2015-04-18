
package net.atomshare.satori;

import spark.*;

import java.net.URLEncoder;

public class Server extends BaseServer {
    public static void main(String[] args) {
        Server server = new Server();
        Spark.get("/", server::index);
        Spark.get("/login", server::login);
    }

    public String login(Request request, Response response) {
        return renderTemplate("/templates/login.html");
    }

    public String index(Request request, Response response) {
        Session session = getSession(request);
        if(!session.isValid()) {
            return redirectToLogin(request, response);
        } else {
            return renderTemplate("/templates/index.html");
        }
    }

    private String redirectToLogin(Request request, Response response) {
        response.redirect("/login?next=" + Util.urlEncode(
                request.url()));
        return "";
    }

    private Session getSession(Request request) {
        String token = request.cookie("satori_token");
        return new Session(token);
    }
}