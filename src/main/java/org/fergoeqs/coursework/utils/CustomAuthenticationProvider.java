package org.fergoeqs.coursework.utils;

import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.repositories.UserRepository;
import org.fergoeqs.coursework.security.UserDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public CustomAuthenticationProvider(UserRepository userRepository, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userRepository = userRepository;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        Collection<GrantedAuthority> authorities = user.getAuthorities();


        if (!password.equals(user.getPassword())) {
            throw new BadCredentialsException("Authentication failed.");
        }

        return new UsernamePasswordAuthenticationToken(username, password, authorities);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}