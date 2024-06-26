package com.pda.userapplication.services.in;


import com.pda.userapplication.services.in.dto.req.SearchUserServiceRequest;
import com.pda.userapplication.services.in.dto.res.GetUserPagingResponse;
import com.pda.userapplication.services.in.dto.res.UserServiceResponse;

import java.time.LocalDate;

public interface GetUserUseCase {
    UserServiceResponse findById(Long id, Long myId);
    GetUserPagingResponse searchUserByNickname(SearchUserServiceRequest request);
    LocalDate getBirth(Long id);
    boolean isAssetConnected(Long id);
}
