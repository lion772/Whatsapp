package com.example.whatsapp.model;

import com.example.whatsapp.activity.ConfiguracoesActivity;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable { /*1ª - Criar os atributos; 2ª - Criar o Constructor "vazio"; 3ª - Criar os Getter and
                        Setter com todos os atributos */

    private String id;
    private String nome;
    private String email;
    private String senha;
    private String foto;

    public Usuario() {
    }

    public void salvar(){

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase(); // Objeto que permite salvar os dados no firebase
        firebaseRef.child("usuarios")
                .child( getId() ) // Se eu deixar só o nó usuarios, toda vez que eu criar uma nova conta, irá substituir a anteriormente criada, por isso chamei o identificador aqui
                .setValue(this); //this é o próprio objeto usuario. Como estamos passando o objeto usuário, vamos salvar todos esses atributos

    }

    public void atualizar(){

        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();

        DatabaseReference usuariosRef = database.child("usuarios").child( identificadorUsuario );

        Map<String, Object> valoresUsuario = converterParaMap();

        usuariosRef.updateChildren( valoresUsuario ); // O método vai atualizar automaticamente os valores de email, nome e foto
    }

    @Exclude
    public Map<String, Object> converterParaMap(){ //Método para converter os valores do usuario para um objeto do tipo HashMap, pois para usar o "updateChildren" nós precisamos passar para HashMap

        HashMap<String, Object> usuarioMap = new HashMap<>(); //HashMap <K,V> .. HashMap (java.util)
        usuarioMap.put("email", getEmail());
        usuarioMap.put("nome", getNome());
        usuarioMap.put("foto", getFoto());

        return usuarioMap;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Exclude
    public String getId() { // Como já chamei o id em salvar(), não preciso dele novamente
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    } // Não quero salvar a senha dos usuários no banco de Database

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
