package xyz.upperlevel.verifier.server.ui.console;

import xyz.upperlevel.commandapi.CommandSender;
import xyz.upperlevel.messager.MessageType;

public class ConsoleSender implements CommandSender{
    public static final ConsoleSender INSTANCE = new ConsoleSender();

    @Override
    public void sendMessage(MessageType type, String s) {
        if(type == MessageType.ERROR)
            System.err.println(s);
        else
            System.out.println(s);
    }
}
