package com.conorsmine.net.webserver;

import com.conorsmine.net.PlayerDataManipulator;
import com.conorsmine.net.Properties;
import com.conorsmine.net.files.WebsiteFile;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;

public class WebHTTPHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String requestPath = exchange.getRequestURI().getPath();
            int responseCode = 200;
            byte[] responseBytes;

            if (requestPath.matches(String.format(".*/%s/%s\\.json", Properties.URL_REQUEST_PREFIX, Properties.UUID_REGEX)))
                responseBytes = handlePlayerDataParseRequest(requestPath);
            else if (requestPath.startsWith(String.format("/%s/", Properties.URL_CHANGES_PREFIX)))
                responseBytes = handlePlayerDataChangePost(requestPath, exchange);
            else responseBytes = WebsiteFile.getResourceAsBytes(requestPath);

            exchange.getResponseHeaders().add("Content-Type", determineFileType(requestPath).contentDefinition);
            exchange.sendResponseHeaders(responseCode, responseBytes.length);

            // Write the response content to the output stream
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseBytes);
            outputStream.close();
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    private byte[] handlePlayerDataParseRequest(final String requestPath) {
        return getPlayerDataResource(requestPath.replaceAll(".*/playerData/", ""));
    }

    private byte[] handlePlayerDataChangePost(final String requestPath, final HttpExchange exchange) throws IOException {
        final StringBuilder builder = new StringBuilder();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        String s = reader.readLine();
        while (s != null) { builder.append(s); s = reader.readLine(); }

        try {
            final JSONObject json = (JSONObject) new JSONParser().parse(builder.toString());
            WebsiteFile.setJSONOfChangeFile(requestPath.replaceAll(".+/", ""), json);
        } catch (Exception e) { e.printStackTrace(); }


        return new byte[0];
    }

    private byte[] getPlayerDataResource(final String fileID) {
        final File jsonFileFromID = WebsiteFile.getParsedFileFromID(fileID);
        if (jsonFileFromID == null) return new byte[0];

        try { return Files.readAllBytes(jsonFileFromID.toPath()); }
        catch (IOException e) { e.printStackTrace(); }

        return new byte[0];
    }

    private byte[] getResource(final String path) {
        try { return WebsiteFile.getResourceAsBytes(path); }
        catch (IOException e) { e.printStackTrace(); }

        return new byte[0];
    }






    private static ContentTypes determineFileType(final String resource) {
        final String end = resource.replaceFirst("^.+\\.", "");

        for (ContentTypes type : ContentTypes.values()) {
            if (!type.type.equalsIgnoreCase(end)) continue;
            return type;
        }

        PlayerDataManipulator.staticSendMsg(String.format("Website does not support contenttype: \"%s\"", end), String.format(">> %s", resource));
        return ContentTypes.HTML;
    }

    private enum ContentTypes {
        HTML    ("html", "text/html"),
        CSS     ("css", "text/css"),
        PNG     ("png", "image/png"),
        JS      ("js", "application/javascript"),
        TTF     ("ttf", "font/ttf"),
        ICO     ("ico", "image/x-icon"),
        JSON    ("json", "application/json");

        public final String type, contentDefinition;

        ContentTypes(String type, String contentDefinition) {
            this.type = type;
            this.contentDefinition = contentDefinition;
        }
    }
}
