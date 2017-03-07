package group10.tcss450.uw.edu.cantusfirmus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/***
 * The login page. Communicates with the server using OkHttp.
 * @author Alec Walsh
 * @version Feb 10 2017
 */
public class login extends AppCompatActivity implements View.OnClickListener {
    Button b;
    /***
     * Handler to allow other threads to touch the UI thread.
     */
    private Handler handler;
    private static SharedPreferences mPrefs;
    /***
     * Cookie manager to keep the login cookie.
     */
    static final CookieManager cm = new CookieManager();

    /***
     * Oncreate method contains magic to store and restore login session from shared preferences.
     * @param savedInstanceState Required Parameter
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_login);
        b = (Button)findViewById(R.id.perform_login);
        b.setOnClickListener(this);
        handler = new Handler();
        CookieHandler.setDefault(cm);
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        String tempCookie = mPrefs.getString("session","");
        //Log.d("COOKIE!!!",tempCookie);
        if(!(tempCookie.equals(""))){
            b.setEnabled(false);
            Toast.makeText(this, "Pre-existing session detected, attempting to login", Toast.LENGTH_SHORT).show();
            //Cookie.parse(null,"str");
            //Cookie.Builder ce = new Cookie.Builder();
            Cookie ce = new Cookie.Builder().domain("damp-anchorage-73052.herokuapp.com").name("session").value(tempCookie).httpOnly().build();
            //Log.d("Generated Cookie",ce.toString());
            List cookie = new ArrayList();
            cookie.add(ce);
            //Log.d("COOKIE ADDED","It was added");
            new JavaNetCookieJar(cm).saveFromResponse(HttpUrl.parse("https://damp-anchorage-73052.herokuapp.com/"),cookie);
            //Log.d("Here is added cookie",cm.getCookieStore().getCookies().toString());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        test_Cookie();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }

    /***
     * Login function, creates and OkHttpClient and generates a request, seding it to the server. The response is stored,
     * and the cookie is also kept.
     * @param login the email to use in the login attempt.
     * @param password the password to use in the login attempt.
     * @throws IOException In the event of an issue with the server, an IOException may be generated.
     */
    private void login(String login, String password) throws IOException{
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(cm))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,"email="+login+"&password="+password);
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/login")
                .post(body)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "6001af9e-af55-fdfa-6ffa-7dbb370b2ac3")
                .build();
        final Response response = client.newCall(request).execute();
        //Log.d("RequestLogin",request.headers().toString());
        //Log.d("response",response.peekBody(Long.valueOf("100")).string());
        final String[] details = response.body().string().split(",");
        if(cm.getCookieStore().getCookies().size()>0) {
            mPrefs.edit().putString("session", cm.getCookieStore().getCookies().get(cm.getCookieStore().getCookies().size() - 1).getValue()).apply();
        }
        handler.post(new Runnable(){
            @Override
            public void run(){
                b.setEnabled(true);
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
        view.setEnabled(false);
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
    public static void clearShared(){
        mPrefs.edit().putString("session","").apply();
    }

    private void test_Cookie() throws IOException{
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(login.getCookieManager()))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        //Log.d("What cookie to use",cm.getCookieStore().getCookies().get(0).getValue());
        Request request = new Request.Builder()
                .url("https://damp-anchorage-73052.herokuapp.com/user_info")
                .get()
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "536e387f-52c2-946a-fdac-79006118dbc8")
                .build();
        //Log.d("Request",request.toString());
        Response response = client.newCall(request).execute();
        final String[] details = response.body().string().split(",");
        //Log.d("LOOKLISTEN",details[0]);
        handler.post(new Runnable(){
            @Override
            public void run(){
                if(details[0].contains("error")){
                    b.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Pre-existing session invalid, please log in", Toast.LENGTH_SHORT).show();
                }else{
                    b.setEnabled(true);
                    Intent i = new Intent(login.this,MainMenu.class);
                    startActivity(i);
                }
            }
        });
    }
}
