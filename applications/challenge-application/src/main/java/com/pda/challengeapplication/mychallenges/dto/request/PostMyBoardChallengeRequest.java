package com.pda.challengeapplication.mychallenges.dto.request;

import com.pda.challengeapplication.challenges.repository.Challenge;
import com.pda.challengeapplication.mychallenges.repository.MyBoardChallenge;
import com.pda.challengeapplication.mychallenges.repository.MyChallenge;
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
public class PostMyBoardChallengeRequest {
    @Schema(description ="챌린지 id", example = "1")
    long challengeId;

    @Schema(description ="유저 id", example = "1")
    long usesrId;

    @Schema(description = "나의 게시글 id ", example = "1")
    long boardId;

    public MyBoardChallenge convertToBoardEntity(MyChallenge mc) {
        MyBoardChallenge myBoardChallenge = new MyBoardChallenge(mc.getId(), boardId,mc);
        return myBoardChallenge;
    }

    public MyChallenge convertToMyChallengeEntitiy(Challenge c){
        LocalDate startAt = LocalDate.now();
        LocalDate endAt;
        if(c.getTerm() == null){
            endAt = LocalDate.of(9999, 12, 31); // 매우 먼 미래 날짜로 설정
        }else{
            endAt = startAt.plusDays(c.getTerm());
        }
        MyChallenge myChallenge = new MyChallenge(null,c, usesrId,startAt, endAt, "진행중");
        return myChallenge;
    }
}
