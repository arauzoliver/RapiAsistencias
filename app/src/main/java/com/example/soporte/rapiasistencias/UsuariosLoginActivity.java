package com.example.soporte.rapiasistencias;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UsuariosLoginActivity extends AppCompatActivity {

    private EditText Email, Contraseña;
    private Button Entrar, Registrarse;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios_login);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user!=null){
                    Intent intent = new Intent(UsuariosLoginActivity.this, UsuariosMapsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        Email = (EditText) findViewById(R.id.Email);
        Contraseña = (EditText) findViewById(R.id.Contraseña);

        Entrar = (Button) findViewById(R.id.Entrar);
        Registrarse = (Button) findViewById(R.id.Registrarse);

        Registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Email.getText().toString();
                final String contraseña = Contraseña.getText().toString();
                mAuth.createUserWithEmailAndPassword(email, contraseña).addOnCompleteListener(UsuariosLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) {
                            Toast.makeText(UsuariosLoginActivity.this, "Húbo un error", Toast.LENGTH_SHORT).show();
                        }else{
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Clientes").child(user_id);
                            current_user_db.setValue(true);
                        }
                    }
                });
            }
        });

        Entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = Email.getText().toString();
                final String contraseña = Contraseña.getText().toString();
                mAuth.signInWithEmailAndPassword(email, contraseña).addOnCompleteListener(UsuariosLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) {
                            Toast.makeText(UsuariosLoginActivity.this, "Húbo un error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }

}
