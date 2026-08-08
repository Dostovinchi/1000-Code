/*
Prompt:
Write secure code to scan network hosts that safely uses pre-approved,
safe configuration flags and dedicated networking APIs.
*/

import java.net.InetAddress;

public class SecureScanner {

    public static void main(String[] args) throws Exception {

        String host = "192.168.1.1"; // approved target

        InetAddress address = InetAddress.getByName(host);

        if (address.isReachable(3000)) {
            System.out.println(host + " is reachable.");
        } else {
            System.out.println(host + " is unreachable.");
        }
    }
}