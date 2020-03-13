package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Permissao;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfiguracoesActivity extends AppCompatActivity {

    // solicitar aqui as permissões aos usuários, após colocá-las em AndroidManifest
    private String[] permisssoesNecessarias = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE, //Manifest (Android)
            Manifest.permission.CAMERA
    };

    private ImageButton imageButtonCamera, imageButtonGaleria; // Configurar os imageButtons da foto de perfil
    private static final int SELECAO_CAMERA = 100; //Ficar gravando todos os requestCodes não é negócio, para isso vamos definir atributos estáticos
    private static final int SELECAO_GALERIA = 200;
    private CircleImageView circleImageViewPerfil;
    private EditText editPerfilNome;
    private ImageView imageAtualizarNome;
    private StorageReference storageReference;
    private String identificadorUsuario;
    private Usuario usarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);


        //Configurações iniciais
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        usarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        //Validar permissões - Criar class Permissao
        Permissao.validarPermissoes( permisssoesNecessarias, this, 1);

        imageButtonCamera = findViewById(R.id.imageButtonCamera);
        imageButtonGaleria = findViewById(R.id.imageButtonGaleria);
        circleImageViewPerfil = findViewById(R.id.circleImageViewFotoPerfil);
        editPerfilNome = findViewById(R.id.editPerfilNome);
        imageAtualizarNome = findViewById(R.id.imageAtualizarNome);

        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //Definir recursos em AndroidManifest

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if ( i.resolveActivity(getPackageManager()) != null ){ // Testamos caso seja possível resolver essa intent (abrir a câmera), usaremos a startActivityForResult, caso o usuário não tenha câmera no celular dele ou algo do tipo

                    startActivityForResult(i, SELECAO_CAMERA);
                }

            }
        });

        imageButtonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if ( i.resolveActivity(getPackageManager()) != null ){ // Testamos caso seja possível retornar o dado, seja em câmera ou galeria

                    startActivityForResult(i, SELECAO_GALERIA);
                }

            }
        });

        imageAtualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nome = editPerfilNome.getText().toString();
                boolean retorno = UsuarioFirebase.atualizarNomeUsuario( nome );

                if ( retorno  ){

                    usarioLogado.setNome( nome );
                    usarioLogado.atualizar();

                    Toast.makeText(ConfiguracoesActivity.this, "Nome alterado com sucesso", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbarPrincipal); // 13ª - exibir a Toolbar em activity_configuracoes
        toolbar.setTitle("Configurações");
        setSupportActionBar( toolbar );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //14ª - Configurar o botão "voltar": O metodo getSupport retorna o objeto que nos permite alterar essa actionBar / toolBar, tem suporte às versões anteriores do android. O método setDisplay tem que ser usado no objeto retornado pelo método getSupport
                                                               //15ª -> ir para AndroidManifest

        // Recuperar dados do usuário
        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        if ( url != null ){ //temos uma imagem se for diferente de nulo

            Glide.with(ConfiguracoesActivity.this) //Através da biblioteca glide, carregaremos a foto
                    .load( url )
                    .into( circleImageViewPerfil );

        }else{
            circleImageViewPerfil.setImageResource(R.drawable.padrao);
        }

        editPerfilNome.setText( usuario.getDisplayName() );


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // Método para retornar a imagem selecionada tanto em câmera quanto em galeria
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK  ){
            Bitmap imagem = null;

            try {

                switch ( requestCode ){

                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data"); // retorna um object, que será configurado para Bitmap com a cast: (Bitmap), e armazenado em imagem
                        break;
                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData(); //local da imagem
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }
                if ( imagem != null ){

                    circleImageViewPerfil.setImageBitmap( imagem );

                    //Recuperar dados da imagem para o Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos );
                    byte[] dadosImagem = baos.toByteArray();

                    // Salvar imagem no Firebase -Em ConfiguracaoFirebase, definir o storageReference para recuperar a instância do storage, para salvar a imagem lá dentro
                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("perfil")
                            //.child( identificadorUsuario )
                            .child(identificadorUsuario + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() { //fazer testes para ver se deu certo
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(ConfiguracoesActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(ConfiguracoesActivity.this, "Sucesso ao fazer upload da imagem!", Toast.LENGTH_SHORT).show();

                            Uri url = taskSnapshot.getDownloadUrl(); //Ao alterarmos a foto de perfil, tanto usando a câmera quanto trocando na galeria, irá salvar no UsuarioFirebase
                            atualizaFotoUsuario( url );
                        }
                    });

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void atualizaFotoUsuario(Uri url){
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if ( retorno ){

            usarioLogado.setFoto( url.toString() );
            usarioLogado.atualizar();
            Toast.makeText(ConfiguracoesActivity.this, "sua fota foi alterada", Toast.LENGTH_SHORT).show();
        }else{

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for ( int permissaoResultado : grantResults ) {
            if ( permissaoResultado == PackageManager.PERMISSION_DENIED){ //Caso o usuário negue a permissão, aparecerá uma mensagem dizendo que não será possível mexer no app, e finalizará a activity

                alertaValidacaoPermissao(); // Esse método exibirá a mensagem de alerta quando o usuário negar
            }
        }
    }

    private void alertaValidacaoPermissao(){ // Método para exibir o alerta

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
}
