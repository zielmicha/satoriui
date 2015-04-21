package net.atomshare.satori;

import com.google.gson.*;
import org.apache.thrift.TException;
import spark.*;

public abstract class BaseServer<T> {
    protected BaseServer() {
    }

    private Gson gson = new GsonBuilder().create();

    public interface RestHandler<T> {
        Object handle(T session, Request request) throws TException;
    };

    public Route withREST(RestHandler<T> handler) {
        return (request, response) -> {
            response.type("application/json; charset=utf-8");
            T session = getSession(request, response);
            Object ret = handler.handle(session, request);
            return gson.toJson(ret);
        };
    }

    protected abstract T getSession(Request request, Response response) throws TException;
}

