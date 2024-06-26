package com.pda.userapplication.services.in;

import com.pda.userapplication.services.in.dto.req.ExchangeServiceRequest;
import com.pda.userapplication.services.in.dto.res.TokenInfoServiceResponse;

public interface FinfluencerUseCase {
    TokenInfoServiceResponse becomeFinfluencer(Long userId, String token);
    void exchange(ExchangeServiceRequest request);
}
