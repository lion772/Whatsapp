package com.example.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.adapter.MensagensAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Grupo;
import com.example.whatsapp.model.Mensagem;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private EditText editMensagem;
    private ImageView imageCamera;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

    //Identificador usuarios remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private Grupo grupo;

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();

    private static final int SELECAO_CAMERA = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configuracoes iniciais
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera);

        //Recuperar dados do usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente   = UsuarioFirebase.getDadosUsuarioLogado();

        //Recuperar dados do usuario destinatario
        Bundle bundle = getIntent().getExtras();
        if ( bundle != null ){

            if ( bundle.containsKey("chatGrupo") ){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                textViewNome.setText(grupo.getNome());

                String foto = grupo.getFoto();
                if ( foto != null ){

                    Uri url = Uri.parse( foto );
                    Glide.with(ChatActivity.this)
                            .load( url )
                            .into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

            }else {

                /*******Conversa convencional********/
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNome.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();
                if ( foto != null ){

                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this)
                            .load( url )
                            .into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

                //Recuperar dados usuarios destinatario
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
                /***************/
            }
        }

        //Configurar adapter
        adapter = new MensagensAdapter( mensagens, getApplicationContext() );

        //Configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize( true ); ; //serve para otimizar o recyclerView, dizendo que terá um tamanho fixo
        recyclerMensagens.setAdapter( adapter );


        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child( idUsuarioRemetente )
                .child( idUsuarioDestinatario );

        //evento de clique na camera
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if ( i.resolveActivity(getPackageManager()) != null ){ // Testamos caso seja possível resolver essa intent (abrir a câmera), usaremos a startActivityForResult, caso o usuário não tenha câmera no celular dele ou algo do tipo

                    startActivityForResult(i, SELECAO_CAMERA);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK  ){

            Bitmap imagem = null;

            try {

                switch ( requestCode ){

                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data"); // retorna um object, que será configurado para Bitmap com a cast: (Bitmap), e armazenado em imagem
                        break;
                }
                if ( imagem != null ){

                    //Recuperar dados da imagem para o Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos );
                    byte[] dadosImagem = baos.toByteArray();

                    //criar nome da imagem
                    String nomeImagem = UUID.randomUUID().toString(); //Cria identificadores baseados na data, hora, minuto e segundo

                    //Configurar referencia do firebase
                    StorageReference imagemRef = storage.child("imagens")
                            .child("fotos")
                            .child( idUsuarioRemetente )
                            .child( nomeImagem );

                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Erro", "Erro ao fazer upload da imagem" );
                            Toast.makeText(ChatActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                            if ( usuarioDestinatario != null ){//Mensagem normal

                                Mensagem mensagem = new Mensagem();
                                mensagem.setIdUsuario( idUsuarioRemetente );
                                mensagem.setMensagem("imagem.jpeg");
                                mensagem.setImagem( downloadUrl );

                                //Salvar mensagem para o remetente
                                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                                //Salvar mensagem para o destinatario
                                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                            }else {//Mensagem em grupo

                                for ( Usuario membro: grupo.getMembros() ){

                                    String idRemetenteGrupo = Base64Custom.codificarBase64( membro.getEmail() );
                                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                    Mensagem mensagem = new Mensagem();
                                    mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                                    mensagem.setMensagem("imagem.jpeg");
                                    mensagem.setImagem( downloadUrl );
                                    mensagem.setNome(usuarioRemetente.getNome());

                                    //Salvar mensagem para o membro
                                    salvarMensagem( idRemetenteGrupo, idUsuarioDestinatario, mensagem ); //idUsuarioDestinatario é o próprio grupo

                                    //Salvar conversa
                                    salvarConversa( idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true );
                                }
                            }

                            Toast.makeText(ChatActivity.this, "Sucesso ao enviar mensagem!", Toast.LENGTH_SHORT).show();

                        }
                    });

                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }



    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if ( !textoMensagem.isEmpty() ){

            if ( usuarioDestinatario != null ){

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario( idUsuarioRemetente );
                mensagem.setMensagem( textoMensagem );

                //Salvar mensagem para o remetente
                salvarMensagem( idUsuarioRemetente, idUsuarioDestinatario, mensagem );

                //Salvar mensagem para o destinatario
                salvarMensagem( idUsuarioDestinatario, idUsuarioRemetente, mensagem );

                //Salvar conversa para o remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false );

                //Salvar conversa para o destinatario
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false );

            }else{

                for ( Usuario membro: grupo.getMembros() ){

                    String idRemetenteGrupo = Base64Custom.codificarBase64( membro.getEmail() );
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                    mensagem.setMensagem( textoMensagem );
                    mensagem.setNome(usuarioRemetente.getNome());

                    //Salvar mensagem para o membro
                    salvarMensagem( idRemetenteGrupo, idUsuarioDestinatario, mensagem ); //idUsuarioDestinatario é o próprio grupo

                    //Salvar conversa
                    salvarConversa( idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true );
                }
            }





        }else{
            Toast.makeText(ChatActivity.this, "Digite uma mensagem para enviar!", Toast.LENGTH_SHORT ).show();
        }

    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem msg, boolean isGroup){

        //Salvar conversa remetente
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente( idRemetente );
        conversaRemetente.setIdDestinatario( idDestinatario );
        conversaRemetente.setUltimaMensagem( msg.getMensagem() );

        if( isGroup ){//Conversa de grupo

            conversaRemetente.setGrupo( grupo );
            conversaRemetente.setIsGroup("true");

        }else {//Conversa normal

            conversaRemetente.setUsuarioExibicao( usuarioExibicao );
            conversaRemetente.setIsGroup("false");
        }

        conversaRemetente.salvar();
    }


    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child( idRemetente )
                .child( idDestinatario )
                .push()
                .setValue( msg );

        //limpar o texto
        editMensagem.setText("");


    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener( childEventListenerMensagens );
    }

    private void recuperarMensagens(){

        mensagens.clear();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Mensagem mensagem = dataSnapshot.getValue( Mensagem.class );
                mensagens.add( mensagem );
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
