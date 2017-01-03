package xyz.upperlevel.verifier.server.ui.console.commands.login;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.commandapi.executor.ParamName;
import xyz.upperlevel.verifier.packetlib.utils.ByteSecurityUtil;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.login.AuthData;
import xyz.upperlevel.verifier.server.login.LoginManager;

import java.util.Arrays;
import java.util.stream.Collectors;

import static xyz.upperlevel.verifier.packetlib.utils.ByteSecurityUtil.zero;

public class PasswordCommand extends Command {
    public PasswordCommand() {
        super("password", "changes the password for the specified user");
        addAlias("passwd");
        addAlias("passw");
    }

    @CommandRunner
    public void run(@ParamName("class") String clazz, @ParamName("username") String[] username) {
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
        char[] pssw = ByteSecurityUtil.askPassword();
        if(pssw != null) {
            zero(data.getPassword());
            data.setPassword(pssw);
            System.out.println("New password set, use \"login save\" to save the current changes");
        } else
            System.out.println("Password NOT set");
    }
}
