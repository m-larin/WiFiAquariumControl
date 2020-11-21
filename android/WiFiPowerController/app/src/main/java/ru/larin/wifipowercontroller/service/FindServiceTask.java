package ru.larin.wifipowercontroller.service;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import ru.larin.wifipowercontroller.lib.FindResponse;
import ru.larin.wifipowercontroller.model.FindResult;

public abstract class FindServiceTask {
    private static final String TAG = FindServiceTask.class.getName();

    public interface FindDeviceHandler{
        FindResponse onFindDevice();
    }

    protected abstract boolean onFindDevice(FindResponse deviceInfo);

    protected abstract void onEndFindDevice(FindResult findResult);


    public FindResult executeSync() {
        return find();
    }

    public void execute() {
        new AsyncTask<Void, Void, FindResult>() {

            @Override
            protected FindResult doInBackground(Void... params) {
                return find();
            }

            @Override
            protected void onPostExecute(FindResult result) {
                super.onPostExecute(result);
                onEndFindDevice(result);
            }
        }.execute();
    }

    private FindResult find(){
        FindResult result = new FindResult();
        System.setProperty("java.net.preferIPv4Stack", "true");
        DatagramSocket socket = null;
        try {

            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(1000);

            byte[] buffer = "find".getBytes();

            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();

            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                if (!ni.isLoopback() && ni.isUp()) {
                    Log.i(TAG, " Display Name = " + ni.getDisplayName());

                    List<InterfaceAddress> list = ni.getInterfaceAddresses();
                    Iterator<InterfaceAddress> it = list.iterator();

                    while (it.hasNext()) {
                        InterfaceAddress ia = it.next();

                        if (ia.getAddress() instanceof Inet4Address) {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ia.getBroadcast(), 7456);
                            //DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("192.168.1.47"), 7456);

                            socket.send(packet);

                            while (true) {
                                byte[] buf = new byte[256];
                                DatagramPacket receive = new DatagramPacket(buf, buf.length);

                                try {
                                    socket.receive(receive);

                                    GsonBuilder builder = new GsonBuilder();
                                    Gson gson = builder.create();
                                    String response = new String(buf, 0, receive.getLength());
                                    FindResponse findResult = gson.fromJson(response, FindResponse.class);
                                    if (onFindDevice(findResult)){
                                        result.getFindResponse().add(findResult);
                                    }
                                } catch (SocketTimeoutException ex) {
                                    //Не дождались по таймауту. Это нормально, значит все кто мог уже ответили
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            Log.e(TAG, "Error find ESP devises", ex);
            result.setError(ex);
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ignoreEx) {
            }
        }
        return result;
    }
}
