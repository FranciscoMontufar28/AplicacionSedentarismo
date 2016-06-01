package me.jbakita.pebbledatalogging;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class Menu extends Activity implements View.OnClickListener {

    Button entrenar_app;
    ImageButton iniciar_seguimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        iniciar_seguimiento = (ImageButton)findViewById(R.id.button_iniciar_seguimiento);
        iniciar_seguimiento.setOnClickListener(this);

        entrenar_app = (Button)findViewById(R.id.button_entrenar_app);
        entrenar_app.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(iniciar_seguimiento)){
            Intent siguiente = new Intent();
            siguiente.setComponent(new ComponentName(this, SedentaryTrackerActivity.class));
            startActivity(siguiente);
        }else{
            if(v.equals(entrenar_app)){
                Intent siguiente = new Intent();
                siguiente.setComponent(new ComponentName(this, MainActivity.class));
                startActivity(siguiente);
            }
        }

    }
}
