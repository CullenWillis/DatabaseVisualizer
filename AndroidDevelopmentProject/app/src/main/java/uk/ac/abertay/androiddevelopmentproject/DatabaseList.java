package uk.ac.abertay.androiddevelopmentproject;

public class DatabaseList {

    String server;
    String database;
    String username;
    String password;

    public DatabaseList(String server, String database, String username, String password) {

        this.server = server;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public String getServer() { return server;}

    public void setServer(String server) { this.server = server; }

    public String getDatabase() { return database; }

    public void setDatabase(String database) { this.database = database; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }
}
