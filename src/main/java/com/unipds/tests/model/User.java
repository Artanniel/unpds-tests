package com.unipds.tests.model;

/**
 * Modelo de Dados (Model) utilizado para representar um Usuário em testes e operações.
 * Parte do conceito de Test Data Factory.
 */
public class User {

    private String nome;
    private String email;
    private String senha;
    private String endereco;

    public User() {
    }

    public User(String nome, String email, String senha, String endereco) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.endereco = endereco;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public static class UserBuilder {
        private String nome;
        private String email;
        private String senha;
        private String endereco;

        public UserBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder senha(String senha) {
            this.senha = senha;
            return this;
        }

        public UserBuilder endereco(String endereco) {
            this.endereco = endereco;
            return this;
        }

        public User build() {
            return new User(nome, email, senha, endereco);
        }
    }
}
