package com.yourgg.geng.controller;

import com.yourgg.geng.service.RiotApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/summoner")
public class SummonerController {
    private final RiotApiService riotApiService;

    public SummonerController(RiotApiService riotApiService) {
        this.riotApiService = riotApiService;
    }

    @GetMapping("/{input}/latest")
    public String getSummonerProfile(@PathVariable("input") String input, Model model) {
        String[] parts = input.split("#");
        if (parts.length != 2) {
            model.addAttribute("errorMessage", "올바른 형식으로 입력하세요. 예: Hide on Bush#KR");
            return "error";
        }

        String gameName = parts[0].trim();
        String tagLine = parts[1].trim();

        try {
            // 1. PUUID 비동기 가져오기
            CompletableFuture<String> puuidFuture = riotApiService.getPUUIDByRiotId(gameName, tagLine);

            // 2. 소환사 정보 비동기 가져오기
            CompletableFuture<Map<String, Object>> summonerInfoFuture = puuidFuture.thenCompose(riotApiService::getSummonerInfoByPUUID
            );

            // 3. 티어 정보 비동기 가져오기
            CompletableFuture<List<Map<String, Object>>> tierInfoFuture = summonerInfoFuture.thenCompose(summonerInfo -> {
                String summonerId = (String) summonerInfo.get("id");
                return riotApiService.getTierInfoBySummonerId(summonerId);
            });

            // 4. 매치 리스트 비동기 가져오기
            CompletableFuture<List<String>> matchIdsFuture = puuidFuture.thenCompose(riotApiService::getMatchListByPUUID
            );

            // 5. 매치 상세 정보 비동기 가져오기 및 게임 타입별 분류
            CompletableFuture<List<Map<String, Object>>> soloRankMatchesFuture = matchIdsFuture.thenCompose(matchIds -> {
                List<CompletableFuture<Map<String, Object>>> matchFutures = new ArrayList<>();
                for (String matchId : matchIds) {
                    matchFutures.add(riotApiService.getMatchDetail(matchId));
                }

                return CompletableFuture.allOf(matchFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> {
                            List<Map<String, Object>> soloRankMatches = new ArrayList<>();
                            for (int i = 0; i < matchFutures.size(); i++) {
                                Map<String, Object> matchDetail = matchFutures.get(i).join();
                                List<Map<String, Object>> participants = (List<Map<String, Object>>) matchDetail.get("participants");
                                Map<String, Object> playerData = participants.stream()
                                        .filter(p -> p.get("puuid").equals(puuidFuture.join()))
                                        .findFirst()
                                        .orElse(null);

                                if (playerData != null) {
                                    playerData.put("matchId", matchIds.get(i)); // matchId 추가

                                    int queueId = (int) matchDetail.get("queueId");

                                    // 게임 타입별로 분류
                                    if (queueId == 420) {
                                        soloRankMatches.add(playerData);
                                    }
                                }
                            }
                            return soloRankMatches;
                        });
            });

            // 모든 비동기 작업 결과를 기다리기
            Map<String, Object> summonerInfo = summonerInfoFuture.join();
            List<Map<String, Object>> tierInfoList = tierInfoFuture.join();
            List<Map<String, Object>> soloRankMatches = soloRankMatchesFuture.join();

            // 결과 추출
            int level = (int) summonerInfo.get("summonerLevel");
            int profileIconId = (int) summonerInfo.get("profileIconId");
            String tier = "Unranked";
            if (!tierInfoList.isEmpty()) {
                Map<String, Object> tierInfo = tierInfoList.getFirst();
                tier = tierInfo.get("tier") + " " + tierInfo.get("rank");
            }

            // 모델에 데이터 추가
            model.addAttribute("summonerName", gameName);
            model.addAttribute("level", level);
            model.addAttribute("profileIconId", profileIconId);
            model.addAttribute("tier", tier);
            model.addAttribute("soloRankMatches", soloRankMatches);

        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "해당 Riot ID를 찾을 수 없습니다.");
            return "error";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "예상 못한 오류");
            return "error";
        }

        return "profile";
    }

    @GetMapping("/")
    public String showSearchPage() {
        return "index";
    }
}
