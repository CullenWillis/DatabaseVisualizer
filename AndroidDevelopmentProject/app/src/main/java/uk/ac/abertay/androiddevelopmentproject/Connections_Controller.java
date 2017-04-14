package uk.ac.abertay.androiddevelopmentproject;

import android.util.Log;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.Semaphore;

/**
 * Created by Cullen on 07/04/2017.
 */

public class Connections_Controller{

    DatabaseConnections db;

    boolean found = false;

    List<String> list;
    List<DatabaseSales> dbList;

    public Connections_Controller(String _server, String _database, String _username, String _password) {

        db = new DatabaseConnections(_server, _database, _username, _password);
    }

    public boolean OpenConnection() {

        found = false;

        ExecutorService es = Executors.newSingleThreadExecutor();

        Future<Boolean> result = es.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {

                return db.OpenConnection();
            }
        });

        try {

            found = result.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();

        Log.i("Thread Value", String.valueOf(found));

        return found;
    }

    public boolean CloseConnection(){

        found = false;

        ExecutorService es = Executors.newSingleThreadExecutor();

        Future<Boolean> result = es.submit(new Callable<Boolean>() {
            public Boolean call() throws Exception {

                return db.CloseConnection();
            }
        });

        try {

            found = result.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();

        Log.i("Thread Value", String.valueOf(found));

        return found;
    }

    public List<String> GetTables() {

        list = new ArrayList<String>();

        ExecutorService es = Executors.newSingleThreadExecutor();

        Future<List<String>> result = es.submit(new Callable<List<String>>() {
            public List<String> call() throws Exception {

                return db.GetTables();
            }
        });

        try {

            list = result.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();


        return list;
    }

    public List<String> GetColumns(String table) {

        list = new ArrayList<String>();

        ExecutorService es = Executors.newSingleThreadExecutor();

        Future<List<String>> result = es.submit(new Callable<List<String>>() {
            public List<String> call() throws Exception {

                return db.GetColumns(table);
            }
        });

        try {

            list = result.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();


        return list;
    }

    public List<String> GetColumnContents(String table, String column) {

        list = new ArrayList<String>();

        ExecutorService es = Executors.newSingleThreadExecutor();

        Future<List<String>> result = es.submit(new Callable<List<String>>() {
            public List<String> call() throws Exception {

                return db.GetColumnContents(table, column);
            }
        });

        try {

            list = result.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();


        return list;
    }

    public List<String> GetData(String table, String toGather, String toCompare, String where) {

        list = new ArrayList<String>();

        ExecutorService es = Executors.newSingleThreadExecutor();

        Future<List<String>> result = es.submit(new Callable<List<String>>() {
            public List<String> call() throws Exception {

                return db.GetData(table,toGather, toCompare, where);
            }
        });

        try {

            list = result.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();


        return list;
    }

    public List<DatabaseSales> GetAllData(String table, String item1, String item2, String item3) {

        dbList = new ArrayList<DatabaseSales>();

        ExecutorService es = Executors.newSingleThreadExecutor();

        Future<List<DatabaseSales>> result = es.submit(new Callable<List<DatabaseSales>>() {
            public List<DatabaseSales> call() throws Exception {

                return db.GetAllData(table, item1, item2, item3);
            }
        });

        try {

            dbList = result.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        es.shutdown();


        return dbList;
    }
}
