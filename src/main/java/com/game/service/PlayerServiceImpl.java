package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.BadRequestException;
import com.game.exceptions.NotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class PlayerServiceImpl implements PlayerService{
    private PlayerRepository playerRepository;

    private static final int LEFT_BIRTHDAY_YEAR = 2000;
    private static final int RIGHT_BIRTHDAY_YEAR = 3000;
    private static final int NAME_LENGTH = 12;
    private static final int TITLE_LENGTH = 30;

@Autowired
    public void setPlayerRepository(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Page<Player> getAllPlayers(Specification<Player> specification, Pageable pageable) {
        return playerRepository.findAll(specification, pageable);
    }

    @Override
    public Long getPlayersCount(Specification<Player> specification) {
        return playerRepository.count(specification);
    }

    @Override
    public Player getPlayerById(Long id) {
        validateId(id);
        return playerRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Player with id %d not found!", id)));
    }

    @Override
    public void deletePlayerById(Long id) {
        Player player = getPlayerById(id);
        playerRepository.delete(player);

    }

    @Override
    public Player createPlayer(Player player) {
        validateName(player.getName());
        validateTitle(player.getTitle());
        validateRace(player.getRace());
        validateProfession(player.getProfession());
        validateExperience(player.getExperience());
        validateBirthday(player.getBirthday());


        if (player.getBanned() == null)
            player.setBanned(false);

        player.setLevel(calculateLevel(player.getExperience()));
        player.setUntilNextLevel(calculateUntilNextLevel(player.getExperience(), player.getLevel()));

        return playerRepository.saveAndFlush(player);
    }

    @Override
    public Player updatePlayer(Long id, Player newPlayer) {
        Player old = getPlayerById(id);

        if (newPlayer.getName() != null) {
            validateName(newPlayer.getName());
            old.setName(newPlayer.getName());
        }

        if (newPlayer.getTitle() != null) {
            validateTitle(newPlayer.getTitle());;
            old.setTitle(newPlayer.getTitle());
        }

        if (newPlayer.getRace() != null) {
            validateRace(newPlayer.getRace());;
            old.setRace(newPlayer.getRace());
        }

        if (newPlayer.getProfession() != null) {
            validateProfession(newPlayer.getProfession());;
            old.setProfession(newPlayer.getProfession());
        }

        if (newPlayer.getExperience() != null) {
            validateExperience(newPlayer.getExperience());;
            old.setExperience(newPlayer.getExperience());
        }

        if (newPlayer.getBirthday() != null) {
            validateBirthday(newPlayer.getBirthday());;
            old.setBirthday(newPlayer.getBirthday());
        }


        if (newPlayer.getBanned() != null) {
            old.setBanned(newPlayer.getBanned());
        }

        old.setLevel(calculateLevel(old.getExperience()));
        old.setUntilNextLevel(calculateUntilNextLevel(old.getExperience(), old.getLevel()));

        playerRepository.save(old);
        return old;
    }

    @Override
    public Specification<Player> filterByName(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(root.get("name"), "%" + name + "%");
    }

    @Override
    public Specification<Player> filterByTitle(String title) {
        return (root, query, cb) -> title == null ? null : cb.like(root.get("title"), "%" + title + "%");
    }

    @Override
    public Specification<Player> filterByRace(Race race) {
        return (root, query, cb) -> race == null ? null : cb.equal(root.get("race"), race);
    }

    @Override
    public Specification<Player> filterByProfession(Profession profession) {
        return (root, query, cb) -> profession == null ? null : cb.equal(root.get("profession"), profession);
    }

    @Override
    public Specification<Player> filterByExperience(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }

            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("experience"), max);
            }

            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("experience"), min);
            }

            return cb.between(root.get("experience"), min, max);
        };
    }

    @Override
    public Specification<Player> filterByLevel(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }

            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("level"), max);
            }

            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("level"), min);
            }

            return cb.between(root.get("level"), min, max);
        };
    }

    @Override
    public Specification<Player> filterByUntilNextLevel(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }

            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("untilNextLevel"), max);
            }

            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("untilNextLevel"), min);
            }

            return cb.between(root.get("untilNextLevel"), min, max);
        };
    }

    @Override
    public Specification<Player> filterByBirthday(Long after, Long before) {
        return (root, query, cb) -> {
            if (after == null && before == null) {
                return null;
            }

            if (after == null) {
                return cb.lessThanOrEqualTo(root.get("birthday"), new Date(before));
            }

            if (before == null) {
                return cb.greaterThanOrEqualTo(root.get("birthday"), new Date(after));
            }

            return cb.between(root.get("birthday"), new Date(after), new Date(before));
        };
    }

    @Override
    public Specification<Player> filterByBanned(Boolean banned) {
        return (root, query, cb) -> {
            if (banned == null) {
                return null;
            }

            if (banned) {
                return cb.isTrue(root.get("banned"));
            }

            return cb.isFalse(root.get("banned"));
        };
    }

    private void validateId(Long id) {
        if (id <= 0) {
            throw new BadRequestException("Player id is invalid!");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isEmpty() || name.length() > NAME_LENGTH)
            throw new BadRequestException("Wrong player name!");
    }

    private void validateTitle(String title) {
        if (title == null || title.isEmpty() || title.length() > TITLE_LENGTH)
            throw new BadRequestException("Wrong player title!");
    }

    private void validateRace(Race race){
        if (race == null)
            throw new BadRequestException("Wrong player race!");
    }

    private void validateProfession(Profession profession){
        if (profession == null)
            throw new BadRequestException("Wrong player profession!");
    }

    private void validateExperience(Integer experience) {
        if (experience == null || experience < 0 || experience > 10000000)
            throw new BadRequestException("Wrong experience!");
    }

    private void validateBirthday(Date birthday){
        if (birthday == null)
            throw new BadRequestException("Wrong birthday!");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(birthday.getTime());
        if (calendar.get(Calendar.YEAR) < LEFT_BIRTHDAY_YEAR || calendar.get(Calendar.YEAR) > RIGHT_BIRTHDAY_YEAR)
            throw new BadRequestException("Wrong player birthday");

    }

    private int calculateLevel(int experience){
    return (int)((Math.sqrt(2500 + 200*experience)-50)/100);
        }

    private int calculateUntilNextLevel(int experience, int level){
        return 50 * (level + 1)* (level + 2)- experience;
    }
}
