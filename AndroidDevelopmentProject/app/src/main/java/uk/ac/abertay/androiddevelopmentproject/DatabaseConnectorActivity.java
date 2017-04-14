package uk.ac.abertay.androiddevelopmentproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DatabaseConnectorActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String SPValue = "Servers";

    private Bundle extras;
    private String paused = null;

    private boolean connectionStatus = false;

    private Button Button_Connect;
    private Spinner spinnerManager;

    private EditText serverDetails;
    private EditText databaseDetails;
    private EditText usernameDetails;
    private EditText passwordDetails;

    private ArrayList<DatabaseList> servers;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_connector);

        GetExtras();

        intializeInterfaceObjects();
        initalize();
    }

    private void intializeInterfaceObjects(){

        Button_Connect = (Button)findViewById(R.id.Button_Connect);
        Button_Connect.setOnClickListener(this);

        spinnerManager = (Spinner)findViewById(R.id.spinner);
        spinnerManager.setOnItemSelectedListener(this);

        serverDetails = (EditText)findViewById(R.id.Text_Server);
        databaseDetails = (EditText)findViewById(R.id.Text_Database);
        usernameDetails = (EditText)findViewById(R.id.Text_Username);
        passwordDetails = (EditText)findViewById(R.id.Text_Password);
    }

    private void initalize(){

        servers = new ArrayList<DatabaseList>();
        populateServersFromSP();

        if(servers != null){

            populateToSpinner();
        }
    }

    private boolean GetExtras(){

        extras = getIntent().getExtras();

        if(extras != null)
        {
            paused = (String) extras.get("_paused");

            return true;
        }

        return false;
    }

    public void CheckConnection(){

        String server = serverDetails.getText().toString();
        String database = databaseDetails.getText().toString();
        String username = usernameDetails.getText().toString();
        String password = passwordDetails.getText().toString();

        Connections_Controller dbConnect = new Connections_Controller(server, database, username, password);

        try
        {
            connectionStatus = dbConnect.OpenConnection();

            if(connectionStatus)
            {
                Intent i = new Intent(DatabaseConnectorActivity.this, DatabaseVisualizerActivity.class);

                i.putExtra("_server", server);
                i.putExtra("_database", database);
                i.putExtra("_username", username);
                i.putExtra("_password", password);

                startActivity(i);
            }
            else
            {
                Toast.makeText(this, "Can't get connection", Toast.LENGTH_SHORT).show();
                Log.w("Connection", "Can't get connection");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.w("Error connection","" + e.getMessage());
        }
    }

    // Load Vizualizer
    private void loadDatabase(){

        connectionStatus = false;

        if(serverDetails.getText().toString() != "" || databaseDetails.getText().toString() != "" || usernameDetails.getText().toString() != "" || passwordDetails.getText().toString() != "")
        {
            CheckConnection();
        }
    }

    private void populateToSpinner(){

        ArrayList<String> arrayList = new ArrayList<>();

        arrayList.add("Select Server...");

        // Convert set into Arraylist
        for (int i = 0; i < servers.size(); i++){

            arrayList.add(servers.get(i).getServer().substring(4));
        }

        // Display Arraylist in ListView
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        spinnerManager.setAdapter(adapter);
    }

    private void setDetails(String search){

        String[] details = getDetails(search);

        serverDetails.setText(details[0].substring(4).trim());
        databaseDetails.setText(details[1].substring(4).trim());
        usernameDetails.setText(details[2].substring(4).trim());
        passwordDetails.setText(details[3].substring(4).trim());
    }

    private String[] getDetails(String search){

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

    //-------------------------------------------------------------
    //             GATHER DATA
    //-------------------------------------------------------------

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

            for (int i = 0; i < servers.size(); i++)
            {
                Log.i("After Convertion", servers.get(i).getServer());
                Log.i("After Convertion", servers.get(i).getDatabase());
                Log.i("After Convertion", servers.get(i).getUsername());
                Log.i("After Convertion", servers.get(i).getPassword());
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

    // Bubble sort String[]
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

        for(String s : values)
        {
            Log.i("After Sort: ", s);
        }

        return values;
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            case R.id.Button_Connect:
                loadDatabase();
                break;
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {

        if(!spinnerManager.getItemAtPosition(i).toString().equals("Select Server..."))
        {
            setDetails(spinnerManager.getItemAtPosition(i).toString());
        }
        else
        {
            serverDetails.setText("");
            databaseDetails.setText("");
            usernameDetails.setText("");
            passwordDetails.setText("");
        }
    }

    public void onNothingSelected(AdapterView<?> parent) { }

    protected void onResume(){
        super.onResume();

        if(paused != null)
        {
            if(paused.equals("true")){
                Toast.makeText(this, "You were logged out for saftey reasons...", Toast.LENGTH_SHORT).show();
                paused = null;
            }
        }
    }
}
