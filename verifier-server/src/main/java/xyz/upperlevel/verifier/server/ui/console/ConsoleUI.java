package xyz.upperlevel.verifier.server.ui.console;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import xyz.upperlevel.commandapi.CommandHandler;
import xyz.upperlevel.verifier.packetlib.simple.SimpleServer.SimpleServerOptions;
import xyz.upperlevel.verifier.proto.ErrorType;
import xyz.upperlevel.verifier.server.ui.UI;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class ConsoleUI implements UI {
    public static final Scanner in = new Scanner(new InputStreamReader(System.in));

    public final CommandHandler commandHandler = new CommandHandler().registerPackageRelative("commands");

    @Parameter(names = {"-p", "--port"}, validateWith = PortValidator.class)
    private int port = -1;

    @Parameter(names = {"-b", "-bossThreadNumber"}, description = "the number of threads to assign at the boss thread group")
    private int bossThreadNumer = SimpleServerOptions.DEFAULT.bossThreadNumber;

    @Parameter(names = {"-w", "-workerThreadNumber"}, description = "the number of threads to assign at the worker thread group")
    private int workerThreadNumber =  SimpleServerOptions.DEFAULT.workerThreadNumber;

    public static boolean running = false;


    @Override
    public void init(String[] args) {
        new JCommander(this, args);
    }

    @Override
    public void askConnInfo(BiConsumer<Integer, SimpleServerOptions> callback) {
        while (!checkPort(port)) {
            System.out.print("port:");
            port = in.nextInt();
            in.nextLine();
        }
        callback.accept(
                port,
                SimpleServerOptions.builder()
                        .bossThreadNumber(bossThreadNumer)
                        .workerThreadNumber(workerThreadNumber)
                        .build()
        );
    }

    @Override
    public void error(ErrorType type, String message) {
        System.err.println("[ERROR]" + type.name() + "->" + message);
    }

    @Override
    public void start() {
        running = true;
        while(running)
            try {
                if(!commandHandler.execute(ConsoleSender.INSTANCE, Arrays.asList(in.nextLine().split(" "))))
                    System.out.println("Command not found, don't type \"help\" because it doesn't work");
            } catch (Exception e) {
                e.printStackTrace();
            }
        System.out.println("Closing console listener...");
    }

    public static class PortValidator implements IParameterValidator {
        @Override public void validate(String name, String value) throws ParameterException {
            int val = Integer.parseInt(value);
            if(!checkPort(val))
                throw new ParameterException("The port must be between 0 and 65535");
        }
    }

    private static boolean checkPort(int port) {
        return (port >= 0 && port <= 65535);
    }
}
