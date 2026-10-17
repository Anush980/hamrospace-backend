package com.anush.hamrospace.core.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.anush.hamrospace.core.security.user.UserPrincipal;
import com.anush.hamrospace.user.entity.User;
import com.anush.hamrospace.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
        .orElseThrow(()->new UsernameNotFoundException("User not found "+email));

       return new UserPrincipal(user);
    }
    
}
