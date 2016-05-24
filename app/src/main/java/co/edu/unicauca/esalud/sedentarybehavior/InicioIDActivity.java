package co.edu.unicauca.esalud.sedentarybehavior;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class InicioIDActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_id);

        editText = (EditText) findViewById(R.id.IdusrInicio);
        button = (Button) findViewById(R.id.BtnusrInicio);

        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BtnusrInicio:
                ingresar();
                break;
        }
    }

    private void ingresar() {
        Intent intent = new Intent(this, MenuEntrenamientoActivity.class);
        String Id = editText.getText().toString();
        startActivity(intent);
    }
}
