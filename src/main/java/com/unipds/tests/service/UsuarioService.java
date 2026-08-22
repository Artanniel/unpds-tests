package com.unipds.tests.service;

import com.unipds.tests.model.Usuario;
import com.unipds.tests.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario salvarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public boolean podeVotar(int idade) {
        boolean retorno = false;

        if (idade >= 18 || idade <= 70) {
            return true;
        }
        return retorno;
    }
}
