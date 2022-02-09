package me.ayunami2000.remotescroll;

import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        String key=String.valueOf(Math.random());
        while(key.length()<=6){
            key=String.valueOf(Math.random());
        }
        key=key.substring(2,6);
        int port=8269;
        System.out.println(key);
        System.out.println(InetAddress.getLocalHost().getHostAddress()+":"+port);
        InputStream inputStream = Main.class.getResourceAsStream("/index.html");
        String indexFile = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, indexFile.length());
            OutputStream os = exchange.getResponseBody();
            os.write(indexFile.getBytes());
            os.close();
        });
        String finalKey = key;
        server.createContext("/scroll", exchange -> {
            String[] params=exchange.getRequestURI().getQuery().split(";",2);
            boolean valid=false;
            boolean clearcookie=true;
            if(params.length==2&&params[0].equals(finalKey)&&params[1].matches("-?\\d+"))valid=true;
            if(valid) {
                try {
                    /*
                    int offset=Integer.parseInt(params[1]);
                    robot.mousePress(MouseEvent.BUTTON2_MASK);
                    Point xd=MouseInfo.getPointerInfo().getLocation();
                    robot.mouseMove(xd.x,xd.y+offset);
                    robot.mouseRelease(MouseEvent.BUTTON2_MASK);
                    robot.mouseMove(xd.x,xd.y-offset);
                    */
                    robot.mouseWheel(Integer.parseInt(params[1]));
                }catch(NumberFormatException e){
                    valid=false;
                    clearcookie=false;
                }
            }
            if(valid){
                exchange.sendResponseHeaders(200, 0);
            }else{
                if(clearcookie)exchange.getResponseHeaders().add("Set-Cookie", "key=deleted; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
                exchange.sendResponseHeaders(404, 0);
            }
            exchange.getResponseBody().close();
        });
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}