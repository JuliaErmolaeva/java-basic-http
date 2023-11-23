package ru.otus.ermolaeva.http;

public class MainApplication {
    public static final int PORT = 8189;

    public static void main(String[] args) {
        Server server = new Server(PORT);
        server.start();
    }
}