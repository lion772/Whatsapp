package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);

    }

    public void logarUsuario(Usuario usuario){

        autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())  /*2ª - Poderia criar um método validarLogin() se quisesse*/
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            abrirTelaPrincipal();

                        } else {
                            String excecao = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                excecao = "Usuário não está cadastrado";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                excecao = "E-mail e senha não correspondem a um usuário cadastrado";
                            } catch (Exception e) {
                                excecao = "Erro ao cadastrar usuário" + e.getMessage();
                                e.printStackTrace();
                            }
                            Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                        }


                    }
                });

    }

    public void validarAutenticacaoUsuario(View view){ //1ª

        //Recuperar o que o usuário escreveu
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        //Validar se os campos foram preenchidos
        if ( !textoEmail.isEmpty() ) { // Verifica email: Se o textoEmail não estiver vazio
            if ( !textoSenha.isEmpty() ) { // Verifica senha: Se o textoSenha não estiver vazio

                Usuario usuario = new Usuario(); //Colocando o private Usuario usuario como atributo é muito mais simples, assim não precisaria colocar o parâmetro em validarLogin e tipificá-lo nesse método
                usuario.setEmail( textoEmail );
                usuario.setSenha( textoSenha );

                logarUsuario( usuario );

            }else {
                Toast.makeText(LoginActivity.this, "Preencha a senha!" , Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(LoginActivity.this, "Preencha o email!" , Toast.LENGTH_LONG).show();
        }


    }

    @Override
    protected void onStart() { //4ª
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null) {
            abrirTelaPrincipal();

        }
    }

    public void abrirTelaCadastro(View view){

        Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity( intent );

    }

    public void abrirTelaPrincipal() { //3ª

        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }
}
