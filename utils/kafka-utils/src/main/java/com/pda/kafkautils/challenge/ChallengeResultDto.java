package com.pda.kafkautils.challenge;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pda.kafkautils.KafkaJson;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@JsonSerialize
@JsonDeserialize
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChallengeResultDto implements KafkaJson {
    private Long userId;
    private String logoUrl;
    private String challengeName;
    private String result;
    private LocalDateTime transactionDateTime;

}
