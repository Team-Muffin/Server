package com.pda.challengeapplication.challenges.controller;

import com.pda.apiutils.ApiUtils;
import com.pda.apiutils.GlobalResponse;
import com.pda.challengeapplication.challenges.dto.request.PostChallengeRequest;
import com.pda.challengeapplication.challenges.dto.request.PostCorpChallengeRequest;
import com.pda.challengeapplication.challenges.dto.response.ChallengeDetailResponse;
import com.pda.challengeapplication.challenges.dto.response.ChallengeSummaryResponse;
import com.pda.challengeapplication.challenges.repository.Challenge;
import com.pda.challengeapplication.challenges.service.ChallengeService;
import com.pda.challengeapplication.challenges.service.CorpChallengeService;
import com.pda.tofinsecurity.user.AuthUser;
import com.pda.tofinsecurity.user.AuthUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name="Challenge", description = "투핀 챌린지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
@Slf4j
public class ChallengeController {

    private final ChallengeService challengeService;
    private final CorpChallengeService corpChallengeService;

    // 등록    1.자체 챌린지 등록  2. 기업 챌린지 등록
    // TODO: ADMIN 권한 걸기
    @PostMapping
    @Operation(summary = "자체 챌린지 등록", description = "자체 챌린지를 등록합니다")
    @ApiResponse(responseCode = "201", description = "성공")
    public GlobalResponse<Challenge> registerChallenge(@Valid @RequestBody PostChallengeRequest challengeDTO){
        log.info("challengeDTO",challengeDTO);
        Challenge savedChallenge = challengeService.createChallenge(challengeDTO);
        return ApiUtils.success("챌린지 등록", savedChallenge);
    }

    @PostMapping("/corp")
    @Operation(summary = "기업 챌린지 등록", description = "기업 챌린지를 등록합니다")
    @ApiResponse(responseCode = "201", description = "성공")
    public GlobalResponse<Challenge> registerCorpChallenge(@Valid @RequestBody PostCorpChallengeRequest corpChallengeDTO){
        Challenge savedChallenge = corpChallengeService.createCorpChallenge(corpChallengeDTO);
        return ApiUtils.success("챌린지 등록", savedChallenge);
    }

    // 조회(자체)
    @GetMapping
    @Operation(summary = "자체 챌린지 조회", description = "자체 챌린지를 조회합니다")
    @ApiResponse(responseCode = "200", description = "성공")
    public GlobalResponse<List<ChallengeSummaryResponse>> findAllChallenge()
    {
        List<ChallengeSummaryResponse> challenges = challengeService.readAllChallenge();
        return ApiUtils.success("챌린지 최신순 조회", challenges);

    }

    // 조회(기업) 1. 0 == 최신순 esle == 마감순
    @GetMapping("/corp")
    @Operation(summary = "기업 챌린지 조회", description = "기업 챌린지를 조회합니다")
    @ApiResponse(responseCode = "200", description = "성공")
    public GlobalResponse<List<ChallengeSummaryResponse>> findAllCorpChallenge(
           @Parameter(name = "sortId", description = "0이면 등록 최신순, 나머지 마감기한순")
           @RequestParam(name = "sortId", required = false, value = "sortId") int sortId)
    {

        if(sortId == 0) {
            List<ChallengeSummaryResponse> challenges = corpChallengeService.readAllCorpChallengeByNew();
            return ApiUtils.success("챌린지 최신순 조회", challenges);
        }

        List<ChallengeSummaryResponse> challenges = corpChallengeService.readAllCorpChallengeByEnd();
        return ApiUtils.success("챌린지 마감순 조회", challenges);

    }


    // 챌린지 상세 조회 (자체 챌린지만 가능)
    @GetMapping("/{id}")
    @Operation(summary = "자체 챌린지 상세 조회", description = "챌린지 상세를 조회합니다",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "성공")
    public GlobalResponse<ChallengeDetailResponse> findChallengeDetail(
            @Valid @PathVariable(value = "id") long id,
            @AuthUser AuthUserInfo userInfo){

        long userId;
        try{
            userId = userInfo.getId();
        }catch (NullPointerException e){
            userId = 0;
        }

        ChallengeDetailResponse challenge = challengeService.findChallenge(id,userId);
        return ApiUtils.success("챌린지 상세 조회", challenge);


    }


    // 기업별 챌린지 조회 (0이면 진행중인 챌린지, 1이면 마감된 챌린지)
    @GetMapping("/corp/{id}")
    @Operation(summary = "기업 id로 기업 챌린지 조회(프로필)", description = "기업별 챌린지를 조회합니다")
    @ApiResponse(responseCode = "200", description = "성공")
    public GlobalResponse<List<ChallengeSummaryResponse>> findCorpChallengeDetail(
            @RequestParam(required = false, value = "isDone") int isDone,
            @Valid @PathVariable(value = "id") long id){

            if(isDone == 0){
                List<ChallengeSummaryResponse> challenge =corpChallengeService.findCorpChallenge(id);
                return ApiUtils.success("진행중인 기업 챌린지 조회", challenge);
            }else{
                List<ChallengeSummaryResponse> challenge = corpChallengeService.findEndCorpChallenge(id);
                return ApiUtils.success("마감된 기업 챌린지 조회", challenge);
            }

    }

    @GetMapping("/search")
    @Operation(summary = "챌린지 이름으로 검색", description = "챌린지를 검색합니다")
    @ApiResponse(responseCode = "200", description = "성공")
    public GlobalResponse<List<ChallengeSummaryResponse>> searchChallengeByName(
            @RequestParam(value = "name") String name
    ){
        List<ChallengeSummaryResponse> searchList = challengeService.searchChallengeByName(name);
        return ApiUtils.success("챌린지 검색", searchList);
    }










}
