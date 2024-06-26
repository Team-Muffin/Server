package com.pda.challengeapplication.mychallenges.dto.request;

import com.pda.challengeapplication.mychallenges.repository.MyAssetChallenge;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostMyEmoLogRequest {

    @Schema(description = "선택한 이모지ID", example = "1")
    long emojiId;


    public MyAssetChallenge convertToAccountEntity(Long mid) {
        MyAssetChallenge myAssetChallenge = new MyAssetChallenge(null, mid, emojiId, LocalDate.now(), true);
        return myAssetChallenge;
    }
}
