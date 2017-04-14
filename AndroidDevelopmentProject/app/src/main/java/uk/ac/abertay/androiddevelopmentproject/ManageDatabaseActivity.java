package uk.ac.abertay.androiddevelopmentproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManageDatabaseActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String SPValue = "Servers";

    private String _server = "";
    private String _database = "";
    private String _username = "";
    private String _password = "";

    private Button addServerButton;
    private Button clearServersButton;

    private ListView listView;

    private ArrayList<DatabaseList> servers;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_database);

        listView = (ListView) findViewById(R.id.List_databases);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                buildManageAlert(listView.getItemAtPosition(i).toString());
            }
        });

        initalize();
    }

    // Intialize
    private void initalize(){

        addServerButton = (Button)findViewById(R.id.Button_New);
        addServerButton.setOnClickListener(this);

        clearServersButton = (Button)findViewById(R.id.Button_Clear);
        clearServersButton.setOnClickListener(this);

        servers = new ArrayList<DatabaseList>();
        populateServersFromSP();

        if(servers != null){

            buildListView();
        }
    }

    private void resetListView(){

        servers = new ArrayList<DatabaseList>();
        populateServersFromSP();

        if(servers != null){

            buildListView();
        }
    }

    // Build List View Items
    private void buildListView(){

        ArrayList<String> arrayList = new ArrayList<>();

        // Convert set into Arraylist
        for (int i = 0; i < servers.size(); i++){

            arrayList.add(servers.get(i).getServer().substring(4));
        }

        // Display Arraylist in ListView
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        listView = (ListView) findViewById(R.id.List_databases);
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);
    }

    //-------------------------------------------------------------
    //              Add new item
    //-------------------------------------------------------------

    // Display Alert Box to allow user to input new Details
    private void buildNewAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Details");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputServer = new EditText(this);
        inputServer.setHint("Server...");
        layout.addView(inputServer);

        final EditText inputDatabase = new EditText(this);
        inputDatabase.setHint("Database...");
        layout.addView(inputDatabase);

        final EditText inputUsername = new EditText(this);
        inputUsername.setHint("Username...");
        layout.addView(inputUsername);

        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("Password...");
        layout.addView(inputPassword);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                _server = inputServer.getText().toString();
                _database = inputDatabase.getText().toString();
                _username = inputUsername.getText().toString();
                _password = inputPassword.getText().toString();

                ValidateDatabaseValues();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Validate Input Details
    private void ValidateDatabaseValues(){

        if(_server.equals("") || _database.equals("") || _username.equals("")){

            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "Error! Missing values!", Toast.LENGTH_SHORT);
            toast.show();

        } else {

            // Write to SP
            writeToSP(_server, _database, _username, _password);
            getListViewValues();
        }
    }

    // Get New List View Values
    private void getListViewValues(){

        servers = new ArrayList<DatabaseList>();
        populateServersFromSP();

        if(servers.size() != 0){

            buildListView();
        }
    }

    // Put in thread
    private void writeToSP(String server, String database, String username, String password) {

        // intialize new variables
        SharedPreferences.Editor editor = getSharedPreferences(SPValue, MODE_PRIVATE).edit();

        Set<String> set = new HashSet<>();
        set = readFromSP();

        int index = 1;
        if(set != null)
        {
            if(set.size() < 20) // user can't pass 5 db presets
            {
                if(set.size() == 4)
                    index = 2;
                else if(set.size() == 8)
                    index = 3;
                else if(set.size() == 12)
                    index = 4;
                else if(set.size() == 16)
                    index = 5;

                // add data to set
                set.add(index + ".1 " + server);
                set.add(index + ".2 " + database);
                set.add(index + ".3 " + username);
                set.add(index + ".4 " + password);
            }
        }
        else
        {
            set = new HashSet<>();

            // add data to set
            set.add(index + ".1 " + server);
            set.add(index + ".2 " + database);
            set.add(index + ".3 " + username);
            set.add(index + ".4 " + password);
        }

        Toast.makeText(this, "New database under: " + server + " has been created.", Toast.LENGTH_SHORT).show();

        // clear data from SP
        editor.clear();
        editor.commit();

        // apply new data to SP
        editor.putStringSet(SPValue, set);
        editor.commit();
    }

    //-------------------------------------------------------------
    //             Manage Items
    //-------------------------------------------------------------

    // Display Alert Box to allow user to input Details
    private void buildManageAlert(String search){

        String[] serverDetails = new String[4];
        serverDetails = searchDetails(search);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage Server Details");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputServer = new EditText(this);
        inputServer.setHint("Server...");
        inputServer.setText(serverDetails[0].substring(4).trim());
        layout.addView(inputServer);

        final String previousServer = serverDetails[0].substring(4).trim();

        final EditText inputDatabase = new EditText(this);
        inputDatabase.setHint("Database...");
        inputDatabase.setText(serverDetails[1].substring(4).trim());
        layout.addView(inputDatabase);

        final EditText inputUsername = new EditText(this);
        inputUsername.setHint("Username...");
        inputUsername.setText(serverDetails[2].substring(4).trim());
        layout.addView(inputUsername);

        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("Password...");
        inputPassword.setText(serverDetails[3].substring(4).trim());
        layout.addView(inputPassword);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                String server = inputServer.getText().toString();
                String database = inputDatabase.getText().toString();
                String username = inputUsername.getText().toString();
                String password = inputPassword.getText().toString();

                editSP(previousServer, server, database, username, password);

                resetListView();
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                deleteFromSP(inputServer.getText().toString());

                resetListView();
            }
        });

        builder.show();
    }

    // Delete from Shared Preferences and Reset ListView
    private void deleteFromSP(String search){

        // intialize new variables
        SharedPreferences.Editor editor = getSharedPreferences(SPValue, MODE_PRIVATE).edit();

        Set<String> set = new HashSet<>();
        set = readFromSP();

        int arrayIndex = getIndex(search);

        String[] setValues = new String[set.size() - 4];

        if(set != null)
        {
            int index = 0;
            for(String s : set)
            {
                if(!s.substring(0, 1).equals(String.valueOf(arrayIndex)))
                {
                    setValues[index] = s;
                    index++;
                }
            }
            setValues = sortValues(setValues);
        }

        set = new HashSet<>(Arrays.asList(setValues));

        Toast.makeText(this, "Database has been deleted", Toast.LENGTH_SHORT).show();

        // clear data from SP
        editor.clear();
        editor.commit();

        // apply new data to SP
        editor.putStringSet(SPValue, set);
        editor.commit();
    }

    private void editSP(String search, String server, String database, String username, String password) {

        // intialize new variables
        SharedPreferences.Editor editor = getSharedPreferences(SPValue, MODE_PRIVATE).edit();

        Set<String> set = new HashSet<>();
        set = readFromSP();

        int arrayIndex = getIndex(search);

        String[] setValues = new String[set.size()];

        if(set != null)
        {
            int index = 0;
            for(String s : set)
            {
                setValues[index] = s;
                index++;
            }
            setValues = sortValues(setValues);

            for(int i = 0; i < setValues.length; i++)
            {
                if(setValues[i].substring(4).equals(search))
                {
                    String previousSlot = setValues[i].substring(0, 2);

                    setValues[i] = previousSlot + "1 " + server;
                    setValues[i + 1] = previousSlot + "2 " + database;
                    setValues[i + 2] = previousSlot + "3 " + username;
                    setValues[i + 3] = previousSlot + "4 " + password;
                }
            }
        }

        set = new HashSet<>(Arrays.asList(setValues));

        Toast.makeText(this, "Database has been edited", Toast.LENGTH_SHORT).show();

        // clear data from SP
        editor.clear();
        editor.commit();

        // apply new data to SP
        editor.putStringSet(SPValue, set);
        editor.commit();
    }

    // Get index of Array that search is found at
    private int getIndex(String search){

        int index = 0;

        for(int i = 0; i < servers.size(); i++)
        {
            if(servers.get(i).getServer().substring(4).equals(search))
                index = i + 1;
        }

        return index;
    }

    private void clearSP(){

        SharedPreferences sharedPrefs = getSharedPreferences(SPValue, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.commit();
    }

    //-------------------------------------------------------------
    //             GATHER DATA
    //-------------------------------------------------------------

    // Search through database
    private String[] searchDetails(String search){

        String[] details = new String[4];

        for(int i = 0; i < servers.size(); i++)
        {
            if(servers.get(i).getServer().substring(4).equals(search))
            {
                details[0] = servers.get(i).getServer();
                details[1] = servers.get(i).getDatabase();
                details[2] = servers.get(i).getUsername();
                details[3] = servers.get(i).getPassword();
            }
        }

        return details;
    }

    // Populate ArrayList
    private void populateServersFromSP(){

        String server = "";
        String database = "";
        String username = "";
        String password = "";

        Set<String> set = new HashSet<String>();

        // Get set from SP and then convert into List
        set = readFromSP();

        if(set != null){

            String[] setValues = new String[set.size()];

            int index = 0;
            for(String s : set)
            {
                setValues[index] = s;
                index++;
            }

            setValues = sortValues(setValues);

            int count = 0;
            for(String value : setValues)
            {
                if (count == 0)
                {
                    server = value;
                    count++;
                }
                else if (count == 1)
                {
                    database = value;
                    count++;
                }
                else if(count == 2)
                {
                    username = value;
                    count++;
                }
                else if(count == 3)
                {
                    password = value;

                    DatabaseList db = new DatabaseList(server, database, username, password);
                    servers.add(db);
                    count = 0;
                }
            }
        }
    }

    // Put in thread
    private Set<String> readFromSP(){

        Set<String> set = new HashSet<>();
        SharedPreferences preference = getSharedPreferences(SPValue, MODE_PRIVATE);

        set = preference.getStringSet(SPValue, null);

        // return Set<String>
        return set;
    }

    private String[] sortValues(String[] values){


        boolean swapped = true;
        int j = 0;
        double s1 = 0;
        double s2 = 0;
        String tmp;

        while (swapped) {
            swapped = false;
            j++;
            for (int i = 0; i < values.length - j; i++)
            {
                s1 = Double.parseDouble(values[i].substring(0, 3));
                s2 = Double.parseDouble(values[i + 1].substring(0, 3));

                if (s1 > s2)
                {
                    tmp = values[i];
                    values[i] = values[i + 1];
                    values[i + 1] = tmp;
                    swapped = true;
                }
            }
        }

        return values;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            case R.id.Button_New:
                buildNewAlert();
                break;
            case R.id.Button_Clear:
                clearSP();
                Toast.makeText(this, "All database Information have been cleared.", Toast.LENGTH_SHORT).show();
                resetListView();
                break;
        }
    }
}

