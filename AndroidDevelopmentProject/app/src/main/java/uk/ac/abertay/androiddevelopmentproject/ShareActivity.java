package uk.ac.abertay.androiddevelopmentproject;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener ,AdapterView.OnItemSelectedListener{

    private Bundle extras;

    private String server = null;
    private String database = null;
    private String username = null;
    private String password = null;

    private String dbTable = null;
    private String dbSelected = null;
    private String dbGather = null;
    private String dbXAxis = null;
    private String dbYAxis = null;

    private Spinner spinnerNumbers;
    private Button sendSMS;
    private EditText text_PhoneNumber;
    private EditText text_Message;

    private String message = "";
    private String number = "";

    private ArrayList<phoneNumbers> numbers;

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        initializeInterface();

        numbers = new ArrayList<phoneNumbers>();
        getNumbers();
        PopulateToSpinner();

        getExtras();
        Log.i("Message", message);

    }

    private void getExtras(){

        extras = getIntent().getExtras();

        if(extras != null)
        {
            server = (String) extras.get("_server");
            database = (String) extras.get("_database");
            username = (String) extras.get("_username");
            password = (String) extras.get("_password");

            dbTable = (String) extras.get("_table");
            dbSelected = (String) extras.get("_selected");
            dbGather = (String) extras.get("_gather");
            dbXAxis = (String) extras.get("_x");
            dbYAxis = (String) extras.get("_y");

            password = md5(password);

            message = "sv:" + server + ",db:" + database + ",un:" + username + ",pw:" + password + ",tb:" + dbTable + ",sl:" + dbSelected + ",gt:" + dbGather + ",x:" + dbXAxis + ",y:" + dbYAxis;

        }
    }

    private void initializeInterface() {

        spinnerNumbers = (Spinner)findViewById(R.id.Spinner_Contacts);
        spinnerNumbers.setOnItemSelectedListener(this);

        sendSMS = (Button)findViewById(R.id.Button_SendSMS);
        sendSMS.setOnClickListener(this);

        text_PhoneNumber = (EditText)findViewById(R.id.Text_PhoneNumber);
        text_Message = (EditText)findViewById(R.id.Text_SMSMessage);
    }

    // Convert password into encrypted string for sending
    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

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

    // populate spinner with contacts
    private void PopulateToSpinner(){

        ArrayList<String> arrayList = new ArrayList<>();

        arrayList.add("Select Contact...");

        for(int i = 0; i < numbers.size(); i++)
        {
            arrayList.add(numbers.get(i).getName());
        }

        // Display Arraylist in ListView
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        spinnerNumbers.setAdapter(adapter);
    }

    private void sendSMS(){


    }

    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {

        if(!spinnerNumbers.getItemAtPosition(i).toString().equals("Select Contact..."))
        {
            for(int n = 0; n < numbers.size(); n++)
            {
                if(numbers.get(n).getName().equals(spinnerNumbers.getItemAtPosition(i).toString()))
                {
                    text_PhoneNumber.setText(numbers.get(n).getNumber());
                }
            }

        }
    }

    public void onNothingSelected(AdapterView<?> parent) { }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.Button_SendSMS:
                sendSMS();
            break;
        }
    }
}


