package group10.tcss450.uw.edu.cantusfirmus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
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
                    Toast.makeText(MainActivity.this,"Storage Permission Denied, Playback Disabled",Toast.LENGTH_LONG);
                }
                return;
            }
        }
    }
    private void playMusic(View view) {
        Intent intent = new Intent(this, audio_player.class);
        startActivity(intent);

    }


}
