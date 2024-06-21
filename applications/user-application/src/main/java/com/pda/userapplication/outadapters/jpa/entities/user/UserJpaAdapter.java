package com.pda.userapplication.outadapters.jpa.entities.user;

import com.pda.exceptionhandler.exceptions.BadRequestException;
import com.pda.userapplication.domains.NormalUser;
import com.pda.userapplication.domains.User;
import com.pda.userapplication.domains.vo.TofinId;
import com.pda.userapplication.domains.vo.UserId;
import com.pda.userapplication.outadapters.jpa.mapper.UserDetailEntityMapper;
import com.pda.userapplication.outadapters.jpa.mapper.UserEntityMapper;
import com.pda.userapplication.services.out.CreateUserOutputPort;
import com.pda.userapplication.services.out.ReadNormalUserOutputPort;
import com.pda.userapplication.services.out.ReadUserOutputPort;
import com.pda.userapplication.services.out.SaveNormalUserOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserJpaAdapter implements CreateUserOutputPort, ReadUserOutputPort, SaveNormalUserOutputPort, ReadNormalUserOutputPort {
    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserEntityMapper userMapper;
    private final UserDetailEntityMapper userDetailMapper;

    @Override
    public User create(final User user) {
        return userMapper.toUser(
            userRepository.save(userMapper.toUserEntity(user)));
    }

    @Override
    public boolean isExistsByTofinId(final TofinId tofinId) {
        return userRepository.existsByTofinId(tofinId.toString());
    }

    @Override
    public User getByTofinId(TofinId tofinId) {
        return findByTofinId(tofinId).orElseThrow(
            () -> new BadRequestException("해당 아이디의 유저를 찾을 수 없습니다."));
    }

    @Override
    public Optional<User> findByTofinId(TofinId tofinId) {
        return Optional.ofNullable(
            userMapper.toUser(
                userRepository.findByTofinId(tofinId.toString())
                .orElse(null)));
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return Optional.ofNullable(
            userMapper.toUser(userRepository.findById(userId.toLong())
                .orElse(null)));
    }

    @Override
    public User getByUserId(UserId userId) {
        return findById(userId).orElseThrow(
            () -> new BadRequestException("해당 아이디의 유저를 찾을 수 없습니다."));
    }

    @Override
    public NormalUser save(NormalUser normalUser) {
        return userDetailMapper.toNormalUser(
            userDetailRepository.save(userDetailMapper
                .toUserDetailEntity(normalUser)));
    }

    private String toPublicOptions(NormalUser user) {
        String amountOption = user.isPublicAmount()?"1":"0";
        String percentOption = user.isPublicPercent()?"1":"0";

        return amountOption + percentOption;
    }

    @Override
    public boolean existsByContact(String contact) {
        return userDetailRepository.existsByContact(contact);
    }

    @Override
    public Optional<NormalUser> findByUserId(UserId userId) {
        return Optional.ofNullable(userDetailMapper.toNormalUser(
            userDetailRepository.findByUserId(userId.toLong())
                .orElse(null)));
    }
}