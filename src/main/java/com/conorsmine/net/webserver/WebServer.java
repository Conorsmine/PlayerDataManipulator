package com.conorsmine.net.webserver;

import com.conorsmine.net.PlayerDataManipulator;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;

public class WebServer {

    private final int PORT;
    private HttpServer server;

    public WebServer(int PORT) {
        this.PORT = PORT;
    }

    public String getLink() {
        return server.getAddress().toString();
    }

    public void initServer() {
        try {
            // Setup server
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new WebHTTPHandler());
            server.start();

            PlayerDataManipulator.staticSendMsg(String.format("Server started successfully on port %d.", PORT));
        }
        catch (BindException _e) { PlayerDataManipulator.staticSendMsg(String.format("Port: %d is already in use, please define another in the config.yml file.", PORT)); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void stop() {
        server.stop(0);
    }
}
