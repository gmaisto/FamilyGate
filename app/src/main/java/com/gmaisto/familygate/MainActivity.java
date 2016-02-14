package com.gmaisto.familygate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.goebl.david.Webb;
import com.goebl.david.WebbException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private Context ctx = this;
    private TextView temp;
    private TextView garage;
    private TextView patio;
    private TextView lux;
    private TextView infomsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();


        temp = (TextView) findViewById(R.id.textView);
        temp.setText("N/A");

        garage = (TextView) findViewById(R.id.garageValue);
        garage.setText("n/a");


        patio = (TextView) findViewById(R.id.patioValue);
        patio.setText("n/a");

        lux = (TextView) findViewById(R.id.luxValue);
        lux.setText("n/a");

        infomsg = (TextView) findViewById(R.id.infoMessage);
        infomsg.setText("");

        if (networkInfo == null || !networkInfo.isConnected()) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("FamilyGate");
            alert.setMessage("Attenzione. Non è stata rilevata alcuna connessione alla rete.");

            alert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            finish();
                        }
                    });

            alert.show();
        } else {

            new DownloadFamilyGateDataTask().execute("");

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        new DownloadFamilyGateDataTask().execute("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
            new DownloadFamilyGateDataTask().execute("");
            return true;
    }



    private class DownloadFamilyGateDataTask extends AsyncTask<String, Void, String> {

        private ProgressDialog progDailog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(MainActivity.this);
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            try {

                Webb webb = Webb.create();
                result = webb
                        .get("http://familygate.mooo.com:9886/fgdata")
                        .retry(5, true) // at most three retry, don't do exponential backoff
                        .asString()
                        .getBody();

            } catch (WebbException exception) {
                Toast.makeText(ctx,
                        "Errore di comunicazione con FamilyGate",
                        Toast.LENGTH_SHORT).show();
                finish();
            }


            return result;
        }

        protected void onPostExecute(String result) {
            if (progDailog.isShowing()) {
                progDailog.dismiss();
            }
            if (!result.equals("")) {
                Log.d("MyApp", "Received json  " + result);
                try {
                    JSONObject jObj = new JSONObject(result);

                    String message = jObj.getString("message");
                    if (!message.equals("OK")) {
                        Toast.makeText(ctx,
                                "Sistema non disponibile",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    String temps = jObj.getString("temp").replaceAll("0+$", "").replaceAll("\\.$",".0");


                    temp.setText(temps + "°");
                    lux.setText(jObj.getString("lux"));
                    garage.setText(jObj.getString("light1"));
                    if (jObj.getString("lstate").equals("0")) {
                        patio.setText("OFF");
                    } else {
                        patio.setText("ON");
                    }
                    infomsg.setText(jObj.getString("lastseen"));

                } catch (JSONException exception) {

                    Toast.makeText(ctx,
                            "Errore di ricezione: formato dati",
                            Toast.LENGTH_SHORT).show();
                    finish();

                }
            }
        }


    }


}
