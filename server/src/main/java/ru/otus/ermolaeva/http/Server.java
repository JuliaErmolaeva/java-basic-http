package ru.otus.ermolaeva.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int port;
    private static final Logger logger = LogManager.getLogger(MainApplication.class);
    public static ExecutorService serv;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Сервер запущен, порт: " + port);

            Map<String, MyWebApplication> router = new HashMap<>();
            router.put("/calculator", new CalculatorWebApplication());
            router.put("/greetings", new GreetingsWebApplication());
            try {
                router.put("/items", new ItemsWebApplication());
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }

            System.out.println("Построен маппинг для точек назначения:");
            router.forEach((key, value) -> System.out.println(key + ": " + value.getClass().getSimpleName()));


            serv = Executors.newFixedThreadPool(4);

            while (true) {
                Socket socket = serverSocket.accept();
                serv.execute(() -> {
                    try {
                        clientConnect(socket, router);
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
            logger.error(e.getStackTrace());
            e.printStackTrace();
        }
    }

    public static void clientConnect(Socket socket, Map<String, MyWebApplication> router) throws IOException {
        logger.info("Клиент подключился");

        Request request = new Request(socket.getInputStream());
        request.show();
        boolean executed = false;
        for (Map.Entry<String, MyWebApplication> e : router.entrySet()) {
            if (request.getUri().startsWith(e.getKey())) {
                e.getValue().execute(request, socket.getOutputStream());
                executed = true;
                break;
            }
        }
        if (!executed) {
            StringBuilder html = new StringBuilder();

            html.append("<html><body><h1>Построен маппинг для точек назначения:</h1>");

            router.forEach((key, value) -> html.append("<h2>").append(key)
                    .append(": ")
                    .append(value.getClass().getSimpleName())
                    .append("</h2>"));

            html.append("</body></html>");

            socket.getOutputStream().write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Type: text/html;charset=utf-8\r\n\r\n" +
                            html).getBytes(StandardCharsets.UTF_8));
        }
    }
}
