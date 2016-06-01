package co.edu.unicauca.esalud.sedentarybehavior;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuEntrenamientoActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn1, btn2, btn3, btn4;

    String Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_entrenamiento);

        btn1 = (Button) findViewById(R.id.btnSentadoEscritorio);
        btn2 = (Button) findViewById(R.id.btnParadoEscritorio);
        btn3 = (Button) findViewById(R.id.btnSentadoCama);
        btn4 = (Button) findViewById(R.id.btnAcostadoCama);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);

        Id = getIntent().getStringExtra("id");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSentadoEscritorio:
                Intent intent =new Intent(this, EntrenamientoActivity.class);
                intent.putExtra("Actividad", "Sitting_in_desk");
                intent.putExtra("id", Id);
                startActivity(intent);
                break;
            case R.id.btnParadoEscritorio:
                Intent intent1 =new Intent(this, EntrenamientoActivity.class);
                intent1.putExtra("Actividad", "Standing_near_the_desk");
                intent1.putExtra("id", Id);
                startActivity(intent1);
                break;
            case R.id.btnSentadoCama:
                Intent intent2 =new Intent(this, EntrenamientoActivity.class);
                intent2.putExtra("Actividad", "Sitting_in_bed");
                intent2.putExtra("id", Id);
                startActivity(intent2);
                break;
            case R.id.btnAcostadoCama:
                Intent intent3 =new Intent(this, EntrenamientoActivity.class);
                intent3.putExtra("Actividad", "Lying_in_bed");
                intent3.putExtra("id", Id);
                startActivity(intent3);
                break;
        }

    }
}
