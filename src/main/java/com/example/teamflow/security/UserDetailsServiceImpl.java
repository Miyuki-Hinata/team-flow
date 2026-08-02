package com.example.teamflow.security;

import com.example.teamflow.entity.User;
import com.example.teamflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 論理削除済みを除外して引く。このクラスはログイン時と毎リクエストの JWT 検証時の
        // 両方から呼ばれる「認証の入口」なので、ここで弾けば削除済みアカウントは
        // ログインもできず、発行済みトークンも次のリクエストで無効になる。
        //
        // なお、ここで投げた UsernameNotFoundException は Spring Security が
        // BadCredentialsException に差し替えて返すため、「そのアカウントは削除済み」という
        // 情報は外部に漏れない（存在しないユーザーと同じ応答になる）。
        User user = userRepository.findByLoginIdAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getLevel() == 2) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getLoginId(),
                user.getPassword(),
                authorities
        );
    }
}