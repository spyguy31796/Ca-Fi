package group10.tcss450.uw.edu.cantusfirmus;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;

import okhttp3.Cookie;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/***
 * The login page. Communicates with the server using OkHttp.
 */
public class login extends AppCompatActivity implements View.OnClickListener {
    /***
     * Handler to allow other threads to touch the UI thread.
     */
    private Handler handler;
    /***
     * Cookie manager to keep the login cookie.
     */
    static final CookieManager cm = new CookieManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button b = (Button)findViewById(R.id.perform_login);
        b.setOnClickListener(this);
        handler = new Handler();
    }

    /***
     * Login function, creates and OkHttpClient and generates a request, seding it to the server. The response is stored,
     * and the cookie is also kept.
     * @param login the email to use in the login attempt.
     * @param password the password to use in the login attempt.
     * @throws IOException In the event of an izssue with the server, an IOException may be generated.
     */
    private void login(String login, String password) throws IOException{
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(cm)).build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,"email="+login+"&password="+password);
        Request request = new Request.Builder()
                .url("https://hidden-scrubland-70822.herokuapp.com/login")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "105de595-5532-1029-3f73-6ef2903a00d7")
                .build();
        final Response response = client.newCall(request).execute();
        Log.d("response",response.peekBody(Long.valueOf("100")).string());
        final String[] details = response.body().string().split(",");
        //Log.d("Cookies",cm.getCookieStore().getCookies().get(0).toString());
        handler.post(new Runnable(){
            @Override
            public void run(){
                if(details[0].contains("Success")){
                    Intent i = new Intent(login.this,MainMenu.class);
                    startActivity(i);
                }else if(details[0].contains("user currently signed in")){
                    Toast.makeText(login.this,"User is already signed in, Please sign out!",Toast.LENGTH_LONG).show();
                }else if(details[0].contains("no user found")||details[0].contains("incorrect email")){
                    Toast.makeText(login.this,"Incorrect email or password!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /***
     * Click listener for the login button.
     * @param view the button clicked.
     */
    public void onClick(View view){
        final String login = ((EditText)findViewById(R.id.username_field)).getText().toString().replace(" ","%40");
        final String password = ((EditText)findViewById(R.id.password_field)).getText().toString().replace(" ","%40");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    login(login,password);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /***
     * Allows the cookiemanager to be accessed in other classes.
     * @return CookieManager for the current session.
     */
    public static CookieManager getCookieManager(){
        return cm;
    }
}
