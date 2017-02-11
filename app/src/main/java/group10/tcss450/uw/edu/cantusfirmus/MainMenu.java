package group10.tcss450.uw.edu.cantusfirmus;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainMenu extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
    private static Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        handler = new Handler();
        Button playMusicbtn = (Button) findViewById(R.id.MusicPlayerBtn);
        playMusicbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMusic(view);
            }
        });
        Button findMusicBtn = (Button) findViewById(R.id.ExternalPlayerBtn);
        findMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findMusic(view);
            }
        });
        Button userInfoBtn = (Button) findViewById(R.id.UserInfoBtn);
        userInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInfo(view);
            }
        });
    }

    public void requestPermissions(){
        //Requests permissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Storage access is available
                }else{
                    //Permission Denied
                    Toast.makeText(MainMenu.this,"Storage Permission Denied, Playback Disabled",Toast.LENGTH_LONG);
                }
                return;
            }
        }
    }
    private void playMusic(View view) {
        Intent intent = new Intent(this, audio_player.class);
        startActivity(intent);
    }
    private void findMusic(View view) {
        Intent intent = new Intent(this, find_music.class);
        startActivity(intent);
    }
    private void userInfo(View view) {
        Intent intent = new Intent(this, info.class);
        startActivity(intent);
    }
    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you wish to log out?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    logout();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }
    private void logout() throws IOException{
        CookieManager cm = login.getCookieManager();
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        OkHttpClient client = new OkHttpClient.Builder().cookieJar(new JavaNetCookieJar(cm)).build();
        Request request = new Request.Builder()
                .url("https://hidden-scrubland-70822.herokuapp.com/logout")
                .get()
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "c7ba7ef7-d585-9974-b57e-e0f3bdb8c6ac")
                .build();
        client.newCall(request).execute();
        handler.post(new Runnable(){
            @Override
            public void run(){
                finish();
            }
        });

    }


}
