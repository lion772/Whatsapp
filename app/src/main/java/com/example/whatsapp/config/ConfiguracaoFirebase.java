package com.example.whatsapp.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {

    private static DatabaseReference database; //É estático porque não vamos instanciar essa classe, só vamos utilizar esse método
    private static FirebaseAuth auth;
    private static StorageReference storage;

    //Retorna a instância do FirebaseDatabase
    public static DatabaseReference getFirebaseDatabase(){
        if ( database == null ) {
            database = FirebaseDatabase.getInstance().getReference(); //Permite gerenciar o banco de dados
        }
        return database;
    }

    //Retorna a instância do FirebaseAuth
    public static FirebaseAuth getFirebaseAutenticacao(){
        if ( auth == null ) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;

    }

    public static StorageReference getFirebaseStorage(){
        if ( storage == null ) {
            storage = FirebaseStorage.getInstance().getReference(); //com o getReference, terá que definir de FirebaseStorage para StorageReference
        }
        return storage;
    }
}
