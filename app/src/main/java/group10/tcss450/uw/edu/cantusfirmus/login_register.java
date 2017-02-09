package group10.tcss450.uw.edu.cantusfirmus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class login_register extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        Button b = (Button)findViewById(R.id.login_btn);
        b.setOnClickListener(this);
        b = (Button)findViewById(R.id.register_btn);
        b.setOnClickListener(this);
    }
    public void onClick(View view){
        Intent i;
        switch (view.getId()){
            case R.id.login_btn:
                i = new Intent(this,login.class);
                startActivity(i);
                break;
            case R.id.register_btn:
                i = new Intent(this,register.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }
}
