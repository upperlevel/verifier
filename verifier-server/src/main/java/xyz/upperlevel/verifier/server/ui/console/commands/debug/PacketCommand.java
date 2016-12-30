package xyz.upperlevel.verifier.server.ui.console.commands.debug;

import xyz.upperlevel.commandapi.CommandRunner;
import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.commands.NodeCommand;
import xyz.upperlevel.verifier.packetlib.PacketHandler;
import xyz.upperlevel.verifier.packetlib.PacketManager;
import xyz.upperlevel.verifier.server.Main;

public class PacketCommand extends NodeCommand {
    public PacketCommand() {
        super("packet");
        registerSub(new ListCommand());
    }

    public static class ListCommand extends Command {

        public ListCommand() {
            super("list");
        }

        @CommandRunner
        public void run() {
            PacketManager manager = Main.getServer().getPacketManager();
            for(PacketHandler<?> handler :manager.id_mapped)
                System.out.println(">" + handler.getId() + "->\"" + handler.getName() + "\" (" + handler.getHandled().getName() + ")");
        }
    }
}
