 package xyz.upperlevel.verifier.server.login;

 import com.google.common.io.Files;
 import lombok.Getter;
 import xyz.upperlevel.verifier.server.ClientHandler;

 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.locks.StampedLock;
 import java.util.stream.Collectors;

 import static xyz.upperlevel.verifier.packetlib.utils.ByteSecurityUtil.zero;

 public class LoginManager {
     public static final File FOLDER = new File("users");
     public static final File BACKUP_FOLDER = new File("backups");
     public static final DateFormat BACKUP_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

     public static final String FILE_PATTERN = "[1-5]([a-i]|[A-I]).*";

     @Getter
     private StampedLock lock = new StampedLock();

     public Map<String, Map<String, AuthData>> users = new HashMap<>();

     public List<AuthData> nonMapped = new ArrayList<>(2048);

     static {
         if (!FOLDER.isDirectory() && !FOLDER.mkdirs())
             throw new IllegalStateException("Cannot create users folder!");
         if (!FOLDER.isDirectory())
             throw new IllegalStateException("users is not a fodler!");
     }


     public AuthData get(String clazz_name, String username) {
         long stamp = lock.readLock();
         try {
             Map<String, AuthData> clazz = users.get(clazz_name);
             if (clazz == null)
                 return null;
             return clazz.get(username);
         } finally {
             lock.unlockRead(stamp);
         }
     }

     public Map<String, Map<String, AuthData>> getClasses() {
         return users;
     }

     public List<AuthData> getUsers() {
         return nonMapped;
     }

     public Collection<AuthData> getClazz(String clazz_name) {
         Map<String, AuthData> res = users.get(clazz_name);
         return res != null ? res.values() : null;
     }


     public boolean register(String clazz, String userame, char[] password) {
         clazz = clazz.toLowerCase();
         long stamp = lock.writeLock();
         try {
             Map<String, AuthData> clzz = users.computeIfAbsent(clazz, (a) -> new HashMap<>());
             AuthData data = new AuthData(clazz, userame, password);
             return clzz.putIfAbsent(data.getUsername(), data) == null;
         } finally {
             lock.unlockWrite(stamp);
         }
     }

     public void reload() {
         long stamp = lock.writeLock();
         try {
             List<AuthData> oldUsers = users.values().stream()
                     .map(Map::values)
                     .flatMap(Collection::stream)
                     .collect(Collectors.toList());

             users.clear();
             registerFromFiles();

             for (AuthData data : oldUsers) {
                 if (data.isLogged()) {
                     ClientHandler client = data.getLogged();
                     long st2 = client.authLock.writeLock();
                     try {
                         AuthData newData = get(data.getClazz(), data.getUsername());
                         newData.setLogged(client);
                         client.data = newData;
                         data.setLogged(null);
                     } finally {
                         client.authLock.unlockWrite(st2);
                     }
                 }
                 zero(data.getPassword());
             }
         } finally {
             lock.unlockWrite(stamp);
         }
     }

     public void saveToFiles() throws IOException {
         long stamp = lock.readLock();
         try {
             backupFiles0(true);
             System.out.println("Init writing");
             for (Map.Entry<String, Map<String, AuthData>> entry : users.entrySet()) {
                 File outFile = new File(FOLDER, entry.getKey() + ".txt");
                 if (!outFile.createNewFile())
                     throw new IllegalStateException("Cannot create file \"" + entry.getKey() + ".txt\"");
                 System.out.println("Init writing to " + outFile.getName());
                 try (FileWriter writer = new FileWriter(outFile)) {
                     for (AuthData data : entry.getValue().values()) {
                         writer.write(data.getUsername());
                         writer.write(" ");
                         writer.write(data.getPassword());
                         writer.write("\r\n");
                     }
                     writer.flush();
                 }
                 System.out.println("End single file writing");
             }
         } catch (Exception e) {
             try {
                 bringUp0();
             } catch (IOException e1) {
                 throw new IllegalStateException("Cannot restore from backupFiles!", e1);
             }

             throw new IllegalStateException(e);
         } finally {
             lock.unlockRead(stamp);
         }
         System.out.println("Save successfully terminated without errors");
     }

     public void backupFiles() throws IOException {
         long stamp = lock.readLock();
         try {
             backupFiles0(false);
         } finally {
             lock.unlockRead(stamp);
         }
     }


     private void backupFiles0(boolean move) throws IOException {
         File backupDir = new File(BACKUP_FOLDER, BACKUP_FORMAT.format(new Date()));
         if (!backupDir.mkdirs())
             throw new IllegalStateException("Cannot create backupFiles folder!");

         File[] files = FOLDER.listFiles((dir, name) -> name.matches(FILE_PATTERN));

         if (files == null)
             throw new IllegalStateException("users is not a directory or IO exception");

         System.out.println("Backing up current users");

         for (File file : files) {
             if (move) {
                 Files.move(
                         file,
                         new File(backupDir, file.getName())
                 );
             } else {
                 Files.copy(
                         file,
                         new File(backupDir, file.getName())
                 );
             }
         }
         System.out.println("Finish backupFiles");
     }

     private void bringUp0() throws IOException {
         List<File> files = Arrays.asList(Objects.requireNonNull(BACKUP_FOLDER.listFiles(), "Cannot read backupFiles folders"));
         File last = Collections.max(files, Comparator.comparing(File::getName));
         bringUp0(last);
     }

     private void bringUp0(File folder) throws IOException {
         if(!folder.isDirectory())
             throw new IllegalArgumentException("Cannot get backupFiles from file!");
         System.out.println("Init restoring backupFiles from " + folder.getName());
         System.out.println("Clearing folder");
         deleteContents(FOLDER);

         File[] files = folder.listFiles();
         if(files == null)
             throw new IllegalStateException("Cannot get files from backupFiles dir");
         for(File file : files) {
             Files.copy(
                     file,
                     new File(FOLDER, file.getName())
             );
         }
         System.out.println("Finish restoring from backupFiles");
     }

     private static void deleteContents(File folder) {
         File[] files = folder.listFiles();
         if(files!=null) { //some JVMs return null for empty dirs
             for(File f: files) {
                 if(f.isDirectory()) {
                     deleteContents(f);
                 } else {
                     f.delete();
                 }
             }
         }
     }


     public void registerFromFiles() {
         File[] files = FOLDER.listFiles((dir, name) -> name.matches(FILE_PATTERN));

         if (files == null)
             throw new IllegalStateException("users is not a directory or IO exception");

         System.out.println("Loading users from files (files:" + files.length + ")");

         for (File file : files) {
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
         try(FileReader r = new FileReader(file)) {
             BufferedReader reader = new BufferedReader(r);
             String line;
             Map<String, AuthData> scholars = new HashMap<>(50);
             final String clazz = removeExt(file.getName()).toLowerCase();
             while ((line = reader.readLine()) != null) {
                 if ("".equals(line)) continue;
                 String[] strs = splitLast(line, ' ');
                 if (strs.length != 2) {
                     System.err.println("[WARNING] wrong formatting in file \"" + file.getName() + "\", it must be \"username password\"");
                     continue;
                 }
                 AuthData data = new AuthData(clazz, strs[0], strs[1].toCharArray());//TODO: better password managment
                 scholars.put(data.getUsername(), data);
                 nonMapped.add(data);
             }
             users.put(clazz, scholars);
         }
     }

     private String removeExt(String str) {
         final int extIndex = str.lastIndexOf('.');
         if (extIndex > 0)
             return str.substring(0, extIndex);
         return str;
     }

     private String[] splitLast(String str, char c) {
         int i = str.lastIndexOf(c);
         return new String[]{str.substring(0, i), str.substring(i + 1)};
     }
 }
