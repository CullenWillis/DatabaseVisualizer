package uk.ac.abertay.androiddevelopmentproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

public class IncomingSmsInterceptor extends BroadcastReceiver {

    Encryption encryption;
    final SmsManager smsManager = SmsManager.getDefault();
    private String message = "";

    String server = "";
    String database = "";
    String username = "";
    String password = "";

    ArrayList<String> details;

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        if(bundle != null)
        {
            Object[] smsMessage = (Object[])bundle.get("pdus"); // Get SMS message
            String format = (String)bundle.get("format"); // Get format
            SmsMessage[] messages = new SmsMessage[smsMessage.length]; // put SMS message(s) into messages

            for(int i = 0; i < smsMessage.length; i++){ // Get each message

                messages[i] = SmsMessage.createFromPdu((byte[])smsMessage[i], format); // Get message at index
                message = messages[i].getMessageBody(); // Get SMS body

                Toast.makeText(context, "Message Received. Connecting to db...", Toast.LENGTH_SHORT).show(); // Toast telling user SMS has been received
            }

            findDetails(); // Find details from message

            connect(context); // Force app to connect to database
        }
    }

    private void connect(Context context){

        Intent i = new Intent(context, DatabaseVisualizerActivity.class);

        i.putExtra("_server", server);
        i.putExtra("_database", database);
        i.putExtra("_username", username);
        i.putExtra("_password", password);

        i.putStringArrayListExtra("_details", details);

        context.startActivity(i);
    }

    private void findDetails()
    {
        details = new ArrayList<String>(); // new arraylist of each detail
        int start = 0;

        Encryption encryption = new Encryption(); // Instantiate new encryption

        int count = 0;
        for(int i = 6; i < message.length(); i++) // for each detail (6 details)
        {
            if(message.substring(i, i + 1).equals(":"))
            {
                start = i + 1;
            }

            if(message.substring(i, i + 1).equals(","))
            {
                if(count == 0){
                    server = message.substring(start, i); // Get server details
                }
                else if (count == 1){
                    database = message.substring(start, i); // Get database details
                }
                else if (count == 2){
                    username = message.substring(start, i); // Get usename details
                }
                else if(count == 3) {

                    String toDecrypt = "";

                    try
                    {
                        toDecrypt = new String( encryption.decrypt( message.substring(start, i) )); // Decrypt password
                    }
                    catch (Exception e)
                    {
                        Log.e("Error", e.toString());
                    }

                    password = toDecrypt;
                }
                else {

                    details.add(message.substring(start, i)); // Everything else (Grid details) put into arraylist
                }

                count ++;
            }
        }
    }
}