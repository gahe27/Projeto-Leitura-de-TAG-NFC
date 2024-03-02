package com.example.controlbus.passageiro;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Passageiro {
    private String id;
    private String nome;
    private String sobrenome;
    private String telefone;
    public void salvar(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("passageiros");
        reference.child(getId()).setValue(this);
    }
    public String getDados(){
        String dados = getNome() + " " + getSobrenome() + " - " + getTelefone();
        return dados;
    }
    public Passageiro() {
    }
    public Passageiro(String id, String nome, String sobrenome, String telefone) {
        this.id = id;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.telefone = telefone;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getSobrenome() {
        return sobrenome;
    }
    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
