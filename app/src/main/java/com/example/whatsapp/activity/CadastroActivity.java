package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoNome, campoEmail, campoSenha;
    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editPerfilNome);
        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
    }

    public void cadastrarUsuario (final Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()

        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if ( task.isSuccessful() ) { // Salvar os cadastros no Database

                    String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setId( identificadorUsuario );
                    usuario.salvar();
                    Toast.makeText(CadastroActivity.this, "Sucesso ao cadastrar usuário!", Toast.LENGTH_SHORT).show();
                    UsuarioFirebase.atualizarNomeUsuario( usuario.getNome() ); //Depois de atualizar a foto do usuário, gravando-a no UsuarioFirebase, vamos salvar o nome do usuario também
                    finish();

                }else {

                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch ( FirebaseAuthWeakPasswordException e) {
                        excecao = "Digite uma senha mais forte!";
                    } catch ( FirebaseAuthInvalidCredentialsException e) {
                        excecao = "Por favor, digite um email válido";
                    } catch ( FirebaseAuthUserCollisionException e) {
                        excecao = "Esta conta já foi cadastrada";
                    } catch ( Exception e ) {
                        excecao = "Erro ao cadastrar usuário" + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void validarCadastroUsuario(View view){

        //Recuperar textos dos campos
        String textoNome    = campoNome.getText().toString();
        String textoEmail   = campoEmail.getText().toString();
        String textoSenha   = campoSenha.getText().toString();

        if ( !textoNome.isEmpty() ) { // Verifica nome: Se o textoNome não estiver vazio
            if ( !textoEmail.isEmpty() ) { // Verifica e-mail: Se o textoEmail não estiver vazio
                if ( !textoSenha.isEmpty() ) { //Verifica senha; Se o textoSenha não estiver vazio

                    Usuario usuario = new Usuario();
                    usuario.setNome( textoNome );
                    usuario.setEmail( textoEmail );
                    usuario.setSenha( textoSenha );

                    cadastrarUsuario( usuario );

                }else {
                    Toast.makeText(CadastroActivity.this, "Preencha a senha!" , Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(CadastroActivity.this, "Preencha o email!" , Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(CadastroActivity.this, "Preencha o nome!" , Toast.LENGTH_LONG).show();
        }

    }

}
