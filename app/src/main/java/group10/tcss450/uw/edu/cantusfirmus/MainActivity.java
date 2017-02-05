package group10.tcss450.uw.edu.cantusfirmus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playMusicbtn = (Button) findViewById(R.id.MusicPlayerBtn);

        playMusicbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playMusic(view);
            }
        });
    }


    private void playMusic(View view) {
        Intent intent = new Intent(this, audio_player.class);
        startActivity(intent);

    }


}
