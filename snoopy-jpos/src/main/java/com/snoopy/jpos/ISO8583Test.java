package com.snoopy.jpos;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOServer;
import org.jpos.iso.channel.ASCIIChannel;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by hnair20160706 on 2017/2/16.
 */
public class ISO8583Test {
    public static void main(String[] args) throws IOException, ISOException {
//        ASCIIChannel asciiChannel = new ASCIIChannel();
//        ISOServer isoServer = new ISOServer(60000,asciiChannel,null);
//        isoServer.run();
        ServerSocket serverSocket = new ServerSocket(60000);
        serverSocket.accept();


    }
}
