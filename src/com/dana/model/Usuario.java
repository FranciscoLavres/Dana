package com.dana.model;

import com.dana.exception.CadastroInvalidoException;
import com.dana.util.SenhaUtil;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Classe abstrata: não existe "usuário genérico", só Voluntario, Instituicao ou Parceiro.
 * Guarda o que é comum aos três (dados de acesso) e força cada filho a dizer seu tipo e documento.
 * O construtor valida os dados: não dá para criar um usuário inválido.
 */
public abstract class Usuario extends Entidade implements Autenticavel {

    private static final long serialVersionUID = 1L;
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    public static final int TAMANHO_MINIMO_SENHA = 4;

    private final String nome;
    private final String email;
    private final String senhaHash; // nunca guardamos a senha pura

    protected Usuario(String nome, String email, String senha) throws CadastroInvalidoException {
        super(Usuario.class);
        if (nome == null || nome.isBlank()) {
            throw new CadastroInvalidoException("O nome é obrigatório.");
        }
        if (email == null || !FORMATO_EMAIL.matcher(email.trim()).matches()) {
            throw new CadastroInvalidoException("E-mail inválido.");
        }
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new CadastroInvalidoException(
                    "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
        this.nome = nome.trim();
        this.email = normalizarEmail(email);
        this.senhaHash = SenhaUtil.gerarHash(senha);
    }

    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean autenticar(String senha) {
        return senha != null && SenhaUtil.verificar(senha, senhaHash);
    }

    /** Cada subclasse informa o seu tipo (polimorfismo). */
    public abstract String getTipo();

    /** CPF ou CNPJ, só com dígitos. */
    public abstract String getDocumento();

    /** "CPF" ou "CNPJ". */
    public abstract String getNomeDocumento();

    public abstract String getDocumentoFormatado();

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return getTipo() + " #" + getId() + " - " + nome;
    }
}
