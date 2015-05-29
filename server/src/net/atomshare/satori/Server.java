
package net.atomshare.satori;

import com.google.gson.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import spark.*;

import java.io.*;

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
        Spark.get("/result/:id", withREST(this::getResult));
        Spark.get("/problems/:id", withREST(this::getProblems));

        Spark.get("/blob/:model/:id/:category/:name/:filename", this::getBlob);
    }

    public Object getBlob(Request request, Response requestResponse) throws Exception {
        request.params("category");
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(
                "https://satori.tcs.uj.edu.pl:2887/blob"
                        + "/" + request.params("model")
                        + "/" + request.params("id")
                        + "/" + request.params("category")
                        + "/" + request.params("name"));
        get.setHeader("Cookie", "satori_token=" + getSession(request, requestResponse).getToken());

        String fileName = request.params("filename");
        if (fileName.endsWith(".pdf")) {
            requestResponse.type("application/pdf");
        } else {
            requestResponse.type("application/octet-stream");
        }

        HttpResponse response = client.execute(get);
        byte[] content = EntityUtils.toByteArray(response.getEntity());
        OutputStream out = requestResponse.raw().getOutputStream();
        out.write(content);
        out.flush();
        return null;
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

    public Object getResult(Session session, Request request) {
        return session.getResult(Long.parseLong(request.params("id")));
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