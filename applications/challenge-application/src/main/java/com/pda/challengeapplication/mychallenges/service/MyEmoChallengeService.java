package com.pda.challengeapplication.mychallenges.service;

import com.pda.apiutils.GlobalResponse;
import com.pda.challengeapplication.challenges.repository.Challenge;
import com.pda.challengeapplication.challenges.repository.ChallengeRepository;
import com.pda.challengeapplication.emojis.EmojiRepository;
import com.pda.challengeapplication.mychallenges.dto.request.PostMyEmoLogRequest;
import com.pda.challengeapplication.mychallenges.dto.request.outer.PostMyEmoChallengeRequest;
import com.pda.challengeapplication.mychallenges.dto.request.outer.SendChallengeResultRequest;
import com.pda.challengeapplication.mychallenges.dto.request.outer.TransferRequest;
import com.pda.challengeapplication.mychallenges.dto.response.MyEmoChallengeLog;
import com.pda.challengeapplication.mychallenges.dto.response.MyEmoChallengeLogResponse;
import com.pda.challengeapplication.mychallenges.dto.response.outer.AccountResponse;
import com.pda.challengeapplication.mychallenges.dto.response.outer.AssetInfoResponse;
import com.pda.challengeapplication.mychallenges.dto.response.outer.UserDetailInfoResponse;
import com.pda.challengeapplication.mychallenges.repository.*;
import com.pda.exceptionhandler.exceptions.ConflictException;
import com.pda.exceptionhandler.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyEmoChallengeService {

    @Value("${out-service.asset.url}")
    private String assetUrl;

    @Value("${out-service.user.url}")
    private String userUrl;

    private final MyAssetChallengeDetailRepository myAssetChallengeDetailRepository;
    private final MyAssetChallengeRepository myAssetChallengeRepository;
    private final MyChallengeRepository myChallengeRepository;
    private final EmojiRepository emojiRepository;
    private final ChallengeRepository challengeRepository;
    private final  SendMyChallengeResultPort sendMyChallengeResultPort;

    //챌린지 참여
    public Long participateEmoChallenge(PostMyEmoChallengeRequest pa, String token, long uid) {

        log.info("url!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", assetUrl);
        log.info("url!??????????", userUrl);
        if(pa.getInACNT() == pa.getOutACNT()){
            throw new ConflictException("입금 계좌와 출금 계좌는 다른 계좌여야합니다");
        }

        Challenge c = challengeRepository.findById(pa.getChallengeId());
        LocalDate startAt = LocalDate.now();
        LocalDate endAt = startAt.plusDays(c.getTerm());
        MyChallenge mc = pa.converToMCEntity(c,uid,startAt, endAt,"진행중" );

        // body : 선택한 입출금 계좌, myChallengeId
        // req to User : user 토큰   -> res from user : 유저정보
        URI uri = UriComponentsBuilder
                .fromUriString(userUrl)
                .path("/users/detail-info")
                .encode(Charset.defaultCharset())
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<GlobalResponse<UserDetailInfoResponse>> response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<GlobalResponse<UserDetailInfoResponse>>() {});
        System.out.println("토큰!!!!!!!!!" + token);
        System.out.println("개인정보!!!!!" + response.getBody().getData().getBackSocialId());
//         req to Asset : 유저 정보 -> res from Asset : 계좌 리스트

        uri = UriComponentsBuilder
                .fromUriString(assetUrl)
                .path("/assets")
                .queryParam("targets", "ACCOUNT")
                .encode(Charset.defaultCharset())
                .build()
                .toUri();


        HttpHeaders assetHeaders = new HttpHeaders();
        assetHeaders.set("front-social-id", response.getBody().getData().getFrontSocialId());
        assetHeaders.set("back-social-id", response.getBody().getData().getBackSocialId());
        assetHeaders.set("user-social-contact", response.getBody().getData().getContact());

        HttpEntity<String> assetEntity = new HttpEntity<>(assetHeaders);

        log.info(uri.toString());


        RestTemplate assetRestTemplate = new RestTemplate();
        ResponseEntity<GlobalResponse<AssetInfoResponse>> response2
                = assetRestTemplate.exchange(uri, HttpMethod.GET, assetEntity,new ParameterizedTypeReference<GlobalResponse<AssetInfoResponse>>() {});

        List<AccountResponse> ac = response2.getBody().getData().getAccounts();


        // 계좌 리스트에 body 계좌들이 있는지 확인하고 저장!
        int cnt =0;


        for(AccountResponse acr : ac){

            log.info(acr.getAccountNumber());

            if(acr.getAccountNumber().equals(pa.getInACNT())){
                cnt ++;
            }
            else if(acr.getAccountNumber().equals(pa.getOutACNT())){
                cnt ++;
            }

        }

        if(cnt !=2){
            throw new NotFoundException("입금 계좌 또는 출금 계좌를 찾을 수 없습니다");
        }


        MyChallenge mySaveChallenge = myChallengeRepository.save(mc);
        MyAssetChallengeDetail md = new MyAssetChallengeDetail(mySaveChallenge.getId(),pa.getInACNT(), pa.getOutACNT());
        myAssetChallengeDetailRepository.save(md);

        return mySaveChallenge.getId();

    }



    // 감정 저축하기

    public void createMyEmoLog(PostMyEmoLogRequest postMyEmoRequest, String token, Long uid) {
        long mid = myChallengeRepository.selectMyChallenge(1,uid ).getId();
        MyAssetChallengeDetail md = myAssetChallengeDetailRepository.findByMyChallengeId(mid);
        MyChallenge mc = myChallengeRepository.findById(mid);
        long price = emojiRepository.findById(postMyEmoRequest.getEmojiId()).get().getPrice();

        // req to User : 토큰 , res from User: 유저 개인 정보
        URI uri = UriComponentsBuilder
                .fromUriString(userUrl)
                .path("/users/detail-info")
                .encode(Charset.defaultCharset())
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<GlobalResponse<UserDetailInfoResponse>> response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<GlobalResponse<UserDetailInfoResponse>>() {});

        // req to Asset: 계좌번호, 송금금액  , void
        uri = UriComponentsBuilder
                .fromUriString(assetUrl)
                .path("/transfers")
                .encode(Charset.defaultCharset())
                .build()
                .toUri();

        // body랑 header 설정
        HttpHeaders assetHeaders = new HttpHeaders();
        TransferRequest body = new TransferRequest(md.getOutACNT(), md.getInACNT(), price);
        assetHeaders.set("front-social-id", response.getBody().getData().getFrontSocialId());
        assetHeaders.set("back-social-id", response.getBody().getData().getBackSocialId());
        assetHeaders.set("user-social-contact", response.getBody().getData().getContact());


        HttpEntity<?> assetEntity = new HttpEntity<>(body, assetHeaders);

       // 요청

        HttpEntity<Void> transferResponse = restTemplate.postForEntity(uri,assetEntity,null );


        MyAssetChallenge myAssetChallenge = postMyEmoRequest.convertToAccountEntity(mid);
        myAssetChallengeRepository.save(myAssetChallenge);

        if(mc.getEndAt() == LocalDate.now()){
            mc.editMyChallengeStatus("성공");
            myChallengeRepository.save(mc);
            SendResult(mc.getChallenge().getName(), mc.getChallenge().getLogoUrl(), mc.getUserId(), mc.getChallenge().getChallengeDetail().getReward());
            // TODO 저축 챌린지 성공 알림

        }
    }

    @Async
    public void SendResult(String challengeName, String logoUrl, Long userId, Integer reward){
        sendMyChallengeResultPort.sendChallengeResult(SendChallengeResultRequest.builder()
                .result(challengeName+"성공으로" +reward+" 크레딧을 획득하셨습니다.")
                .challengeName(challengeName)
                .userId(userId)
                .transactionDateTime(LocalDateTime.now())
                .logoUrl(logoUrl)
                .build());


    }

    @Async
    public void SendFailResult(String challengeName, String logoUrl, Long userId){
        sendMyChallengeResultPort.sendChallengeResult(SendChallengeResultRequest.builder()
                .result(challengeName+"를 실패하셨습니다")
                .challengeName(challengeName)
                .userId(userId)
                .transactionDateTime(LocalDateTime.now())
                .logoUrl(logoUrl)
                .build());


    }




    public MyEmoChallengeLogResponse readAllEmoChallengeLog(long uId) {
        long mId = myChallengeRepository.selectMyChallenge(1,uId).getId();
        List<MyAssetChallenge> echallenges = myAssetChallengeRepository.findByMyChallengeId(mId);
        int totalprice =0;
        List<MyEmoChallengeLog> mcl = echallenges.stream()
                .map((c)->MyEmoChallengeLog.builder()
                        .savingAt(c.getSavingAt())
                        .price(emojiRepository.findById(c.getEmojiId()).get().getPrice())
                        .emotion(emojiRepository.findById(c.getEmojiId()).get().getEmotion())
                        .emojiUrl(emojiRepository.findById(c.getEmojiId()).get().getImgUrl())
                        .build())
                .toList();
        for(MyEmoChallengeLog c: mcl){
            totalprice += c.getPrice();
        }

        MyEmoChallengeLogResponse mr = new MyEmoChallengeLogResponse(mcl,totalprice);
        return mr;
    }

    @Scheduled(cron = "0 55 23 * * *")
    public void checkChallengeStatus() {
        log.info("시작");
        String isSuccess = "진행중";
        // 감정저축 챌린지이면서 진행중인것들
        List<MyChallenge> successChallenge = myChallengeRepository.findAll().stream().filter(c-> c.getStatus().equals("진행중")&& c.getChallenge().getId() ==1) .toList();

        for(MyChallenge s: successChallenge){
            log.info(s.toString());
        }

        for(MyChallenge s: successChallenge){
            log.info("진행중인 챌린지, {}", s.getChallenge().getName());
            if(myAssetChallengeRepository.findByMyChallengeIdAndSavingAt(s.getId(),LocalDate.now()) == null){
                // 그날 감정 저축한 기록이 없으면 해당 myChallengeId는 실패로
                isSuccess = "실패";
                SendFailResult(s.getChallenge().getName(), s.getChallenge().getLogoUrl(), s.getUserId());
                //TODO 저축 알림 실패했을 경우 알람
            }
            s.editMyChallengeStatus(isSuccess);
            myChallengeRepository.save(s);
        }

    }



}


