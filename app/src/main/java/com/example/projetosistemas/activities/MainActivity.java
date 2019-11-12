package com.example.projetosistemas.activities;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projetosistemas.R;
import com.example.projetosistemas.models.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText fieldLogin;
    private EditText fieldPassword;

    private Button buttonLogin;
    private Button buttonCadastrar;

    private TextView labelEsqueciSenha;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        labelEsqueciSenha = (TextView) findViewById(R.id.labelEsqueciSenha);

        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonCadastrar = (Button) findViewById(R.id.buttonCadastrar);

        fieldLogin = (EditText) findViewById(R.id.fieldLogin);
        fieldPassword = (EditText) findViewById(R.id.fieldPassword);

        limparCampos();

        labelEsqueciSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TelaRecuperacaoSenha.class);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userLogin = fieldLogin.getText().toString();
                String userPassword = fieldPassword.getText().toString();

                if (verificarInternet()) {
                    verificarDados(userLogin, userPassword);
                }
            }
        });

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TelaCadastroUsuarios.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRestart() {
        super.onRestart();
        limparCampos();
    }

    private boolean verificarDados(String login, String password) {
        if (!verificarCamposVazios(login, password)) {
            return verificarUsuario(login, password);
        }
        return false;
    }

    private boolean verificarCamposVazios(String login, String password) {
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                    "Campos obrigatórios não preenchidos!",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean verificarUsuario(String login, String password) {
        Usuario usuario = new Usuario(login, password);
        realizarLogin(usuario);
        limparCampos();
        return true;
    }

    private void limparCampos() {
        fieldLogin.setText("");
        fieldPassword.setText("");
    }

    private void realizarLogin(Usuario usuario) {
        mAuth.signInWithEmailAndPassword(usuario.getEmail(), usuario.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login realizado com sucesso !",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(getApplicationContext(), TelaMapaLocal.class);
                            startActivity(intent);
                        } else {
                            String erro = ((FirebaseAuthException) task.getException()).getErrorCode();
                            exibirErro(erro);
                        }
                    }
                });
    }

    private void exibirErro(String erro) {
        if (erro != null) {
            switch (erro) {
                case "ERROR_INVALID_EMAIL":
                    Toast.makeText(getApplicationContext(), "E-mail inválido. Tente novamente.",
                            Toast.LENGTH_SHORT).show();
                    break;

                case "ERROR_USER_NOT_FOUND":
                    Toast.makeText(getApplicationContext(), "E-mail não encontrado. Tente novamente.",
                            Toast.LENGTH_SHORT).show();
                    break;

                case "ERROR_WRONG_PASSWORD":
                    Toast.makeText(getApplicationContext(), "Senha inválida. Tente novamente.",
                            Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(getApplicationContext(), "Erro ao realizar login. Tente novamente.",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean verificarInternet() {
        ConnectivityManager conexao = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo informacao = conexao.getActiveNetworkInfo();

        if(informacao != null && informacao.isConnected()) {
            return true;
        }
        else {
            Toast.makeText(getApplicationContext(), "Erro. Verifique sua conexão com a internet.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
