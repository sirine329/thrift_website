package com.thriftby.service;

import com.thriftby.entity.Role;
import com.thriftby.entity.User;
import com.thriftby.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));
    }

    // ---- CRUD ----
    @Transactional(readOnly = true)
    public List<User> findAll() { return userRepository.findAll(); }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable: " + id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable: " + email));
    }

    public User inscrire(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER); // Par défaut : utilisateur normal
        user.setActif(true);
        return userRepository.save(user);
    }

    public User save(User user) {
        if (userRepository.existsByEmail(user.getEmail()) && user.getId() == null) {
            throw new RuntimeException("Email déjà utilisé: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User update(User user) {
        User existing = findById(user.getId());
        existing.setNom(user.getNom());
        existing.setPrenom(user.getPrenom());
        existing.setTelephone(user.getTelephone());
        existing.setVille(user.getVille());
        existing.setBio(user.getBio());
        existing.setRole(user.getRole());
        existing.setActif(user.isActif());
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(existing);
    }

    public void updateProfil(User current, User data) {
        current.setNom(data.getNom());
        current.setPrenom(data.getPrenom());
        current.setTelephone(data.getTelephone());
        current.setVille(data.getVille());
        current.setBio(data.getBio());
        userRepository.save(current);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public void toggleActif(Long id) {
        User user = findById(id);
        user.setActif(!user.isActif());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public List<User> search(String query) {
        return userRepository.search(query);
    }

    // Stats
    public long countTotal()   { return userRepository.count(); }
    public long countActifs()  { return userRepository.countByActifTrue(); }
    public long countAdmins()  { return userRepository.countByRole(Role.ADMIN); }
    public long countUsers()   { return userRepository.countByRole(Role.USER); }
}
