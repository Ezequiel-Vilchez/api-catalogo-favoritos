package com.example.demo.repository;

import com.example.demo.model.Usuario;
import com.example.demo.model.UsuarioEntity;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entity = new UsuarioEntity(
            usuario.id(),
            usuario.email(),
            usuario.passwordHash(),
            usuario.rol()
        );
        UsuarioEntity saved = jpaRepository.save(entity);
        return new Usuario(saved.getId(), saved.getEmail(), saved.getPasswordHash(), saved.getRol());
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(e -> new Usuario(e.getId(), e.getEmail(), e.getPasswordHash(), e.getRol()));
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
