package com.example.projetosistemas.activities;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projetosistemas.R;
import com.example.projetosistemas.models.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class TelaCadastroUsuarios extends AppCompatActivity {
    private EditText fieldLoginCadastro;
    private EditText fieldPasswordCadastro;
    private EditText fieldConfirmarPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_cadastro);

        mAuth = FirebaseAuth.getInstance();

        Button buttonCadastrarUsuario = (Button) findViewById(R.id.buttonCadastrarUsuario);

        fieldLoginCadastro = (EditText) findViewById(R.id.fieldLoginCadastro);
        fieldPasswordCadastro = (EditText) findViewById(R.id.fieldPasswordCadastro);
        fieldConfirmarPassword = (EditText) findViewById(R.id.fieldConfirmarPassword);

        buttonCadastrarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userLogin = fieldLoginCadastro.getText().toString();
                String userPassword = fieldPasswordCadastro.getText().toString();
                String userPasswordConfirm = fieldConfirmarPassword.getText().toString();

                verificarDados(userLogin, userPassword, userPasswordConfirm);
                limparCampos();
            }
        });
    }

    private boolean verificarDados(String login, String password, String confirmPassword) {
        if (!verificarCamposVazios(login, password, confirmPassword)) {
            if (verificarInternet() && verificarConfirmacaoPassword(password, confirmPassword)) {
                    Usuario usuario = new Usuario(login, password);
                    cadastrarUsuario(usuario);
                    return true;
            }
        }
        return false;
    }

    private boolean verificarCamposVazios(String login, String password, String confirmPassword) {
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getApplicationContext(),
                    "Campos obrigatórios não preenchidos!",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean verificarConfirmacaoPassword(String password, String confirmPassword) {
        if (password.equals(confirmPassword)) {
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Senhas digitadas não coincidem !", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void cadastrarUsuario(Usuario usuario) {
        if(usuario != null) {
            mAuth.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getPassword())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Usuário cadastrado com sucesso !", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();
                            }
                            else {
                                String erro = ((FirebaseAuthException)task.getException()).getErrorCode();
                                exibirErro(erro);
                            }
                        }
                    });

        }
    }

    private void exibirErro(String erro) {
        if(erro != null) {
            switch(erro) {
                case "ERROR_INVALID_EMAIL":
                    Toast.makeText(getApplicationContext(), "E-mail inválido. Tente novamente.",
                            Toast.LENGTH_SHORT).show();
                    break;

                case "ERROR_EMAIL_ALREADY_IN_USE":
                    Toast.makeText(getApplicationContext(), "E-mail digitado já está cadastrado. Tente novamente.",
                            Toast.LENGTH_SHORT).show();
                    break;

                case "ERROR_WEAK_PASSWORD":
                    Toast.makeText(getApplicationContext(), "A senha deve conter 6 caracteres ou mais. Tente novamente.",
                            Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(getApplicationContext(), "Erro ao cadastrar usuário. Tente novamente.",
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

    private void limparCampos() {
        fieldLoginCadastro.setText("");
        fieldPasswordCadastro.setText("");
        fieldConfirmarPassword.setText("");
    }

}
