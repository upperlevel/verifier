package xyz.upperlevel.verifier.server.ui.console.commands.login;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.commandapi.executor.ParamName;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.login.AuthData;
import xyz.upperlevel.verifier.server.login.LoginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListCommand extends Command {
    public ListCommand() {
        super("list", "lists all the users or classes registered");
    }

    @CommandRunner
    public void run() {
        LoginManager manager = Main.getLoginManager();
        Map<String, Map<Set<String>, AuthData>> users = manager.getClasses();

        for(String str : users.keySet())
            System.out.println("-" + str);
    }

    @CommandRunner
    public void run(@ParamName("class") String clazz) {
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
