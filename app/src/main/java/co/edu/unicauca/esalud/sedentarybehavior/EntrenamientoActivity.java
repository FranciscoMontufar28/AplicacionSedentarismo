package co.edu.unicauca.esalud.sedentarybehavior;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import co.edu.unicauca.esalud.sedentarybehavior.Background.Contador;

public class EntrenamientoActivity extends AppCompatActivity implements View.OnClickListener {

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if (msg.what == Contador.SECOND)
            {
                int second = msg.arg1;
                reloj.setText(""+second);
            }else {
                reloj.setText("00:00");
            }
        }

    };

    TextView reloj;
    Button btnIniciar, btnParar;
    Contador thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento);
        thread = null;
        reloj = (TextView) findViewById(R.id.RelojConteo);

        btnIniciar = (Button) findViewById(R.id.BtnIniciarEntrenamieto);
        btnParar = (Button) findViewById(R.id.BtnPararEntrenamiento);

        btnIniciar.setOnClickListener(this);
        btnParar.setOnClickListener(this);

        if (thread == null){
            thread = new Contador(handler);
            thread.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BtnIniciarEntrenamieto:
                Iniciar();
                break;
            case R.id.BtnPararEntrenamiento:
                Parar();
                break;
        }

    }

    private void Parar() {

    }

    private void Iniciar() {

    }
}
