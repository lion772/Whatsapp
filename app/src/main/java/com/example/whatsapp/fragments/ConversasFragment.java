package com.example.whatsapp.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatsapp.R;
import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.adapter.ContatosAdapter;
import com.example.whatsapp.adapter.ConversasAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private ArrayList<Conversa> listaConversas = new ArrayList<>();
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;
    private FirebaseUser usuarioAtual;


    public ConversasFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        //Configurações iniciais
        recyclerViewConversas = view.findViewById(R.id.recyclerListaConversas);

        //Configurar adapter
        adapter = new ConversasAdapter(listaConversas, getActivity());

        //Configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity()); // Como estamos dentro de um fragment, pegaremos o context usando o método getActivity, passando a activity principal (seu contexto) como parâmetro
        recyclerViewConversas.setLayoutManager( layoutManager );
        recyclerViewConversas.setHasFixedSize( true ); ; //serve para otimizar o recyclerView, dizendo que terá um tamanho fixo
        recyclerViewConversas.setAdapter( adapter );

        //Configurar evento de clique
        recyclerViewConversas.addOnItemTouchListener(new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Conversa> listaConversasAtualizadas = adapter.getConversas();
                                Conversa conversaSelecionada = listaConversasAtualizadas.get( position );
                                if (conversaSelecionada.getIsGroup().equals("true")){

                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatGrupo", conversaSelecionada.getGrupo() );
                                    startActivity( i );

                                }else {

                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao() );
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
                ));

        //Configurar conversasRef
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef = database.child("conversas")
                .child( identificadorUsuario );

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListenerConversas);
    }


    public void pesquisarConversas(String texto){

        List<Conversa> listaConversasBusca = new ArrayList<>();
        for ( Conversa conversa: listaConversas ){

            if ( conversa.getUsuarioExibicao() != null ){

                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if ( nome.contains( texto ) || ultimaMsg.contains( texto ) ) {

                    listaConversasBusca.add( conversa );
                }

            }else {

                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if ( nome.contains( texto ) || ultimaMsg.contains( texto ) ) {

                    listaConversasBusca.add( conversa );
                }
            }
        }

        adapter = new ConversasAdapter(listaConversasBusca, getActivity());
        recyclerViewConversas.setAdapter( adapter );
        adapter.notifyDataSetChanged();


    }


    public void recarregarConversas(){

        adapter = new ConversasAdapter(listaConversas, getActivity());
        recyclerViewConversas.setAdapter( adapter );
        adapter.notifyDataSetChanged();

    }


    public void recuperarConversas(){

        listaConversas.clear();

        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //Recuperar conversas
                Conversa conversa = dataSnapshot.getValue(Conversa.class);
                listaConversas.add( conversa );

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
