
package net.atomshare.satori;

import com.google.gson.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import spark.*;

import java.io.IOException;

public class Server extends BaseServer<Session> {
    public static void main(String[] args) throws IOException, TTransportException {
        Spark.port(7000);
        //Spark.staticFileLocation("/");
        Spark.externalStaticFileLocation("res");

        Server server = new Server();
        server.initRoutes();
        server.sessionFactory.startConnectionCreator();
    }

    public Server() throws IOException, TTransportException {
        sessionFactory = new SessionFactory();
    }

    private SessionFactory sessionFactory;

    public void initRoutes() {
        Spark.get("/contests", withREST(this::getContests));
        Spark.get("/global-news", withREST(this::getGlobalNews));
        Spark.get("/page-info/:id", withREST(this::getPageInfo));
        Spark.get("/news/:id", withREST(this::getNews));
        Spark.get("/results/:id", withREST(this::getResults));
        Spark.get("/problems/:id", withREST(this::getProblems));
    }

    private Object getNews(Session session, Request request) throws TException {
        return session.getNews(Long.parseLong(request.params("id")));
    }

    private Object getGlobalNews(Session session, Request request) throws TException {
        return session.getGlobalNews();
    }

    private Object getResults(Session session, Request request) throws TException {
        return session.getResults(Long.parseLong(request.params("id")));
    }

    public Object getContests(Session session, Request request) throws TException {
        return session.getContests();
    }

    public Object getPageInfo(Session session, Request request) throws TException {
        return session.getPageInfo(Long.parseLong(request.params("id")));
    }

    public Object getProblems(Session session, Request request) throws TException {
        return session.getProblems(Long.parseLong(request.params("id")));
    }

    public String login(Request request, Response response) {
        return "login";
    }

    public String index(Request request, Response response) {
        return "hello";
    }

    protected Session getSession(Request request, Response response) throws TException {
        String authData = request.headers("Authorization");
        if(authData == null || authData.length() == 0) {
            response.header("WWW-Authenticate", "Basic realm=\"Satori credentials\"");
            Spark.halt(401);
            return null;
        }
        if(!authData.startsWith("Basic "))
            throw new RuntimeException("bad auth header");
        String cred = new String(Base64.decodeBase64(authData.substring(6)));
        return sessionFactory.get(cred);
    }
}