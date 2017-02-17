package com.snoopy.jpos;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOServer;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.q2.iso.QServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * Created by hnair20160706 on 2017/2/16.
 */
public class ISO8583Test {
    public static void main(String[] args) throws IOException, ISOException {
//        ASCIIChannel asciiChannel = new ASCIIChannel();
//        asciiChannel.setHost("127.0.0.1", 60000);
//        asciiChannel.connect();
//        InetSocketAddress address = new InetSocketAddress("127.0.0.1",60001);
//        ServerSocket serverSocket = new ServerSocket();
//        serverSocket.bind(address);
//        asciiChannel.accept(serverSocket);
//        asciiChannel.receive();
//        asciiChannel.accept();
//        ISOServer isoServer = new ISOServer(60000,asciiChannel,null);
//        isoServer.run();
//            String hexStr ="30323030f23e44c1b8e09818000000001000008131363632303035383531313131373036343130303030303030303030303030313030303030353130303934313130303137313130303934313130303531303235313230343034373031313032313030303630383030303130303035303830303031303030353336393535383939303030303030303030303030313d303530393536373839303132333435363039393939393535383939303030303030303030303030313d313536313536303030303030303030303030303030333937363939393233363030303030323037303030303030303030303030303030303030303030303d3030303030303030303030303d303030303030303030313731313046343131323334354634323031323334353637383931324634337a68616e676a69616e67736f6674776172657061726b202020202020202020202020202020313536796bec80728a8a5a32303030303030303030303030303030303237303030302d303030303330303030303030303030303031313030353037363031303030303030303030303030303930303330202020204355503435342020435550414d30303130303130313130303030303030303036d5c4b4f3c7c53031313133383131313131313131303830303034303030303034333531365332323030303030343530303034303030302020203030303030303030303030303030303030303032433839344235440a5b393a35303a31353a3831303430375d5b3035313520303531355d0a";
            String hexStr ="";
//        serverSocket.bind(address);
//        serverSocket.accept();
//        QServer qServer = new QServer();
//        qServer.setPort(60000);
//        qServer.start();
        GenericPackager genericPackager = new GenericPackager("snoopy-jpos/src/main/resources/iso8583/iso8583snoopyascii.xml");
        ISOMsg isoMsg = new ISOMsg();
        genericPackager.unpack(isoMsg,parseTo8583bytes(hexStr));

    }

    private static byte[] parseTo8583bytes(String str) {
        String value = null;
        byte[] retByte = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i = i + 2) {
            value = str.substring(i, i + 2);
            retByte[i / 2] = (byte) Integer.parseInt(value, 16);
        }
        return retByte;
    }
}
