package uk.ac.abertay.androiddevelopmentproject;

import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DatabaseConnections{

    private Connection connection;
    private String server;
    private String database;
    private String username;
    private String password;
    private String url;

    boolean found = false;

    //Constructor
    public DatabaseConnections(String _server, String _database, String _username, String _password) {

        InitializeConnection(_server, _database, _username, _password);
    }

    //Initialize values
    private void InitializeConnection(String _server, String _database, String _username, String _password) {

        server = _server;
        database = _database;
        username = _username;
        password = _password;

        url = "jdbc:mysql://" + server + "/" + database;
    }

    //Open connection to database
    public boolean OpenConnection() {

        found = false;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Class.forName("com.mysql.jdbc.Driver");

            Log.i("Trying connection", url + ", " + username + ", " + password);
            connection = DriverManager.getConnection(url, username, password);
            found = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Error connection", "" + e.getMessage());
            found = false;
        }

        return found;
    }

    //Close connection
    public boolean CloseConnection() {

        found = false;

        try
        {
            connection.close();
            found = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.w("Error connection","" + e.getMessage());
            found = false;
        }

        return found;
    }

    //Get tables
    public List<String> GetTables() {
        List<String> list = new ArrayList<String>();

        //Open connection
        if (this.OpenConnection() == true)
        {
            Log.i("Connection", "Success");

            try
            {
                //Create Command
                String query = "SHOW TABLES";


                Statement st = connection.createStatement();

                ResultSet rs = st.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();

                while(rs.next())
                {
                    list.add(rs.getString(1));
                }

                //close Connection
                this.CloseConnection();
                st.close();
                rs.close();

                //return list to be displayed
                return list;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.w("Error connection","" + e.getMessage());
                return list;
            }
        }
        else
        {
            return list;
        }
    }

    //Get Columns
    public List<String> GetColumns(String table) {

        List<String> list = new ArrayList<String>();

        //Open connection
        if (this.OpenConnection() == true)
        {
            Log.i("Connection", "Success");

            try
            {
                //Create Command
                String query = "SELECT * FROM " + table + "";

                Statement st = connection.createStatement();

                ResultSet rs = st.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();

                    for(int i = 0; i < rsmd.getColumnCount(); i++)
                        list.add(rsmd.getColumnName(i + 1));

                for (String s : list) {

                    Log.i("List Columns", s);
                }

                //close Connection
                this.CloseConnection();
                st.close();
                rs.close();

                //return list to be displayed
                return list;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.w("Error connection","" + e.getMessage());
                return list;
            }
        }
        else
        {
            return list;
        }
    }

    //Get Column Contents
    public List<String> GetColumnContents(String table, String column) {

        List<String> list = new ArrayList<String>();

        //Open connection
        if (this.OpenConnection() == true)
        {
            Log.i("Connection", "Success");

            try
            {
                //Create Command
                String query = "SELECT " + column + " FROM " + table + "";

                Statement st = connection.createStatement();

                ResultSet rs = st.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();

                while(rs.next())
                {
                    for(int i = 0; i < rsmd.getColumnCount(); i++) {

                        if(!list.contains(rs.getString(i + 1)))
                            list.add(rs.getString(i + 1));
                    }
                }

                //close Connection
                this.CloseConnection();
                st.close();
                rs.close();

                //return list to be displayed
                return list;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.w("Error connection","" + e.getMessage());
                return list;
            }
        }
        else
        {
            return list;
        }
    }

    public List<String> GetData(String table, String toGather, String toCompare, String where){


        List<String> list = new ArrayList<String>();

        //Open connection
        if (this.OpenConnection() == true)
        {
            Log.i("Connection", "Success");

            try
            {
                //Create Command
                String query = "SELECT " + toGather + " FROM " + table + " WHERE " + toCompare + " = '" + where + "'";
                Log.i("Statement" , query);
                Statement st = connection.createStatement();

                ResultSet rs = st.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();

                while(rs.next())
                {
                    for(int i = 0; i < rsmd.getColumnCount(); i++) {

                        if(!list.contains(rs.getString(i + 1)))
                            list.add(rs.getString(i + 1));
                    }
                }

                //close Connection
                this.CloseConnection();
                st.close();
                rs.close();

                //return list to be displayed
                return list;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.w("Error connection","" + e.getMessage());
                return list;
            }
        }
        else
        {
            return list;
        }
    }

    public List<DatabaseSales> GetAllData(String table, String item1, String item2, String item3){


        List<DatabaseSales> list = new ArrayList<DatabaseSales>();

        //Open connection
        if (this.OpenConnection() == true)
        {
            Log.i("Connection", "Success");

            try
            {
                //Create Command
                String query = "SELECT " + item3 + ", " + item1 + ", " + item2 + " FROM " + table + " ORDER BY " + item3 + " ASC, " + item2 + " ASC";
                Log.i("Statement" , query);
                Statement st = connection.createStatement();

                ResultSet rs = st.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();

                while(rs.next())
                {
                    for(int i = 0; i < rsmd.getColumnCount(); i++) {

                        if(!list.contains(rs.getString(i + 1)))
                        {
                            String name = rs.getString(i + 1);
                            int sales = Integer.valueOf(rs.getString(i + 2));
                            String month = rs.getString(i + 3);

                            Log.i("Adding to list", name + ", " + sales + ", " + month);

                            DatabaseSales newData = new DatabaseSales(name,sales,month);
                            list.add(newData);

                            i += 3;
                        }

                    }
                }

                //close Connection
                this.CloseConnection();
                st.close();
                rs.close();

                //return list to be displayed
                return list;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.w("Error connection","" + e.getMessage());
                return list;
            }
        }
        else
        {
            return list;
        }
    }
}
