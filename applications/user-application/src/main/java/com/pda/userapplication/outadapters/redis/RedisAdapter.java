package com.pda.userapplication.outadapters.redis;

import com.pda.tofinenums.user.Job;
import com.pda.tofinenums.user.UserRole;
import com.pda.userapplication.domains.User;
import com.pda.userapplication.domains.vo.Birth;
import com.pda.userapplication.domains.vo.ImageUrl;
import com.pda.userapplication.domains.vo.Nickname;
import com.pda.userapplication.domains.vo.UserId;
import com.pda.userapplication.services.out.RefreshTokenOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisAdapter implements RefreshTokenOutputPort {
    private final RefreshInfoRepository refreshInfoRepository;

    @Override
    public void saveOnlyOneUser(User user, String refreshToken) {
        refreshInfoRepository.deleteById(user.getId().toLong());

        refreshInfoRepository.save(RefreshInfoEntity.builder()
                .id(user.getId().toLong())
                .job(user.getJob().toKorean())
                .role(user.getRole().toString())
                .birth(user.getBirth().toLocalDate())
                .nickname(user.getNickname().toString())
                .profile(user.getProfileImage().toString())
                .refreshToken(refreshToken)
            .build());
    }

    @Override
    public Optional<User> deleteByRefreshToken(String refreshToken) {
        RefreshInfoEntity refreshInfo = refreshInfoRepository.findByRefreshToken(refreshToken).orElse(null);

        if (refreshInfo == null) return Optional.empty();

        refreshInfoRepository.deleteById(refreshInfo.getId().toString());

        return Optional.of(User.builder()
                .id(UserId.of(Long.valueOf(refreshInfo.getId())))
                .job(Job.of(refreshInfo.getJob()))
                .role(UserRole.valueOf(refreshInfo.getRole()))
                .birth(Birth.of(refreshInfo.getBirth()))
                .nickname(Nickname.of(refreshInfo.getNickname()))
                .profileImage(ImageUrl.of(refreshInfo.getProfile()))
            .build());
    }
}
