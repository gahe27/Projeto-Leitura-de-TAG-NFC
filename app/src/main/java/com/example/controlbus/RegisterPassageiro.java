package com.example.controlbus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.nfc.NfcEvent;

import com.example.controlbus.administrador.Administrador;
import com.example.controlbus.passageiro.Passageiro;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RegisterPassageiro extends AppCompatActivity {

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("passageiros");
    private FirebaseAuth mAuth;
    private Button btn_registrar_register;
    private Button btn_Cancelar;
    private EditText edt_nome_register;
    private EditText edt_sobrenome_register;
    private EditText edt_telefone_register;
    private ProgressBar ProgressBar_register_passageiro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_passageiro);

        mAuth = FirebaseAuth.getInstance();

        btn_registrar_register = findViewById(R.id.btn_registrar_register);
        btn_Cancelar = findViewById(R.id.btn_cancelar);
        edt_nome_register = findViewById(R.id.edt_nome_register);
        edt_sobrenome_register = findViewById(R.id.edt_sobrenome_register);
        edt_telefone_register = findViewById(R.id.edt_telefone_register);
        ProgressBar_register_passageiro = findViewById(R.id.ProgressBar_register_passageiro);

        btn_registrar_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Passageiro psg = new Passageiro();
                psg.setNome(edt_nome_register.getText().toString());
                psg.setSobrenome(edt_sobrenome_register.getText().toString());
                psg.setTelefone(edt_telefone_register.getText().toString());

                if(!TextUtils.isEmpty(psg.getNome()) && !TextUtils.isEmpty(psg.getSobrenome()) && !TextUtils.isEmpty(psg.getTelefone())){
                        ProgressBar_register_passageiro.setVisibility(View.VISIBLE);
                        psg.setId(reference.push().getKey());
                        psg.salvar();
                        abrirTelaPrincipal();
                }
                    else{
                    Toast.makeText(RegisterPassageiro.this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_Cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterPassageiro.this, Principal.class);
                startActivity(intent);
            }
        });
    }

    private void abrirTelaPrincipal() {
        Intent intent = new Intent(RegisterPassageiro.this, Principal.class);
        startActivity(intent);
        finish();
    }
}