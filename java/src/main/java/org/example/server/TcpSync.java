package org.example.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;


final public class TcpSync {

    private TcpSync(){}

    private static final Logger logger = LogManager.getLogger(TcpSync.class);

    public static void runSyncTCPServer(){
        try(final var serverSocket = new ServerSocket();
            final var executor = Executors.newVirtualThreadPerTaskExecutor()){
            serverSocket.bind(new InetSocketAddress(Config.HOST, Config.PORT));
            logger.info("Started a virtual thread TCP server on {} {}", Config.HOST, Config.PORT);
            while(true){
                final var clientSocket = serverSocket.accept();
                executor.execute(()->{
                    try{writeToSocket(clientSocket, readFromSocket(clientSocket));}
                    catch(final IOException e){logger.error(e);}
                    finally {
                        try {clientSocket.close();}
                        catch (IOException e) {logger.error("Failed to close client socket: ", e);}
                    }});}
        }catch(final IOException e){logger.fatal("Server error: ", e);}
    }

    private static byte[] readFromSocket(final Socket clientSocket) throws IOException {
        final var input = clientSocket.getInputStream();
        final var buffer = new byte[512];
        final var byteArrayOutputStream = new ByteArrayOutputStream();
        int bytesRead;
        if((bytesRead =input.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            logger.info("Read: {}", new String(buffer));
        }return byteArrayOutputStream.toByteArray();
    }

    private static void writeToSocket(final Socket clientSocket, final byte[] message) throws IOException {
        if(message != null && message.length > 0){
        final var output = clientSocket.getOutputStream();
        output.write(message);
        output.flush();
        logger.info("Written: {}", new String(message));
        }else logger.info("Nothing to write");
    }
}
