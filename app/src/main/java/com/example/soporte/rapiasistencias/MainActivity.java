package com.example.soporte.rapiasistencias;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button Cli, Asi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cli = (Button) findViewById(R.id.Usuario);
        Asi = (Button) findViewById(R.id.Asistente);

        Cli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UsuariosLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        Asi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AsistentesLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

    }
}
