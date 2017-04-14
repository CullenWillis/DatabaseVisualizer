package uk.ac.abertay.androiddevelopmentproject;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button manageDatabases;
    Button DatabaseConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        registerReceiver(new IncomingSmsInterceptor(), new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    private void initialize(){

        manageDatabases = (Button)findViewById(R.id.Button_ManageDatabase);
        manageDatabases.setOnClickListener(this);

        DatabaseConnect = (Button)findViewById(R.id.Button_Connect);
        DatabaseConnect.setOnClickListener(this);
    }

    private void loadDatabaseManager(){

        Intent myIntent = new Intent(MainActivity.this, ManageDatabaseActivity.class);
        startActivity(myIntent);
    }

    private void loadDatabaseConnector(){

        Intent myIntent = new Intent(MainActivity.this, DatabaseConnectorActivity.class);
        startActivity(myIntent);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            case R.id.Button_ManageDatabase:
                loadDatabaseManager();
                break;
            case R.id.Button_Connect:
                loadDatabaseConnector();
                break;
        }
    }
}
