package com.example.whatsapp.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.example.whatsapp.R;
import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.activity.ConfiguracoesActivity;
import com.example.whatsapp.activity.GrupoActivity;
import com.example.whatsapp.adapter.ContatosAdapter;
import com.example.whatsapp.adapter.ConversasAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;


    public ContatosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        //Configurações iniciais
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configurar adapter
        adapter = new ContatosAdapter(listaContatos, getActivity());

        //Configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity()); // Como estamos dentro de um fragment, pegaremos o context usando o método getActivity, passando a activity principal (seu contexto) como parâmetro
        recyclerViewListaContatos.setLayoutManager( layoutManager );
        recyclerViewListaContatos.setHasFixedSize( true ); ; //serve para otimizar o recyclerView, dizendo que terá um tamanho fixo
        recyclerViewListaContatos.setAdapter( adapter );

        //Configurar evento de clique no recyclerView
        recyclerViewListaContatos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaContatos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Usuario> listaUsuariosAtualizada = adapter.getContatos();

                                Usuario usuarioSelecionado = listaUsuariosAtualizada.get( position );
                                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                                if ( cabecalho ){
                                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                                    startActivity( i );

                                }else {
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", usuarioSelecionado ); // Implementar Serializable em Usuario, para passar objetos entre activities
                                    startActivity( i );
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        adicionarMenuNovoGrupo();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener( valueEventListenerContatos );
    }

    public void recuperarContatos(){

        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //limparListaContatos();

                for( DataSnapshot dados: dataSnapshot.getChildren()  ){ //O objeto dados percorre todos os usuarios do firebase

                    Usuario usuario = dados.getValue( Usuario.class);

                    String emailUsuarioAtual =  usuarioAtual.getEmail();
                    if (  !emailUsuarioAtual.equals( usuario.getEmail() ) ){ // usuário do Firebase tem que ser diferente do usuário do Database
                        listaContatos.add( usuario );
                    }


                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void limparListaContatos(){

        listaContatos.clear();
        adicionarMenuNovoGrupo();
    }

    public void adicionarMenuNovoGrupo(){

        /* Define usuario com e-mail vazio
         * em caso de e-mail vazio, o usuário será utilizado como
         * cabeçalho, exibindo novo grupo */
        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo grupo");
        itemGrupo.setEmail("");

        listaContatos.add( itemGrupo );
    }

    public void pesquisarContatos(String texto){

        List<Usuario> listaContatosBusca = new ArrayList<>();

        for ( Usuario usuario: listaContatos ){

            String nome = usuario.getNome().toLowerCase();
            if ( nome.contains( texto ) ){
                listaContatosBusca.add( usuario );
            }
        }

        adapter = new ContatosAdapter(listaContatosBusca, getActivity());
        recyclerViewListaContatos.setAdapter( adapter );
        adapter.notifyDataSetChanged();


    }


    public void recarregarContatos(){

        adapter = new ContatosAdapter(listaContatos, getActivity());
        recyclerViewListaContatos.setAdapter( adapter );
        adapter.notifyDataSetChanged();

    }

}
