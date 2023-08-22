package com.conorsmine.net.webserver;

import com.conorsmine.net.PlayerDataManipulator;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;

public class WebServer {

    private final PlayerDataManipulator pl;
    private int PORT;
    private HttpServer server;

    public WebServer(PlayerDataManipulator pl, int PORT) {
        this.pl = pl;
        this.PORT = PORT;
    }

    public String getLink() {
        return server.getAddress().toString();
    }

    public void initServer(@Nullable final Integer port) {
        if (port != null) this.PORT = port;

        try {
            // Setup server
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new WebHTTPHandler(pl));
            server.start();
        }
        catch (BindException _e) { pl.sendMsg(String.format("Port: %d is already in use, please define another in the config.yml file.", PORT)); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }
}
