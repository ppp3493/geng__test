package com.yourgg.geng;

import com.yourgg.geng.controller.SummonerController;
import com.yourgg.geng.service.RiotApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SummonerControllerTest {

    private RiotApiService riotApiService;
    private SummonerController summonerController;
    private Model model;

    @BeforeEach
    void setUp() {
        riotApiService = mock(RiotApiService.class);
        summonerController = new SummonerController(riotApiService);
        model = mock(Model.class);
    }

    @Test
    void testGetSummonerProfile_Success() {
        String input = "testUser#KR";

        // Mocking the service methods
        when(riotApiService.getPUUIDByRiotId("testUser", "KR"))
                .thenReturn(CompletableFuture.completedFuture("mockPuuid"));

        Map<String, Object> summonerInfo = new HashMap<>();
        summonerInfo.put("id", "mockId");
        summonerInfo.put("summonerLevel", 30);
        summonerInfo.put("profileIconId", 123);

        when(riotApiService.getSummonerInfoByPUUID("mockPuuid"))
                .thenReturn(CompletableFuture.completedFuture(summonerInfo));

        // Mock tier info return
        when(riotApiService.getTierInfoBySummonerId("mockId"))
                .thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));

        // Mock match list return
        when(riotApiService.getMatchListByPUUID("mockPuuid"))
                .thenReturn(CompletableFuture.completedFuture(Arrays.asList("matchId1", "matchId2")));

        // Mock match detail return
        Map<String, Object> matchDetail = new HashMap<>();
        matchDetail.put("participants", new ArrayList<>());
        when(riotApiService.getMatchDetail("matchId1"))
                .thenReturn(CompletableFuture.completedFuture(matchDetail));

        when(riotApiService.getMatchDetail("matchId2"))
                .thenReturn(CompletableFuture.completedFuture(matchDetail));

        String result = summonerController.getSummonerProfile(input, model);

        // Validate the results
        assertEquals("profile", result);
        Mockito.verify(model).addAttribute("summonerName", "testUser");
        Mockito.verify(model).addAttribute("level", 30);
        Mockito.verify(model).addAttribute("profileIconId", 123);
    }

    @Test
    void testGetSummonerProfile_PlayerNotFound() {
        String input = "invalidUser#KR";

        // Mocking the service methods to throw an exception
        when(riotApiService.getPUUIDByRiotId("invalidUser", "KR"))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "{\"status\":{\"status_code\":404,\"message\":\"Data not found - No results found\"}}"));

        String result = summonerController.getSummonerProfile(input, model);

        assertEquals("error", result);
        Mockito.verify(model).addAttribute("errorMessage", "해당 Riot ID를 찾을 수 없습니다."); // 수정된 부분
    }

    // 추가적인 테스트 케이스
}

