package group10.tcss450.uw.edu.cantusfirmus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainMenu extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
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
    }

    public void requestPermissions(){
        //Requests permissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                        MY_PERMISSIONS_REQUEST_INTERNET);
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
            case MY_PERMISSIONS_REQUEST_INTERNET:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Internet access is available
                }else{
                    //Permission Denied
                    Toast.makeText(MainMenu.this,"Internet Permission Denied, Remote Playback Disabled",Toast.LENGTH_LONG);
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


}
