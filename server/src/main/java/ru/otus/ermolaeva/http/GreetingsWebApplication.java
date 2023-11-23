package ru.otus.ermolaeva.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class GreetingsWebApplication implements MyWebApplication {
    private String name;
    private static final Logger logger = LogManager.getLogger(GreetingsWebApplication.class);

    public GreetingsWebApplication() {
        this.name = "Greetings Web Application";
    }

    @Override
    public void execute(Request request, OutputStream output) throws IOException {
        String username = request.getParam("username");
        logger.info("username=" + username);
        output.write(("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>" + name + "<h1><h2>Hello, " + username + "<h2></body></html>").getBytes(StandardCharsets.UTF_8));
    }
}
