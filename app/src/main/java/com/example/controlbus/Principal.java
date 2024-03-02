package com.example.controlbus;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.Tag;

import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.nfc.tech.MifareUltralight;
import android.widget.TextView;
import android.widget.Toast;

import com.example.controlbus.passageiro.Passageiro;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Principal extends AppCompatActivity {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("passageiros");
    private FirebaseAuth mAuth;
    private Button btn_logout;
    private Button btn_Adicionar;
    private Button btn_Remover;
    private ListView lv_passageiros;
    String dadosString;

    ArrayAdapter<String> adapter;
    private NfcAdapter nfcAdapter;
    Intent newIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        mAuth = FirebaseAuth.getInstance();
        btn_logout = findViewById(R.id.btn_logout);
        btn_Adicionar = findViewById(R.id.btn_Adicionar);
        btn_Remover = findViewById(R.id.btn_Remover);
        lv_passageiros = findViewById(R.id.lv_passageiros);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(Principal.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        btn_Adicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Principal.this, RegisterPassageiro.class);
                startActivity(intent);
                finish();
            }
        });

        lv_passageiros.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dadosString = (String) lv_passageiros.getItemAtPosition(i);
            }
        });

        btn_Remover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removerPassageiroManual(dadosString);
            }
        });
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String[] dados = null;
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            NdefMessage[] messages = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                messages[i] = (NdefMessage) rawMsgs[i];
            }
            if (messages.length > 0) {
                NdefRecord[] records = messages[0].getRecords();
                if (records.length > 0) {
                    for (NdefRecord record : records) {
                        byte[] payload = record.getPayload();
                        String tagData = new String(payload, Charset.forName("UTF-8"));
                        if (!TextUtils.isEmpty(tagData)) {
                            dados = tagData.split(" ");
                        }
                    }
                    if (dados != null && dados.length >= 4) {
                        String nome = dados[1];
                        String sobrenome = dados[2];
                        String telefone = dados[3];
                        Passageiro psg = new Passageiro();
                        psg.setNome(nome);
                        psg.setSobrenome(sobrenome);
                        psg.setTelefone(telefone);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("passageiros");
                        Query query = reference.orderByChild("dados").equalTo(psg.getDados());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        userSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                adapter.clear();
                                                adapter.notifyDataSetChanged();
                                                preenche_listview();
                                            }}).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                            }});}} else {
                                    psg.setId(reference.push().getKey());
                                    psg.salvar();
                                    preenche_listview();
                                }}
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }});}}}}}

    @Override
    protected void onResume() {
        super.onResume();
        // Configurar para responder a novas tags NFC
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Parar de responder a tags NFC quando a atividade estiver em segundo plano
        nfcAdapter.disableForegroundDispatch(this);
    }

    protected void onStart(){
        super.onStart();
        Intent intent = getIntent();
        FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentuser == null){
            intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }
        else{
            preenche_listview();
        }

    }

    public void preenche_listview(){
        DatabaseReference passageiros = FirebaseDatabase.getInstance().getReference().child("passageiros");

        Query dadosPassageiros = passageiros.orderByChild("nome");

        ArrayList<String> p = new ArrayList<>();

        dadosPassageiros.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                p.clear();

                for(DataSnapshot dados: snapshot.getChildren()){
                    Passageiro pas = dados.getValue(Passageiro.class);
                    p.add(pas.getDados());
                }

                //if (adapter == null) {
                    adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, p);
                    lv_passageiros.setAdapter(adapter);
                //} else {
                    adapter.notifyDataSetChanged();
               // }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Toast.makeText(Principal.this, "ERRO: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removerPassageiroManual(String itemString) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference().child("passageiros");

        Query query = itemsRef.orderByChild("dados").equalTo(itemString);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    itemSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override
                        public void onSuccess(Void aVoid) {
                            preenche_listview();
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Ocorreu um erro ao excluir o item
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Ocorreu um erro na consulta
            }
        });
    }
}