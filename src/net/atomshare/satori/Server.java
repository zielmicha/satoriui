
package net.atomshare.satori;

import com.google.common.io.Resources;
import com.hubspot.jinjava.*;
import com.hubspot.jinjava.loader.ClasspathResourceLocator;
import spark.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public class Server {
    public static void main(String[] args) {
        Server server = new Server();
        Spark.get("/", server::index);
    }

    private final Jinjava templates;

    private String renderTemplate(String name, Object... args) {
        String templateData;
        URL url = getClass().getResource(name);
        if(url == null) {
            throw new RuntimeException("template " + name + " not found");
        }
        try {
            templateData = Resources.asCharSource(
                    url,
                    Charset.forName("UTF-8")).read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return templates.render(templateData,
                Util.<String, Object>makeMap(args));
    }

    public Server() {
        JinjavaConfig config = new JinjavaConfig();
        templates = new Jinjava(config);
        templates.setResourceLocator(new ClasspathResourceLocator());
    }

    public String index(Request request, Response response) {
        return renderTemplate("/templates/index.html");
    }
}