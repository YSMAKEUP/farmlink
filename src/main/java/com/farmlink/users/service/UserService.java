package com.farmlink.users.service;

import com.farmlink.users.domain.UserEntity;
import com.farmlink.users.dto.LoginRequestDto;
import com.farmlink.users.dto.LoginResponseDto;
import com.farmlink.users.dto.SignUpRequestDto;
import com.farmlink.users.dto.SignUpResponseDto;
import com.farmlink.security.JwtTokenProvider;
import com.farmlink.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        UserEntity user = UserEntity.builder()
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .farmCode(requestDto.getFarmCode())
                .farmName(requestDto.getFarmName())
                .build();

        UserEntity saved = userRepository.save(user);

        return SignUpResponseDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        UserEntity user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        return LoginResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .farmName(user.getFarmName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}