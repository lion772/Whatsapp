package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.fragments.ContatosFragment;
import com.example.whatsapp.fragments.ConversasFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal); //5ª - Appcompat.widget
        toolbar.setTitle("Whatsapp");
        setSupportActionBar( toolbar ); // Para que sua toolbar funcione para versões anteriores do android

        // Configurar abas
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter( // 9ª - criar abas: pegue as dependencias da biblioteca no Github
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("Conversas", ConversasFragment.class) //criar 2 fragments: de conversa e de contato
                .add("Contatos", ContatosFragment.class)
                .create());

        final ViewPager viewPager = findViewById(R.id.viewPager); // 10ª
        viewPager.setAdapter( adapter ); // 11ª - ir para toolbar.xml

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);

        //Configuração do searchView
        searchView = findViewById(R.id.materialSearchPrincipal);

        //Listener para o searchView
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

                ConversasFragment fragment = (ConversasFragment) adapter.getPage(0);
                fragment.recarregarConversas();

            }
        });

        //Listener para caixa de texto
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                /* Verifica se está pesquisando Conversas ou Contatos
                * a partir da tab que está ativa */
                switch( viewPager.getCurrentItem() ){

                    case 0:
                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(0); // Agora sim, através de MainActivity, consigo acessar os métodos de ConversasFragment

                        if ( newText != null && !newText.isEmpty() ){
                            conversasFragment.pesquisarConversas( newText.toLowerCase() );
                        }else{
                            conversasFragment.recarregarConversas();
                        }
                        break;
                    case 1:
                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(1);

                        if ( newText != null && !newText.isEmpty() ){
                            contatosFragment.pesquisarContatos( newText.toLowerCase() );
                        }else{
                            contatosFragment.recarregarContatos();
                        }

                        break;

                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // 6ª - Criar o menu: antes, crie o menu_main.xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //Configurar botao de pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa);
        searchView.setMenuItem( item );


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch ( item.getItemId() ) { // 7ª - Ao apertar o "sair" no menu, chamaremos esse método

            case R.id.menuSair:
                deslogarUsuario();
                finish();
                break;

            case R.id.menuConfiguracoes: // 12ª
                startActivity(new Intent(MainActivity.this, ConfiguracoesActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){ // 8ª

        try {
            autenticacao.signOut();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
