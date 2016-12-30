 package xyz.upperlevel.verifier.server.login;

 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;

 public class LoginManager {
    public static final File FOLDER = new File("users");
    public static final String FILE_PATTERN = "[1-5]([a-i]|[A-I]).*";

    public Map<String, Map<String, AuthData>> users = new HashMap<>();

    public List<AuthData> nonMapped = new ArrayList<>(2048);

    static {
        if(!FOLDER.isDirectory() && !FOLDER.mkdirs())
            throw new IllegalStateException("Cannot create users folder!");
        if(!FOLDER.isDirectory())
            throw new IllegalStateException("users is not a fodler!");
    }



    public AuthData get(String clazz_name, String username) {
        Map<String, AuthData> clazz = users.get(clazz_name);
        if (clazz == null)
            return null;
        return clazz.get(username);
    }

    public Map<String, Map<String, AuthData>> getClasses() {
        return users;
    }

    public List<AuthData> getUsers() {
        return nonMapped;
    }

    public Collection<AuthData> getClazz(String clazz_name) {
        Map<String, AuthData> res =  users.get(clazz_name);
        return res != null ? res.values() : null;
    }




    public void registerFromFiles() {
        File[] files = FOLDER.listFiles((dir, name) -> name.matches(FILE_PATTERN));

        if(files == null)
            throw new IllegalStateException("users is not a directory or IO exception");

        System.out.println("Loading users from files (files:" + files.length + ")");

        for(File file : files) {
            System.out.println("Found file:" + file.getName());
            try {
                register(file);
            } catch (IOException e) {
                System.err.println("Error reading file \"" + file.getName() + "\"");
                e.printStackTrace();
            }
        }
    }

    public void register(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        Map<String, AuthData> scholars = new HashMap<>(50);
        final String clazz = removeExt(file.getName()).toLowerCase();
        while((line = reader.readLine()) != null) {
            if("".equals(line)) continue;
            String[] strs = splitLast(line, ' ');
            if(strs.length != 2) {
                System.err.println("[WARNING] wrong formatting in file \"" + file.getName() + "\", it must be \"username password\"");
                continue;
            }
            AuthData data = new AuthData(clazz, strs[0], strs[1].toCharArray());//TODO: better password managment
            scholars.put(data.getUsername(), data);
            nonMapped.add(data);
        }
        users.put(clazz, scholars);
    }

    private String removeExt(String str) {
        final int extIndex = str.lastIndexOf('.');
        if(extIndex > 0)
            return str.substring(0, extIndex);
        return str;
    }

    private final String[] splitLast(String str, char c) {
        int i = str.lastIndexOf(c);
        return new String[]{str.substring(0, i), str.substring(i + 1)};
    }

}
