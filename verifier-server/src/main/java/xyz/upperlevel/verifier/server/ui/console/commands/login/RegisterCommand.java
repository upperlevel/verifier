package xyz.upperlevel.verifier.server.ui.console.commands.login;

import xyz.upperlevel.commandapi.commands.Command;
import xyz.upperlevel.commandapi.executor.CommandRunner;
import xyz.upperlevel.commandapi.executor.ParamName;
import xyz.upperlevel.verifier.packetlib.utils.ByteSecurityUtil;
import xyz.upperlevel.verifier.server.Main;
import xyz.upperlevel.verifier.server.login.LoginManager;

import static xyz.upperlevel.verifier.packetlib.utils.ByteSecurityUtil.zero;

public class RegisterCommand extends Command {
    public RegisterCommand() {
        super("register", "registers a user");
        addAlias("reg");
    }

    @CommandRunner
    public void run(@ParamName("class") String clazz, @ParamName("username") String[] username_raw) {
        String username = String.join(" ", username_raw);
        char[] pssw = ByteSecurityUtil.askPassword();
        if(pssw == null) {
            System.out.println("Cannot register user");
            return;
        }
        LoginManager manager = Main.getLoginManager();
        if(manager.register(clazz, username, String.valueOf(pssw))) {
            System.out.println("User successfully registered, use \"login save\" to save the current changes");
        } else {
            System.out.println("Cannot register user: one with the same class and username already exists!");
        }
        zero(pssw);
    }
}
