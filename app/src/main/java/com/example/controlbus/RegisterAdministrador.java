package com.example.controlbus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.controlbus.administrador.Administrador;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterAdministrador extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText edt_nome_register;
    private EditText edt_sobrenome_register;
    private EditText edt_email_register;
    private EditText edt_senha_register;
    private EditText edt_confirmar_senha_register;
    private Button btn_registrar_register;
    private Button btn_login_register;
    private CheckBox ckb_mostrar_senha_register;
    private ProgressBar loginProgressBar_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        edt_nome_register = findViewById(R.id.edt_nome_register);
        edt_sobrenome_register = findViewById(R.id.edt_sobrenome_register);
        edt_email_register = findViewById(R.id.edt_email_register);
        edt_senha_register = findViewById(R.id.edt_senha_register);
        edt_confirmar_senha_register = findViewById(R.id.edt_confirmar_senha_register);
        btn_registrar_register = findViewById(R.id.btn_registrar_register);
        btn_login_register = findViewById(R.id.btn_login_register);
        ckb_mostrar_senha_register = findViewById(R.id.ckb_mostrar_senha_register);
        loginProgressBar_register = findViewById(R.id.loginProgressBar_register);

        btn_registrar_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Administrador adm = new Administrador();

                adm.setEmail(edt_email_register.getText().toString());
                adm.setNome(edt_nome_register.getText().toString());
                adm.setSobrenome(edt_sobrenome_register.getText().toString());
                String registerSenha = edt_senha_register.getText().toString();
                String confirmarSenha = edt_confirmar_senha_register.getText().toString();

                if(!TextUtils.isEmpty(adm.getNome()) && !TextUtils.isEmpty(adm.getSobrenome()) && !TextUtils.isEmpty(adm.getEmail()) &&
                        !TextUtils.isEmpty(registerSenha) && !TextUtils.isEmpty(confirmarSenha)){
                    if(registerSenha.equals(confirmarSenha)){
                        loginProgressBar_register.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(adm.getEmail(), registerSenha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    adm.setId(mAuth.getUid());
                                    adm.salvar();
                                    abrirTelaPrincipal();
                                }else{
                                    String error;
                                    try {
                                        throw task.getException();
                                    }catch (FirebaseAuthWeakPasswordException e){
                                        error = "A senha deve conter no mínimo 6 caracteres.";
                                    }catch (FirebaseAuthInvalidCredentialsException e){
                                        error = "E-mail inválido.";
                                    }catch (FirebaseAuthUserCollisionException e){
                                        error = "E-mail já cadastrado.";
                                    }catch (Exception e){
                                        error = "Erro ao efetuar o cadastro.";
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(RegisterAdministrador.this, error, Toast.LENGTH_SHORT).show();
                                }
                                loginProgressBar_register.setVisibility(View.INVISIBLE);
                            }
                        });
                    }else{
                        Toast.makeText(RegisterAdministrador.this, "A senha deve ser a mesma em ambos os campos!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegisterAdministrador.this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ckb_mostrar_senha_register.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    edt_senha_register.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    edt_confirmar_senha_register.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    edt_senha_register.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    edt_confirmar_senha_register.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        btn_login_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterAdministrador.this, Login.class);
                startActivity(intent);
                //finish();
            }
        });
    }

    private void abrirTelaPrincipal() {
        Intent intent = new Intent(RegisterAdministrador.this, Principal.class);
        startActivity(intent);
        //finish();
    }

}
