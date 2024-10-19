package com.yourgg.geng.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class RiotApiService {
    private final RestTemplate restTemplate;
    private final String apiKey;

    private static final String ACCOUNT_BY_RIOT_ID_URL = "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}";
    private static final String SUMMONER_BY_PUUID_URL = "https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/{puuid}";
    private static final String LEAGUE_BY_SUMMONER_URL = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/{summonerId}";
    private static final String MATCH_LIST_URL = "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/{puuid}/ids";
    private static final String MATCH_DETAIL_URL = "https://asia.api.riotgames.com/lol/match/v5/matches/{matchId}";

    public RiotApiService(RestTemplate restTemplate, @Value("${riot.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public CompletableFuture<String> getPUUIDByRiotId(String gameName, String tagLine) {
        return CompletableFuture.supplyAsync(() -> {
            String url = UriComponentsBuilder.fromHttpUrl(ACCOUNT_BY_RIOT_ID_URL)
                    .buildAndExpand(gameName, tagLine)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Riot-Token", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, Map.class);

                Map<String, Object> account = response.getBody();
                if (account != null && account.containsKey("puuid")) {
                    return (String) account.get("puuid");
                }
                throw new IllegalArgumentException("해당 Riot ID를 찾을 수 없습니다.");
            } catch (HttpClientErrorException e) {
                throw new IllegalArgumentException("해당 Riot ID를 찾을 수 없습니다.");
            }
        });
    }

    public CompletableFuture<Map<String, Object>> getSummonerInfoByPUUID(String puuid) {
        return CompletableFuture.supplyAsync(() -> {
            String url = UriComponentsBuilder.fromHttpUrl(SUMMONER_BY_PUUID_URL)
                    .buildAndExpand(puuid)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Riot-Token", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        });
    }

    public CompletableFuture<List<Map<String, Object>>> getTierInfoBySummonerId(String summonerId) {
        return CompletableFuture.supplyAsync(() -> {
            String url = UriComponentsBuilder.fromHttpUrl(LEAGUE_BY_SUMMONER_URL)
                    .buildAndExpand(summonerId)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Riot-Token", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            return response.getBody();
        });
    }

    public CompletableFuture<List<String>> getMatchListByPUUID(String puuid) {
        return CompletableFuture.supplyAsync(() -> {
            String url = UriComponentsBuilder.fromHttpUrl(MATCH_LIST_URL)
                    .queryParam("start", 0)
                    .queryParam("count", 10)  // 최근 10경기 가져오기
                    .buildAndExpand(puuid)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Riot-Token", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            return response.getBody();
        });
    }

    public CompletableFuture<Map<String, Object>> getMatchDetail(String matchId) {
        return CompletableFuture.supplyAsync(() -> {
            String url = UriComponentsBuilder.fromHttpUrl(MATCH_DETAIL_URL)
                    .buildAndExpand(matchId)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Riot-Token", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> matchData = response.getBody();
            if (matchData != null && matchData.containsKey("info")) {
                return (Map<String, Object>) matchData.get("info");
            }
            throw new IllegalArgumentException("매치 정보를 가져올 수 없습니다.");
        });
    }
}
