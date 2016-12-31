package xyz.upperlevel.verifier.server.login;

import lombok.Getter;
import lombok.Setter;
import xyz.upperlevel.verifier.server.ClientHandler;

import java.util.*;

public class AuthData {

    @Getter
    private final Set<String> username;
    @Getter
    private final String clazz;
    @Getter
    private char[] password;

    @Getter
    @Setter
    private ClientHandler logged = null;

    public AuthData(String clazz, String username, char[] password) {
        this.clazz = clazz;
        this.username = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(username.toLowerCase().split(" "))));
        this.password = password;
    }

    public void setPassword(char[] password) {
        System.out.println("[WARNING]changed password of \"" + username + "\"!");
        this.password = password;
    }

    public boolean isLogged() {
        return logged != null;
    }

    public int compareTo(AuthData b) {
        return compare(this, b);
    }

    public static int compare(AuthData a, AuthData b) {
        String c1 = a.clazz;
        String c2 = b.clazz;
        if(!Objects.equals(c1, c2))
            return c1.compareToIgnoreCase(c2);
        else
            return compareUsername(a.getUsername(), b.getUsername());
    }

    private static int compareUsername(Set<String> a, Set<String> b) {
        Iterator<String> ia = a.iterator(), ib = b.iterator();
        while(ia.hasNext() && ib.hasNext()) {
            int cmp = ia.next().compareTo(ib.next());
            if(cmp != 0)
                return cmp;
        }
        if(ia.hasNext())
            return 1;
        else if(ib.hasNext())
            return -1;
        else return 0;
    }
}
