package ru.larin.wifipowercontroller.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import ru.larin.wifipowercontroller.lib.*;

import static java.lang.System.out;

public class TestServer {
    private static final String IP = "0.0.0.0";

    private Status status = new Status();
    private Settings settings = new Settings();


    public static void main(String[] args){
        try {
            TestServer testServer = new TestServer();
            testServer.start();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void start() throws Exception {
        status.setChannels(new HashMap<String, String>());
        status.setLighting(65000);
        status.getChannels().put("0", "off");
        status.getChannels().put("1", "off");
        status.getChannels().put("2", "off");
        status.getChannels().put("3", "off");
        status.setTime(new WpcTime(2018, 05, 18, 18, 5, 26));

        String clientSentence;
        ServerSocket welcomeSocket = new ServerSocket(7456, 0, Inet4Address.getByName(IP));
        out.println("Start server");
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            InputStream inFromClient = connectionSocket.getInputStream();
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            byte[] buf = new byte[1024];
            int read = inFromClient.read(buf);
            clientSentence = new String(buf, 0, read);

            out.println("Received: " + clientSentence);

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Request request = gson.fromJson(clientSentence, Request.class);

            String result = null;
            if (request.getCommand().equals("settings-get")) {
                result =  builder.create().toJson(settings);
            }else if(request.getCommand().equals("settings-set")){
                settings = request.getSettings();
                result = getStatus();
            }else if(request.getCommand().equals("on") || request.getCommand().equals("off")){
                status.getChannels().put(request.getChannel().toString(), request.getCommand());
                result = getStatus();
            }else{
                result = getStatus();
            }

            out.println("Send: " + result);
            outToClient.writeBytes(result);
            outToClient.close();
        }
    }

    private String getStatus(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        status.getTime().setYear(calendar.get(Calendar.YEAR));
        status.getTime().setMonth(calendar.get(Calendar.MONTH));
        status.getTime().setDate(calendar.get(Calendar.DATE));
        status.getTime().setHour(calendar.get(Calendar.HOUR_OF_DAY));
        status.getTime().setMin(calendar.get(Calendar.MINUTE));
        status.getTime().setSec(calendar.get(Calendar.SECOND));

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(status);
    }
}
