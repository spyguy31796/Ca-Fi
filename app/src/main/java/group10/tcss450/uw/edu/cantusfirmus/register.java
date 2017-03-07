package group10.tcss450.uw.edu.cantusfirmus;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/***
 * Register screen for registering a new account with the server, uses okhttp.
 * The register activity also checks to verify that the password matches the password confirm and that the email address contains
 * and @ symbol.
 * @author Alec Walsh
 * @version Feb 10 2017
 */
public class register extends AppCompatActivity implements View.OnClickListener {
    Button b;
    /***
     * Handler to allow other threads to touch the UI thread.
     */
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        b = (Button)findViewById(R.id.perform_registration);
        b.setOnClickListener(this);
        handler = new Handler();
    }

    /***
     * Register function creates an OkHttpClient and a request which contains all the registration data.
     * This is all sent to the server, and a response is generated which is check for a success or failure report.
     * @param email the email to register to the account.
     * @param password the password for the account.
     * @param firstName the first name for the account.
     * @param lastName the last name for the account.
     * @param userName the username for the account.
     * @throws IOException thrown if there is a network issue.
     */
    private void register(String email, String password, String firstName, String lastName, String userName)throws IOException{
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "email="+email+"&password="+password+"&firstName="+firstName+"&lastName="+lastName+"&userName="+userName);
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/register")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "45412697-790a-95cf-e88e-e0b72fea4f6f")
                .build();
        Response response = client.newCall(request).execute();
        final String[] details = response.body().string().split(",");
        handler.post(new Runnable(){
            @Override
            public void run(){
                b.setEnabled(true);
                if(details[0].contains("error")){
                    Toast.makeText(register.this,"Username or Email already taken!",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(register.this,"Registration Successful, Please Log in!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    /***
     * Handles the registration button click. Does the input validation.
     * @param view the button clicked.
     */
    public void onClick(View view){
        b.setEnabled(false);
        final String email = ((EditText)findViewById(R.id.reg_email)).getText().toString().replace(" ","%40");
        final String password = ((EditText)findViewById(R.id.reg_password)).getText().toString().replace(" ","%40");
        final String firstName = ((EditText)findViewById(R.id.reg_firstName)).getText().toString().replace(" ","%40");
        final String lastName = ((EditText)findViewById(R.id.reg_lastName)).getText().toString().replace(" ","%40");
        final String userName = ((EditText)findViewById(R.id.reg_userName)).getText().toString().replace(" ","%40");
        String testPword = ((EditText)findViewById(R.id.reg_password)).getText().toString();
        String testPword2 = ((EditText)findViewById(R.id.reg_confirmPass)).getText().toString();
        if(testPword.equals(testPword2)) {
            if(email.contains("@")) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            register(email, password, firstName, lastName, userName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }else{
                ((EditText)findViewById(R.id.reg_email)).setError("Invalid email address");
            }
        }else{
            ((EditText)findViewById(R.id.reg_confirmPass)).setError("Passwords do not match!");
        }
    }
}
