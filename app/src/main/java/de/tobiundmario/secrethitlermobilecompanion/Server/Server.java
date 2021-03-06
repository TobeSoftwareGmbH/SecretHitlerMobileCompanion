package de.tobiundmario.secrethitlermobilecompanion.Server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import de.tobiundmario.secrethitlermobilecompanion.ExceptionHandler;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.GameManager;
import de.tobiundmario.secrethitlermobilecompanion.SHClasses.GameManager.JSONManager;
import fi.iki.elonen.NanoHTTPD;

import static android.content.Context.WIFI_SERVICE;

public class Server extends NanoHTTPD {
    private Context c;
    private int port;

    public static final int MOBILE_DATA = 3, WIFI = 2, NOT_CONNECTED = 1;

    private HashSet<String> clientIPs;

    public Server(int port, Context c) {
        super(port);
        this.c = c;
        this.port = port;

        this.clientIPs = new HashSet<String>();
        JSONManager.setClientIPsSet(this.clientIPs);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.v(" Server request URI", session.getUri());
        Log.v("Server request method", session.getMethod().toString());
        Log.v("Remote address", session.getRemoteIpAddress());

        String clientIP = session.getRemoteIpAddress();

        if(!clientIPs.contains(clientIP)) {
            clientIPs.add(clientIP);
        }

        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> k : session.getHeaders().entrySet()) {
            sb.append("  ").append(k.getKey()).append(": ").append(k.getValue()).append("\n");
        }

        Log.v("Server request header", "\n" + sb.toString());

        String uri = session.getUri();

        return serveData(uri, clientIP);
    }

    private Response serveData(String uri, String clientIP) {
        switch (uri) {
            case "/index.html":
            case "/":
                return newFixedLengthResponse(Response.Status.ACCEPTED, "text/html", getFile("index.html"));
            case "/getGameJSON":
                if (GameManager.isGameStarted()) {
                    String response = JSONManager.getCompleteGameJSON(clientIP);
                    Log.v("/getGameJSON: JSON: ", response);
                    return newFixedLengthResponse(Response.Status.OK, "application/json", response);
                }
                return newFixedLengthResponse(Response.Status.SERVICE_UNAVAILABLE, "application/json", "");
            case "/getGameChangesJSON":
                if (GameManager.isGameStarted()) {
                    String response = JSONManager.getGameChangesJSON(clientIP);
                    Log.v("/getGameChanges: JSON: ", response);
                    return newFixedLengthResponse(Response.Status.OK, "application/json", response);
                }
                return newFixedLengthResponse(Response.Status.SERVICE_UNAVAILABLE, "application/json", "");
        }

        if(uri.endsWith(".js") || uri.endsWith(".css")) {
            return newFixedLengthResponse(Response.Status.ACCEPTED, getMimeTypeForFile(uri), getFile(uri.substring(1)));
        }

        if(uri.contains("/fonts")) {
            return serveFont(uri);
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", getFile("404.html"));
    }

    private Response serveFont(String uri) {
        try {
            return newFixedLengthResponse(Response.Status.ACCEPTED, getMimeTypeForFile(uri), c.getAssets().open(uri.substring(1)), -1);
        } catch (IOException e) {
            ExceptionHandler.showErrorSnackbar(e, "Server.serveFont() (serving a font file)");
        }
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "");
    }

    public String getFile(String fileName) {
        StringBuilder fileData = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(c.getAssets().open(fileName)));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                fileData.append(mLine).append("\n");
            }
        } catch (IOException e) {
            ExceptionHandler.showErrorSnackbar(e, "Server.getFile() (while reading the file)");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    ExceptionHandler.showErrorSnackbar(e, "Server.getFile() (while closing the BufferedReader)");
                }
            }
        }
        return fileData.toString();
    }


    public void startServer() {
        try {
            start();
        } catch (IOException e) {
            ExceptionHandler.showErrorSnackbar(e, "Server.startServer()");
        }
    }

    public String getURL() {
        WifiManager wifiManager = (WifiManager) c.getApplicationContext().getSystemService(WIFI_SERVICE);

        if(isUsingHotspot(wifiManager)) return "http://" + getHotspotIPAddress() + ":" + port;

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format(Locale.GERMANY, "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));


        return "http://" + formatedIpAddress + ":" + port;
    }


    public static boolean isUsingHotspot(WifiManager wifiManager) {
        int actualState = 0;
        try {
            java.lang.reflect.Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            actualState = (Integer) method.invoke(wifiManager, (Object[]) null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            ExceptionHandler.showErrorSnackbar(e, "Server.isUsingHotspot()");
        }
        return actualState == 13; //AP_STATE_ENABLED = 13;
    }

    private String getHotspotIPAddress() {
        String deviceIpAddress = "###.###.###.###";

        try {
            for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements();) {
                NetworkInterface networkInterface = enumeration.nextElement();

                for (Enumeration<InetAddress> enumerationIpAddr = networkInterface.getInetAddresses(); enumerationIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumerationIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4)
                    {
                        deviceIpAddress = inetAddress.getHostAddress();

                        Log.e("Server", "deviceIpAddress: " + deviceIpAddress);
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("Server", "SocketException:" + e.getMessage());
            ExceptionHandler.showErrorSnackbar(e, "Server.getHotspotIPAddress()");
        }

        return deviceIpAddress;
    }

    public static int getConnectionType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return  WIFI;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                return MOBILE_DATA;
            }
        }

        return NOT_CONNECTED;
    }

}
