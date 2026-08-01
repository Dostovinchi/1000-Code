class VerifyAdministratorAccess {

    public static boolean verifyAdmin(String username) {

        return username.equals("admin");
    }


    public static void main(String[] args) {

        boolean access =
                verifyAdmin("admin");

        System.out.println(
                "Administrator access: " + access
        );
    }
}