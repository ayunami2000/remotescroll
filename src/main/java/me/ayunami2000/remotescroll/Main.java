package me.ayunami2000.remotescroll;

import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Main {
    private static final int[] mbtns = new int[]{MouseEvent.BUTTON1_MASK,MouseEvent.BUTTON2_MASK,MouseEvent.BUTTON3_MASK};

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
            String[] params=exchange.getRequestURI().getQuery().split(";",4);
            boolean valid=false;
            boolean clearcookie=true;
            int mode=0;
            if(params.length>=1&&params[0].equals(finalKey)){
                if(params.length==3&&params[1].matches("-?\\d+")&&params[2].matches("-?\\d+")){
                    //switch to mouse move mode
                    mode=1;
                    valid=true;
                }else if(params.length==2&&params[1].matches("-?\\d+")){
                    mode=0;
                    valid=true;
                }else if(params.length==4&&params[1].matches("\\d+")&&params[2].matches("-?\\d+")&&params[3].matches("-?\\d+")){
                    mode=2;
                    valid=true;
                }
            }
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
                    if(mode==0) {
                        robot.mouseWheel(Integer.parseInt(params[1]));
                    }else if(mode==1) {
                        Point mpos=MouseInfo.getPointerInfo().getLocation();
                        Dimension scrsz=Toolkit.getDefaultToolkit().getScreenSize();
                        robot.mouseMove(Math.min(scrsz.width,Math.max(0,mpos.x-Integer.parseInt(params[1]))),Math.min(scrsz.height,Math.max(0,mpos.y-Integer.parseInt(params[2]))));
                    }else if(mode==2) {
                        //Point mpos=MouseInfo.getPointerInfo().getLocation();
                        //robot.mouseMove(mpos.x+Integer.parseInt(params[2]),mpos.y+Integer.parseInt(params[3]));
                        int mbtnraw=Math.min(mbtns.length-1,Integer.parseInt(params[1]));
                        int mbtn=mbtns[mbtnraw];
                        robot.mousePress(mbtn);
                        robot.mouseRelease(mbtn);
                    }
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