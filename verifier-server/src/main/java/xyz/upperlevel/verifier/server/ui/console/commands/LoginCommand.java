package xyz.upperlevel.verifier.server.ui.console.commands;

import xyz.upperlevel.commandapi.CommandRunner;
import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.commands.NodeCommand;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.login.AuthData;
import xyz.upperlevel.verifier.server.login.LoginManager;

import javax.swing.*;
import java.io.Console;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.upperlevel.verifier.packetlib.utils.ByteSecurityUtil.zero;

public class LoginCommand extends NodeCommand {
    public LoginCommand() {
        super("login");

        registerSub(new ListCommand());
        registerSub(new PasswordCommand());
        registerSub(new ReloadCommand());
        registerSub(new RegisterCommand());
        registerSub(new SaveCommand());
        registerSub(new BackupCommand());
    }


    public static class ListCommand extends Command {
        public ListCommand() {
            super("list");
        }

        @CommandRunner
        public void run() {
            LoginManager manager = Main.getLoginManager();
            Map<String, Map<Set<String>, AuthData>> users = manager.getClasses();

            for(String str : users.keySet())
                System.out.println("-" + str);
        }

        @CommandRunner
        public void run(String clazz) {
            LoginManager manager = Main.getLoginManager();
            List<AuthData> users;

            long stamp = manager.getLock().readLock();
            try {
                users = new ArrayList<>(manager.getClazz(clazz));
            } finally {
                manager.getLock().unlockRead(stamp);
            }

            users.sort(AuthData::compare);
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
        public void run(String clazz, String[] username) {
            LoginManager manager = Main.getLoginManager();
            AuthData data = manager.get(
                    clazz,
                    Arrays.stream(username)
                            .map(String::toLowerCase)
                            .collect(Collectors.toSet())
            );
            if(data == null) {
                System.out.println("Cannot find \"" + username + "\" in class \"" + clazz + "\"");
                return;
            }
            char[] pssw = askPassword();
            if(pssw != null) {
                zero(data.getPassword());
                data.setPassword(pssw);
                System.out.println("New password set, use \"login save\" to save the current changes");
            } else
                System.out.println("Password NOT set");
        }
    }

    public static class ReloadCommand extends Command {
        public ReloadCommand() {
            super("reload");
        }

        @CommandRunner
        public void run() {
            LoginManager manager = Main.getLoginManager();
            manager.reload();
        }
    }

    public static class RegisterCommand extends Command {
        public RegisterCommand() {
            super("register");
            addAlias("reg");
        }

        @CommandRunner
        public void run(String clazz, String[] username_raw) {
            String username = String.join(" ", username_raw);
            char[] pssw = askPassword();
            if(pssw == null) {
                System.out.println("Cannot register user");
                return;
            }
            LoginManager manager = Main.getLoginManager();
            if(manager.register(clazz, username, pssw)) {
                System.out.println("User successfully registered, use \"login save\" to save the current changes");
            } else {
                System.out.println("Cannot register user: one with the same class and username already exists!");
            }
        }
    }

    public static class SaveCommand extends Command {
        public SaveCommand() {
            super("save");
        }

        @CommandRunner
        public void run() {
            try {
                Main.getLoginManager().saveToFiles();
            } catch (IOException e) {
                System.err.println("Cannot save changes!");
                e.printStackTrace();
            }
        }
    }

    public static class BackupCommand extends Command {
        public BackupCommand() {
            super("backup");
        }

        @CommandRunner
        public void run() {
            try {
                Main.getLoginManager().backupFiles();
            } catch (IOException e) {
                System.err.println("Cannot create backup entry!");
                e.printStackTrace();
            }
        }
    }

    private static char[] askPassword() {
        Console console = System.console();
        if(console == null) {
            System.out.println("Console not found, using java.swing");
            JPasswordField pf = new JPasswordField();
            int okCxl = JOptionPane.showConfirmDialog(null, pf, "Enter Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (okCxl != JOptionPane.OK_OPTION)
                return null;
            return pf.getPassword();
        } else {
            char[] password = console.readPassword("Enter Password:");
            char[] pssw2 = console.readPassword("Repeat Password:");

            if (!Arrays.equals(password, pssw2)) {
                System.out.println("The two password do NOT match!");
                return null;
            }

            zero(pssw2);
            return password;
        }
    }
}
