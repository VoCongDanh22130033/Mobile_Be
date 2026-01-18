package com.shopsense.security;

import com.shopsense.dao.AdminDA;
import com.shopsense.dao.CustomerDA;
import com.shopsense.dao.SellerDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    @Lazy
    AdminDA adminDA;

    @Autowired
    @Lazy
    CustomerDA customerDA;

    @Autowired
    @Lazy
    SellerDA sellerDA;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String emailTrimmed = email == null ? "" : email.trim();
        UserDetails user = null;

        // Tìm trong admins trước
        try {
            user = adminDA.findByEmail(emailTrimmed);
        } catch (Exception ignored) {
            // Nếu không tìm thấy trong admins, tiếp tục tìm trong customers
        }

        // Nếu không tìm thấy trong admins, tìm trong customers
        if (user == null) {
            try {
                user = customerDA.findByEmail(emailTrimmed);
            } catch (Exception ignored) {
                // Nếu không tìm thấy trong customers, tiếp tục tìm trong sellers
            }
        }

        // Nếu không tìm thấy trong customers, tìm trong sellers
        if (user == null) {
            try {
                user = sellerDA.findByEmail(emailTrimmed);
            } catch (Exception ignored) {
                // Nếu không tìm thấy, sẽ throw exception ở dưới
            }
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return user;
    }
}
