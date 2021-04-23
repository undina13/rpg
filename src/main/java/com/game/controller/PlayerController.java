package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class PlayerController {
private PlayerService playerService;

@Autowired
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/players")
    @ResponseBody
    public List<Player> getShipsList(@RequestParam(value = "name", required = false) String name,
                                     @RequestParam(value = "title", required = false) String title,
                                     @RequestParam(value = "race", required = false) Race race,
                                     @RequestParam(value = "profession", required = false) Profession profession,
                                     @RequestParam(value = "after", required = false) Long after,
                                     @RequestParam(value = "before", required = false) Long before,
                                     @RequestParam(value = "banned", required = false) Boolean banned,
                                     @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                     @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                     @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                     @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                     @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
                                     @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                     @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        return playerService.getAllPlayers(
                Specification.where(playerService.filterByName(name)
                        .and(playerService.filterByTitle(title)))
                        .and(playerService.filterByRace(race))
                        .and(playerService.filterByProfession(profession))
                        .and(playerService.filterByBirthday(after, before))
                        .and(playerService.filterByBanned(banned))
                        .and(playerService.filterByExperience(minExperience, maxExperience))
                        .and(playerService.filterByLevel(minLevel, maxLevel))
                        , pageable)
                .getContent();
    }

    @GetMapping("/players/count")
    public @ResponseBody Long getShipsCount(@RequestParam(value = "name", required = false) String name,
                                            @RequestParam(value = "title", required = false) String title,
                                            @RequestParam(value = "race", required = false) Race race,
                                            @RequestParam(value = "profession", required = false) Profession profession,
                                            @RequestParam(value = "after", required = false) Long after,
                                            @RequestParam(value = "before", required = false) Long before,
                                            @RequestParam(value = "banned", required = false) Boolean banned,
                                            @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                            @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                            @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {
        return playerService.getPlayersCount(
                Specification.where(playerService.filterByName(name)
                        .and(playerService.filterByTitle(title)))
                        .and(playerService.filterByRace(race))
                        .and(playerService.filterByProfession(profession))
                        .and(playerService.filterByBirthday(after, before))
                        .and(playerService.filterByBanned(banned))
                        .and(playerService.filterByExperience(minExperience, maxExperience))
                        .and(playerService.filterByLevel(minLevel, maxLevel)));
    }

    @GetMapping("/players/{id}")
    public @ResponseBody
    ResponseEntity<Player> getPlayer(@PathVariable Long id) {
        Player result = playerService.getPlayerById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/players/")
    @ResponseBody
    public ResponseEntity<Player> createShip(@RequestBody Player player) {
        playerService.createPlayer(player);
        return ResponseEntity.ok(player);
    }

    @PostMapping("/players/{id}")
    public ResponseEntity<Player> updateShip(@PathVariable Long id, @RequestBody Player oldPlayer) {
        Player newPlayer = playerService.updatePlayer(id, oldPlayer);
        return ResponseEntity.ok(newPlayer);
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<Player> deleteShip(@PathVariable Long id) {
        playerService.deletePlayerById(id);
        return ResponseEntity.ok().build();
    }
}
