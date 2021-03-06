package net.starkadder.rfbplayer;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class RFBWebserver implements Runnable {

    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    static final int PORT = 8080;
    static final boolean verbose = false;
    private Socket connect;
    private MediaPlayer player;


    public RFBWebserver(MediaPlayer p) {
        player = p;
    }
    public RFBWebserver(Socket c) {
        connect = c;
    }

    public static void start() {
        try {
            ServerSocket serverConnect = new ServerSocket(PORT);
            Log.i("RFBPlayer", "Listening for connections on port: " + PORT );
            // we listen until user halts server execution
            while (true) {
                RFBWebserver myServer = new RFBWebserver(serverConnect.accept());
                if (verbose) {
                    System.out.println("Connecton opened. (" + new Date() + ")");
                }
                // create dedicated thread to manage the client connection
                Thread thread = new Thread(myServer);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Server Connection error : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // we manage our particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();
            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            // we get file requested
            fileRequested = parse.nextToken().toLowerCase();

                if (verbose) {
                    System.out.println("501 Not Implemented : " + method + " method.");
                }

                String html = HOMEPAGE;
                byte[] bytes = html.getBytes();
                String contentMimeType = "text/html";

                // we send HTTP Headers with data to client
                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMimeType);
                out.println("Content-length: " + bytes.length);
                out.println(); // blank line between headers and content, very important !
                out.flush(); // flush character output stream buffer
                dataOut.write(bytes, 0, bytes.length);
                dataOut.flush();

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }


    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }


    static final String HOMEPAGE = "<html><body>i love baby reuben</body>/html>";

}