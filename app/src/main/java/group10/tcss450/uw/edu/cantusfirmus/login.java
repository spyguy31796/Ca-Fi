package group10.tcss450.uw.edu.cantusfirmus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class login extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button b = (Button)findViewById(R.id.perform_login);
        b.setOnClickListener(this);
    }
    public void onClick(View view){
        //Replace this with real login code
        Intent i = new Intent(this,MainMenu.class);
        startActivity(i);
    }
}
