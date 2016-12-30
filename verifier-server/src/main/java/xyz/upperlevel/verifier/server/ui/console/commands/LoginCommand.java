package xyz.upperlevel.verifier.server.ui.console.commands;

import xyz.upperlevel.commandapi.CommandRunner;
import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.commands.NodeCommand;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.login.AuthData;
import xyz.upperlevel.verifier.server.login.LoginManager;

import javax.swing.*;
import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static xyz.upperlevel.verifier.packetlib.utils.ByteSecurityUtil.zero;

public class LoginCommand extends NodeCommand {
    public LoginCommand() {
        super("login");

        registerSub(new ListCommand());
        registerSub(new PasswordCommand());
    }


    public static class ListCommand extends Command {
        public ListCommand() {
            super("list");
        }

        @CommandRunner
        public void run() {
            LoginManager manager = Main.getLoginManager();
            Map<String, Map<String, AuthData>> users = manager.getClasses();

            for(String str : users.keySet())
                System.out.println("-" + str);
        }

        @CommandRunner
        public void run(String clazz) {
            LoginManager manager = Main.getLoginManager();
            List<AuthData> users = new ArrayList<>(manager.getClazz(clazz));
            users.sort((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()));
            for(AuthData data : users)
                System.out.println("-" + data.getUsername());
        }
    }

    public static class PasswordCommand extends Command {
        public PasswordCommand() {
            super("password");
            addAlias("passwd");
            addAlias("passw");
        }

        @CommandRunner
        public void run(String clazz, String... userName_raw) {
            LoginManager manager = Main.getLoginManager();
            String username = String.join(" ", userName_raw).toLowerCase();
            AuthData data = manager.get(clazz, username);
            if(data == null) {
                System.out.println("Cannot find \"" + username + "\" in class \"" + clazz + "\"");
                return;
            }
            Console console = System.console();
            if(console == null) {
                System.out.println("Console not found, using java.swing");
                JPasswordField pf = new JPasswordField();
                int okCxl = JOptionPane.showConfirmDialog(null, pf, "Enter Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (okCxl != JOptionPane.OK_OPTION) {
                    System.out.println("Password NOT set");
                    return;
                }
                zero(data.getPassword());
                data.setPassword(pf.getPassword());
            } else {
                char[] password = console.readPassword("Enter Password:");
                char[] pssw2 = console.readPassword("Repeat Password:");

                if (!Arrays.equals(password, pssw2)) {
                    System.out.println("The two password do NOT match!");
                    return;
                }

                zero(pssw2);
                zero(data.getPassword());
                data.setPassword(password);
            }
            System.out.println("New password set, use \"login save\" to save the new specs");
        }
    }
}
