package com.pda.userapplication.outadapters.jpa.entities.user;

import com.pda.exceptionhandler.exceptions.BadRequestException;
import com.pda.exceptionhandler.exceptions.InternalServerException;
import com.pda.userapplication.domains.UserDetail;
import com.pda.userapplication.domains.User;
import com.pda.userapplication.domains.vo.TofinId;
import com.pda.userapplication.domains.vo.UserId;
import com.pda.userapplication.outadapters.jpa.mapper.UserDetailEntityMapper;
import com.pda.userapplication.outadapters.jpa.mapper.UserEntityMapper;
import com.pda.userapplication.services.out.ReadUserDetailOutputPort;
import com.pda.userapplication.services.out.ReadUserOutputPort;
import com.pda.userapplication.services.out.SaveUserDetailOutputPort;
import com.pda.userapplication.services.out.SaveUserOutputPort;
import com.pda.userapplication.services.out.dto.req.SearchUserOutputRequest;
import com.pda.userapplication.services.out.dto.res.SearchUserPagingOutputResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailDetailJpaAdapter implements SaveUserOutputPort, ReadUserOutputPort, SaveUserDetailOutputPort, ReadUserDetailOutputPort {
    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserEntityMapper userMapper;
    private final UserDetailEntityMapper userDetailMapper;

    @Override
    public User save(final User user) {
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
    public Optional<User> findUserById(UserId userId) {
        return Optional.ofNullable(
            userMapper.toUser(userRepository.findById(userId.toLong())
                .orElse(null)));
    }

    @Override
    public User getUserByUserId(UserId userId) {
        return findUserById(userId).orElseThrow(
            () -> new BadRequestException("해당 아이디의 유저를 찾을 수 없습니다."));
    }

    @Override
    public SearchUserPagingOutputResponse searchByNickname(final SearchUserOutputRequest request) {
        List<UserEntity> users = userRepository.searchByNickname(request.getNickname(),
            request.getLastIndex(), request.getLimit());

        Long lastIndex = users.isEmpty()?null:users.get(0).getId();
        boolean isLast = true;

        for (UserEntity user : users) {
            if (lastIndex > user.getId())
                lastIndex = user.getId();
        }

        UserEntity lastUser = userRepository.findLastByNickname(request.getNickname())
            .orElse(null);

        if (lastUser!= null)
            isLast = lastUser.getId().equals(lastIndex);

        return SearchUserPagingOutputResponse.builder()
            .lastIndex(lastIndex)
            .isLast(isLast)
            .totalCount(userRepository.countByNicknameLike(String.format("%%%s%%", request.getNickname())))
            .users(users.stream().map(userMapper::toUser).toList())
            .build();
    }

    @Override
    public UserDetail save(UserDetail userDetail) {
        return userDetailMapper.toDomain(
            userDetailRepository.save(userDetailMapper
                .toUserDetailEntity(userDetail)));
    }

    private String toPublicOptions(UserDetail user) {
        String amountOption = user.isPublicAmount()?"1":"0";
        String percentOption = user.isPublicPercent()?"1":"0";

        return amountOption + percentOption;
    }

    @Override
    public boolean existsByContact(String contact) {
        return userDetailRepository.existsByContact(contact);
    }

    @Override
    public Optional<UserDetail> findUserDetailById(UserId userId) {
        return Optional.ofNullable(userDetailMapper.toDomain(
            userDetailRepository.findByUserId(userId.toLong())
                .orElse(null)));
    }

    @Override
    public UserDetail getAdmin() {
        return userDetailMapper.toDomain(userDetailRepository.findByContact("01000000000")
            .orElseThrow(() -> new InternalServerException("Can't Find Admin")));
    }
}
