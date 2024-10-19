package com.yourgg.geng.controller;

import com.yourgg.geng.service.RiotApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/summoner")
public class MatchController {
    private final RiotApiService riotApiService;

    public MatchController(RiotApiService riotApiService) {
        this.riotApiService = riotApiService;
    }

    @GetMapping("/{name}/match/{matchId}")
    public CompletableFuture<String> getMatchDetail(
            @PathVariable("name") String summonerName,
            @PathVariable("matchId") String matchId,
            Model model) {
        return riotApiService.getMatchDetail(matchId)
                .thenApply(matchDetail -> {
                    List<Map<String, Object>> participants = (List<Map<String, Object>>) matchDetail.get("participants");

                    // 팀 정보 분류
                    List<Map<String, Object>> team1 = new ArrayList<>();
                    List<Map<String, Object>> team2 = new ArrayList<>();

                    for (int i = 0; i < participants.size(); i++) {
                        Map<String, Object> player = participants.get(i);
                        player.put("items", List.of(
                                player.get("item0"), player.get("item1"), player.get("item2"),
                                player.get("item3"), player.get("item4"), player.get("item5")
                        ));
                        if (i < 5) {
                            team1.add(player);
                        } else {
                            team2.add(player);
                        }
                    }

                    List<Map<String, Object>> teams = List.of(
                            Map.of("name", "승리팀", "players", team1),
                            Map.of("name", "패배팀", "players", team2)
                    );

                    model.addAttribute("summonerName", summonerName);
                    model.addAttribute("teams", teams);

                    return "match_detail"; // view name
                })
                .exceptionally(e -> {
                    model.addAttribute("error", "매치 데이터를 불러올 수 없습니다: " + e.getMessage());
                    return "error";
                });
    }

    @GetMapping("/match/{matchId}")
    public CompletableFuture<Map<String, Object>> getMatchDetail(@PathVariable("matchId") String matchId) {
        return riotApiService.getMatchDetail(matchId)
                .thenApply(matchDetail -> {
                    List<Map<String, Object>> participants = (List<Map<String, Object>>) matchDetail.get("participants");

                    // 팀과 플레이어 정보를 반환
                    return Map.of(
                            "teams", List.of(
                                    Map.of("name", "승리팀", "players", participants.subList(0, 5)),
                                    Map.of("name", "패배팀", "players", participants.subList(5, 10))
                            )
                    );
                });
    }
}
