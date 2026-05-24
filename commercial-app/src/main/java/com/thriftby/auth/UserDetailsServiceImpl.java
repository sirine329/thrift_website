package com.thriftby.auth;

import com.thriftby.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Implements Spring Security's UserDetailsService.
 * Used by the AuthenticationManager to load users during login
 * (UsernamePasswordAuthenticationToken flow).
 *
 * The JwtAuthFilter uses UserRepository directly for performance,
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Aucun utilisateur trouvé avec l'email: " + email));
    }
}
