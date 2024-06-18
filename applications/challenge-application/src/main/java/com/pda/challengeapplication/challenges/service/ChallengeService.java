package com.pda.challengeapplication.challenges.service;

import com.pda.challengeapplication.challenges.dto.request.ModifyChallengeRequest;
import com.pda.challengeapplication.challenges.dto.request.PostChallengeRequest;
import com.pda.challengeapplication.challenges.dto.response.ChallengeDetailResponse;
import com.pda.challengeapplication.challenges.dto.response.ChallengeSummaryResponse;
import com.pda.challengeapplication.challenges.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeDetailRepository challengeDetailRepository;
    private final CorpChallengeDetailRepository corpChallengeDetailRepository;

    // 챌린지 등록
    @Transactional
    public Challenge createChallenge(PostChallengeRequest challengeDTO) {
        Challenge savedChallenge = challengeRepository.save(challengeDTO.convertToChallengeEntity());
        ChallengeDetail challengeDetail = challengeDTO.convertToCDEntity(savedChallenge);
        challengeDetail.setChallengeId(savedChallenge.getId());
        challengeDetailRepository.save(challengeDetail);
        return challengeRepository.findFirstByOrderByIdDesc();
    }

    // 조회
    public List<ChallengeSummaryResponse> readAllChallenge() {
        List<ChallengeDetail> challengeList = challengeDetailRepository.findAll();
        return challengeList.stream()
                .map((challenge) -> ChallengeSummaryResponse.builder()
                        .id(challenge.getChallenge().getId())
                        .challengeType(challenge.getChallenge().getChallengeType())
                        .name(challenge.getChallenge().getName())
                        .description(challenge.getChallenge().getDescription())
                        .logoUrl(challenge.getChallenge().getLogoUrl())
                        .endAt(challenge.getChallenge().getEndAt())
                        .term(challenge.getChallenge().getTerm())

                        .build())
                .toList();
    }

    // 상세 조회
    public ChallengeDetailResponse findChallenge(long id) {
       Optional<ChallengeDetail> challengeDetail = challengeDetailRepository.findByChallengeId(id);
       return ChallengeDetailResponse.builder()
               .id(challengeDetail.get().getChallenge().getId())
               .challengeType(challengeDetail.get().getChallenge().getChallengeType())
               .name(challengeDetail.get().getChallenge().getName())
               .description(challengeDetail.get().getChallenge().getDescription())
               .logoUrl(challengeDetail.get().getChallenge().getLogoUrl())
               .startAt(challengeDetail.get().getChallenge().getStartAt())
               .endAt(challengeDetail.get().getChallenge().getEndAt())
               .term(challengeDetail.get().getChallenge().getTerm())
               .detailDescription(challengeDetail.get().getDetailDescription())
               .badgeName(challengeDetail.get().getBadgeName())
               .standardNum(challengeDetail.get().getStandardNum())
               .standardCg(challengeDetail.get().getStandardCg())
               .build();
    }



    public Challenge modifyChallenge(ModifyChallengeRequest modifyChallenge, long id) {
        Challenge challenge = challengeRepository.findById(id);
        ChallengeDetail challengeDetail = challengeDetailRepository.findByChallengeId(id).get();
//        if(challenge == null){
//            //예외처리
//        }
        challenge.editChallenge(modifyChallenge.getName(),modifyChallenge.getDescription(),modifyChallenge.getLogoUrl(), modifyChallenge.getStartAt(),modifyChallenge.getEndAt());
        challengeRepository.save(challenge);
        challengeDetail.editChallengeDetail(challengeDetail.getDetailDescription(), challenge);
        challengeDetailRepository.save(challengeDetail);

        return challengeRepository.findById(id);

    }


    public List<ChallengeSummaryResponse> searchChallengeByName(String searchname) {
        List<Challenge> challengeDetails= challengeRepository.findByName(searchname);
        List<ChallengeSummaryResponse> returnList = new ArrayList<>();
        for(Challenge c : challengeDetails){
            long id = c.getId();
            int challengeType = c.getChallengeType();
            String name = c.getName();
            String description = c.getDescription();
            String logoUrl = c.getLogoUrl();
            LocalDate endAt = c.getEndAt();
            int term = c.getTerm();
            String challengeUrl; String corpName;

            if(challengeType ==0){
                challengeUrl = corpChallengeDetailRepository.findByChallengeId(id).getChallengeUrl();
                corpName = corpChallengeDetailRepository.findByChallengeId(id).getCorpName();
                
            }else{
                challengeUrl = null;
                corpName = null;
            }

            ChallengeSummaryResponse cr = ChallengeSummaryResponse.builder()
                    .id(id)
                    .challengeType(challengeType)
                    .name(name)
                    .description(description)
                    .logoUrl(logoUrl)
                    .endAt(endAt)
                    .term(term)
                    .challengeUrl(challengeUrl)
                    .corpName(corpName)
                    .build();

            returnList.add(cr);
        }

        return returnList;
    }
}



