package uk.ac.abertay.androiddevelopmentproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseVisualizerActivity extends AppCompatActivity implements View.OnClickListener ,AdapterView.OnItemSelectedListener{

    private BarChart barChart;
    private LineChart lineChart;
    private Bundle extras;

    private String server = null;
    private String database = null;
    private String username = null;
    private String password = null;

    private List<String> sales;
    private List<String> months;
    private List<DatabaseSales> list;
    private List<String> listAlert;
    private ArrayList<String> details;

    private Connections_Controller dbConnect;

    private List<String> databaseTables;
    private List<String> databaseColumns;

    private String tableSelected = null;
    private String dataSelected = null;
    private String dataToGatherSelected = null;
    private String gatherDataSelected1 = null;
    private String gatherDataSelected2 = null;

    private Button shareButton;

    private Spinner spinnerTables;

    private ArrayAdapter<String> adapter;

    private ArrayList<phoneNumbers> numbers;
    private String number = "";
    private String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_databse_visualizer);

        details = new ArrayList<String>();
        numbers = new ArrayList<phoneNumbers>();

        // Gather extras
        if(GetExtras())
            dbConnect = new Connections_Controller(server, database, username, password);

        Initialize();
        shareButton.setVisibility(View.INVISIBLE);

        if(details != null)
        {
            LoadGraph(details.get(0), details.get(1), details.get(2), details.get(3), details.get(4));
        }
    }

    // ---------------------------------------------------------------------------------------------
    //                                      intializers (OnLoad)
    // ---------------------------------------------------------------------------------------------

    private void startReceivers(){

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){

                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getApplicationContext(), "Error: generic failure!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new IntentFilter("SENT_SMS_ACTION") );

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(),
                        "Message delivered!", Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter("DELIVERED_SMS_ACTION"));

    }

    private boolean GetExtras(){

        extras = getIntent().getExtras();

        if(extras != null)
        {
            server = (String) extras.get("_server");
            database = (String) extras.get("_database");
            username = (String) extras.get("_username");
            password = (String) extras.get("_password");

            details = extras.getStringArrayList("_details");

            return true;
        }

        return false;
    }

    private void Initialize(){

        // InitializeInterface Buttons, textview etc...
        InitializeInterface();

        // Gather tables from selected database & apply to spinner for use to select
        GetDatabaseTables();
        PopulateToSpinner();

        // Insitalize BarChart
        InitializeChart();
    }

    private void InitializeInterface(){

        spinnerTables = (Spinner)findViewById(R.id.Spinner_Table);
        spinnerTables.setOnItemSelectedListener(this);

        shareButton = (Button)findViewById(R.id.Button_SendSMS);
        shareButton.setOnClickListener(this);
    }

    private void GetDatabaseTables() {

        databaseTables = dbConnect.GetTables();
    }

    private void PopulateToSpinner(){

        ArrayList<String> arrayList = new ArrayList<>();

        arrayList.add("Select Table...");

        // Convert set into Arraylist
        for (int i = 0; i < databaseTables.size(); i++){

            arrayList.add(databaseTables.get(i));
        }

        // Display Arraylist in ListView
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        spinnerTables.setAdapter(adapter);
    }

    private void InitializeChart(){

        lineChart = (LineChart)findViewById(R.id.LineGraph);
        barChart = (BarChart)findViewById(R.id.BarGraph);
    }

    // ---------------------------------------------------------------------------------------------
    //                                      Send SMS
    // ---------------------------------------------------------------------------------------------

    // get list of contacts
    private void getNumbers(){

        ContentResolver cr = this.getContentResolver(); //Activity/Application android.content.Context
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if(cursor.moveToFirst())
        {
            numbers.clear();
            do
            {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);

                    while (pCur.moveToNext())
                    {
                        String contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        phoneNumbers contact = new phoneNumbers(contactName, contactNumber);
                        numbers.add(contact);

                        break;
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext()) ;
        }
    }

    private void buildSMSAlert(){

        getNumbers();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send SMS");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        final EditText textPhoneNumber = new EditText(this);
        final Spinner spinnerNumber = new Spinner(this);

        textPhoneNumber.setHint("Enter a phone number...");

        //-------------------------------------------------------------

        ArrayList<String> arrayList = new ArrayList<>();
        List<String> list = new ArrayList<String>();

        arrayList = new ArrayList<>();
        arrayList.add("Select Contact...");

        // Convert set into Arraylist
        for (int i = 0; i < numbers.size(); i++){

            arrayList.add(numbers.get(i).getName());
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);

        spinnerNumber.setAdapter(adapter);
        spinnerNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {

                if(!parent.getItemAtPosition(p).toString().equals("Select Item from Column Above..."))
                {
                    for (int i = 0; i < numbers.size(); i++){

                        if(numbers.get(i).getName().equals(parent.getItemAtPosition(p).toString()))
                            textPhoneNumber.setText(numbers.get(i).getNumber());
                    }

                }
            }

            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //----------------------------------------------------------------------
        //                              Add Views & builder buttons
        //----------------------------------------------------------------------

        layout.addView(textPhoneNumber);
        layout.addView(spinnerNumber);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                number = textPhoneNumber.getText().toString();
                sendSMS();
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

    private void sendSMS(){

        String newMessage = "Code: " + message;
        Log.i("Message", message);

        startReceivers();

        Intent sentIntent = new Intent("SENT_SMS_ACTION");
        PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, 0);

        Intent deliveryIntent = new Intent("DELIVERED_SMS_ACTION");
        PendingIntent deliveryPI = PendingIntent.getBroadcast(getApplicationContext(), 0, deliveryIntent, 0);

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, newMessage, sentPI, deliveryPI);
    }

    // ---------------------------------------------------------------------------------------------
    //                                      Main Methods
    // ---------------------------------------------------------------------------------------------

    // Display Alert Box to allow user to input new Details
    private void buildNewAlert(){

        barChart.setVisibility(View.INVISIBLE);
        lineChart.setVisibility(View.INVISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Graph Details");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Spinners

        final Spinner data = new Spinner(this);
        final Spinner dataToGather = new Spinner(this);
        final Spinner gatherData1 = new Spinner(this);
        final Spinner gatherData2 = new Spinner(this);

        ArrayList<String> arrayList = new ArrayList<>();


        //----------------------------------------------------------------------
        //                              Data to gather ID from
        //----------------------------------------------------------------------

        arrayList = new ArrayList<>();
        arrayList.add("Select Data to Gather...");

        for (int i = 0; i < databaseColumns.size(); i++){

            arrayList.add(databaseColumns.get(i));
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);

        data.setAdapter(adapter);
        data.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {

                if(!parent.getItemAtPosition(p).toString().equals("Select Data to Gather..."))
                {
                    ArrayList<String> arrayList = new ArrayList<>();
                    List<String> list = new ArrayList<String>();

                    list = dbConnect.GetColumnContents(tableSelected, parent.getItemAtPosition(p).toString());


                    arrayList = new ArrayList<>();
                    arrayList.add("Select Data from column...");
                    arrayList.add("All");

                    // Convert set into Arraylist
                    for (int i = 0; i < list.size(); i++){

                        arrayList.add(list.get(i));
                    }

                    adapter = new ArrayAdapter<String>(DatabaseVisualizerActivity.this, android.R.layout.simple_list_item_1, arrayList);
                    dataToGather.setAdapter(adapter);

                    dataSelected = parent.getItemAtPosition(p).toString();
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //----------------------------------------------------------------------
        //                              ID to gather data from
        //----------------------------------------------------------------------

        List<String> list = new ArrayList<String>();

        arrayList = new ArrayList<>();
        //arrayList.add("Select Item to use...");

        // Convert set into Arraylist
        for (int i = 0; i < list.size(); i++){

            arrayList.add(list.get(i));
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);

        dataToGather.setAdapter(adapter);
        dataToGather.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {

                if(!parent.getItemAtPosition(p).toString().equals("Select Item from Column Above..."))
                {
                    dataToGatherSelected = parent.getItemAtPosition(p).toString();
                }
            }

            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //----------------------------------------------------------------------
        //                              Data to display
        //----------------------------------------------------------------------

        arrayList = new ArrayList<>();
        arrayList.add("Select Data to display on X-axis...");

        // Convert set into Arraylist
        for (int i = 0; i < databaseColumns.size(); i++){

            arrayList.add(databaseColumns.get(i));
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);

        gatherData1.setAdapter(adapter);
        gatherData1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {

                if(!parent.getItemAtPosition(p).toString().equals("Select Data For X-Axis..."))
                {
                    gatherDataSelected1 = parent.getItemAtPosition(p).toString();
                }
            }

            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //----------------------------------------------------------------------
        //                              Data to display
        //----------------------------------------------------------------------

        arrayList = new ArrayList<>();
        arrayList.add("Select Data to display on Y-axis...");

        // Convert set into Arraylist
        for (int i = 0; i < databaseColumns.size(); i++){

            arrayList.add(databaseColumns.get(i));
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);

        gatherData2.setAdapter(adapter);
        gatherData2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int p, long id) {

                if(!parent.getItemAtPosition(p).toString().equals("Select Data For X-Axis..."))
                {
                    gatherDataSelected2 = parent.getItemAtPosition(p).toString();
                }
            }

            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        //----------------------------------------------------------------------
        //                              Add Views & builder buttons
        //----------------------------------------------------------------------

        layout.addView(data);
        layout.addView(dataToGather);
        layout.addView(gatherData1);
        layout.addView(gatherData2);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                spinnerTables.setSelection(0);
                // Find details
                LoadGraph(tableSelected, dataSelected, dataToGatherSelected, gatherDataSelected1, gatherDataSelected2);
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

    private void LoadGraph(String table, String column, String toGather, String xAxis, String yAxis){

        String pw = "";

        Encryption encryption = new Encryption();
        try{
            pw = encryption.bytesToHex( encryption.encrypt(password) );
        }
        catch (Exception e)
        {

        }

        message = "sv:" + server + ",db:" + database + ",un:" + username + ",pw:" + pw + ",tb:" + table + ",sl:" + column + ",gt:" + toGather + ",xA:" + xAxis + ",yA:" + yAxis + ",";

        if(!toGather.equals("All")) {

            sales = dbConnect.GetData(table, yAxis, column, toGather);
            months = dbConnect.GetData(table, xAxis, column, toGather);

            ArrayList<BarEntry> barEntries = new ArrayList<>();

            for (int i = 0; i < sales.size(); i++) {
                barEntries.add(new BarEntry(Float.valueOf(sales.get(i)), i));
            }

            BarDataSet dataSet = new BarDataSet(barEntries, "Sales per month");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            BarData data = new BarData(months, dataSet);
            barChart.setData(data);


            barChart.setTouchEnabled(true);
            barChart.setDragEnabled(true);
            barChart.setScaleEnabled(true);
            barChart.setDescription(toGather + "'s monthly sales");
            barChart.notifyDataSetChanged();
            barChart.setVisibility(View.VISIBLE);
        }
        else{

            list = dbConnect.GetAllData(table, yAxis, xAxis, column);

            LineData data = new LineData();
            data.setValueTextColor(Color.BLACK);

            lineChart.setData(data);

            lineChartSetup();

            List<String> names = new ArrayList<String>();

            for(int s = 0; s < list.size(); s++){

                String currentName = list.get(s).getStoreName();

                boolean found = false;
                for (String n : names)
                {
                    if(n.equals(currentName))
                        found = true;
                }

                if(!found)
                    names.add(currentName);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();

            for (int i = 0; i < names.size(); i++)
            {
                String storeName = names.get(i);

                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

                ArrayList<String> xVals = new ArrayList<String>();
                for(int x = 0; x < list.size(); x++){

                    if(list.get(x).getStoreName().equals(storeName)){
                        xVals.add(String.valueOf(list.get(x).getmonth().substring(5,7)));
                    }
                }

                ArrayList<Entry> yVals = new ArrayList<Entry>();
                int count = 0;
                for(int y = 0; y < list.size(); y++){

                    if(list.get(y).getStoreName().equals(storeName)){
                        yVals.add(new Entry(list.get(y).getsales(), count));

                        count++;
                    }
                }

                if(i == 0)
                {
                    LineDataSet set1 = new LineDataSet(yVals, storeName);
                    set1.setColor(color);
                    set1.setCircleColor(color);
                    dataSets.add(set1);
                }
                else if (i == 1)
                {
                    LineDataSet set2 = new LineDataSet(yVals, storeName);
                    set2.setColor(color);
                    set2.setCircleColor(color);
                    dataSets.add(set2);
                }
                else if (i == 2)
                {
                    LineDataSet set3 = new LineDataSet(yVals, storeName);
                    set3.setColor(color);
                    set3.setCircleColor(color);
                    dataSets.add(set3);
                }
                else if (i == 3)
                {
                    LineDataSet set4 = new LineDataSet(yVals, storeName);
                    set4.setColor(color);
                    set4.setCircleColor(color);
                    dataSets.add(set4);
                }

                lineChart.setData(new LineData(xVals,dataSets));
            }

            lineChart.notifyDataSetChanged();
            lineChart.setVisibility(View.VISIBLE);
        }
        shareButton.setVisibility(View.VISIBLE);
    }

    private void lineChartSetup(){

        Legend l = lineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis x1 = lineChart.getXAxis();
        x1.setTextColor(Color.BLACK);
        x1.setDrawGridLines(false);
        x1.setAvoidFirstLastClipping(true);

        YAxis y1 = lineChart.getAxisLeft();
        y1.setTextColor(Color.BLACK);
        y1.setAxisMaxValue(10000f);
        y1.setDrawGridLines(true);

        YAxis y2 = lineChart.getAxisRight();
        y2.setEnabled(false);
    }

    // ---------------------------------------------------------------------------------------------
    //                                      Listeners
    // ---------------------------------------------------------------------------------------------

    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {

        if(!spinnerTables.getItemAtPosition(i).toString().equals("Select Table..."))
        {
            databaseColumns = dbConnect.GetColumns(spinnerTables.getItemAtPosition(i).toString());
            buildNewAlert();

            tableSelected = spinnerTables.getItemAtPosition(i).toString();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.Button_SendSMS:
                buildSMSAlert();
                break;
        }
    }

    protected void onPause(){
        super.onPause();

        Intent myIntent = new Intent(DatabaseVisualizerActivity.this, DatabaseConnectorActivity.class);

        myIntent.putExtra("_paused", "true");

        startActivity(myIntent);
    }
}

class phoneNumbers {

    String contactName = "";
    String contactNumber = "";

    public phoneNumbers(String name, String number)
    {
        contactName = name;
        contactNumber = number;
    }

    public String getName() { return contactName; }
    public String getNumber() { return contactNumber; }
}
