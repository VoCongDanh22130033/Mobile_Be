package com.shopsense.security;

import com.shopsense.dao.CustomerDA;
import com.shopsense.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    CustomerDA customerDA;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerDA.findByEmail(email.trim());

        if (customer == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return customer;
    }
}
