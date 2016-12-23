package me.upperlevel.verifier.conn;

import me.upperlevel.verifier.packetlib.MessageHandler;

public class ServerListener {

    @MessageHandler
    public void onString(ClientHandler sender, String str) {
        System.out.println("received \"" + str + "\" from " + sender);
    }
}
