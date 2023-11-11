package ru.otus.ermolaeva.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApplication {
    private static final Logger logger = LogManager.getLogger(MainApplication.class.getName());
    private static final int PORT = 8189;

    private static final Map<String, MyWebApplication> ROUTER = new HashMap<>() {{
        put("/calculator", new CalculatorWebApplication());
        put("/greetings", new GreetingsWebApplication());
    }};

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Сервер запущен, " + PORT);
            ExecutorService executorService = Executors.newFixedThreadPool(3);

            while (true) {

                Socket socket = serverSocket.accept();
                executorService.execute(() -> {
                    try {
                        clientHandler(socket);
                    } catch (IOException e) {
                        logger.error(e.getStackTrace());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            if (!socket.isClosed()) {
                                socket.close();
                            }
                        } catch (IOException e) {
                            logger.error(e.getStackTrace());
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clientHandler(Socket socket) throws IOException {
        logger.info("Клиент подключился");

        byte[] buffer = new byte[2048];
        int n = socket.getInputStream().read(buffer);
        String rawRequest = new String(buffer, 0, n);

        Request request = new Request(rawRequest);
        request.show();

        boolean executed = false;
        for (Map.Entry<String, MyWebApplication> e : ROUTER.entrySet()) {
            if (request.getUri().startsWith(e.getKey())) {
                e.getValue().execute(request, socket.getOutputStream());
                executed = true;
                break;
            }
        }
        if (!executed) {
            socket.getOutputStream().write(("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>Unknown application<h1></body></html>").getBytes(StandardCharsets.UTF_8));
        }
    }
}