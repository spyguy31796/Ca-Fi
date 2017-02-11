package group10.tcss450.uw.edu.cantusfirmus;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class register extends AppCompatActivity implements View.OnClickListener {
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Button b = (Button)findViewById(R.id.perform_registration);
        b.setOnClickListener(this);
        handler = new Handler();
    }
    private void register(String email, String password, String firstName, String lastName, String userName)throws IOException{
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "email="+email+"&password="+password+"&firstName="+firstName+"&lastName="+lastName+"&userName="+userName);
        Request request = new Request.Builder()
                .url("https://hidden-scrubland-70822.herokuapp.com/register")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "40f43f9d-e8b7-2cb6-8352-717662b9068c")
                .build();
        Response response = client.newCall(request).execute();
        final String[] details = response.body().string().split(",");
        handler.post(new Runnable(){
            @Override
            public void run(){
                if(details[0].contains("error")){
                    Toast.makeText(register.this,"Username or Email already taken!",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(register.this,"Registration Succesful, Logging in!", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(register.this,MainMenu.class);
                    startActivity(i);
                }
            }
        });
    }
    public void onClick(View view){
        final String email = ((EditText)findViewById(R.id.reg_email)).getText().toString().replace(" ","%40");
        final String password = ((EditText)findViewById(R.id.reg_password)).getText().toString().replace(" ","%40");
        final String firstName = ((EditText)findViewById(R.id.reg_firstName)).getText().toString().replace(" ","%40");
        final String lastName = ((EditText)findViewById(R.id.reg_lastName)).getText().toString().replace(" ","%40");
        final String userName = ((EditText)findViewById(R.id.reg_userName)).getText().toString().replace(" ","%40");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    register(email,password,firstName,lastName,userName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
