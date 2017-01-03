package xyz.upperlevel.verifier.server.ui.console.commands.assignment;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.assignments.AssignmentManager;
import xyz.upperlevel.verifier.server.login.AuthData;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CommittedCommand extends Command {
    public CommittedCommand() {
        super("committed", "displays a list of the users that have committed the assignment");
        addAlias("comm");
    }

    @CommandRunner
    public void run() {
        System.out.println("Checking users");
        AssignmentManager manager = Main.getAssignmentManager();
        getUserThat(
                manager::hasCurrentAssignment,
                d -> System.out.println("-" + d.getClazz() + "->" + d.getUsername())
        );
    }

    public static void getUserThat(Predicate<AuthData> pred, Consumer<AuthData> use) {
        List<AuthData> users = Main.getLoginManager().getUsers();
        users.stream()
                .filter(pred)
                .sorted(AuthData::compare)
                .forEach(use);
    }
}
