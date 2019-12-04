package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static java.util.stream.Collectors.toCollection;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    @RequestMapping("/games")
    public LinkedHashSet<Object> getAllGamesData() {
        return gameRepository
                .findAll()
                .stream().map(this::createGameDTO)
                .collect(toCollection( LinkedHashSet::new ));
    }

    @RequestMapping("/game_view/{gamePlayerId}")
    public ResponseEntity<LinkedHashMap<String, Object>> getGamePlayerData(@PathVariable long gamePlayerId) {
        GamePlayer gamePlayer = gamePlayerRepository
                .findById(gamePlayerId)
                .orElse(null);
        LinkedHashMap<String, Object> content = new LinkedHashMap<>();
        if(gamePlayer == null || gamePlayer.getPlayer().getId() != getCurrentlyLoggedInUser().getId()) {
            content.put("error", "You are not authorized to view this page");
            return new ResponseEntity<>(content, HttpStatus.UNAUTHORIZED);
        }
        else {
            content = createGameDTO(gamePlayer.getGame());
            content.put("ships", gamePlayer.getShips()
                .stream().map(this::createShipDTO));
            content.put("salvoes", gamePlayer.getGame()
                .getGamePlayerSet()
                .stream().map(this::createGamePlayerSalvoesDTO));
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
    }

    @RequestMapping("/leaderboard")
    public LinkedHashSet<Object> getLeaderboardData() {
        return playerRepository
                .findAll()
                .stream().map(this::createLeaderboardDTO)
                .collect(toCollection( LinkedHashSet::new ));
    }

    @RequestMapping("/authentication")
    public LinkedHashMap<String, Object> getAuthenticatedUser() {
        return createPlayerDTO(getCurrentlyLoggedInUser());
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<LinkedHashMap<String, Object>> createNewPlayer(String name, String email, String password) {
        Player player = playerRepository.findByEmail(email);
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        if(player != null) {
            response.put("error", "e-mail address " + email + " is already in use");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        else {
            playerRepository.save(new Player(name, email, passwordEncoder.encode(password)));
            response.put("success", "New player created");
            response.putAll(createPlayerDTO(playerRepository.findByEmail(email)));
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<LinkedHashMap<String, Object>> createGame() {
        Player player = getCurrentlyLoggedInUser();
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        if(getCurrentlyLoggedInUser().getId() == 0) {
            response.put("error", "Please log in to create a new game");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        else {
            Game game = new Game(new Date(), player);
            gameRepository.save(game);
            response.put("game_id", game.getId());
            response.put("gamePlayer_id", game.getGamePlayerSet().stream().findFirst().get().getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
    }

    private LinkedHashMap<String, Object> createGameDTO(Game game) {
        LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getId());
        dto.put("description", game.toString());
        dto.put("created_on", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayerSet().stream().map(this::createGamePlayerDTO));
        dto.put("status", game.getStatus().status());
        dto.put("finished_on", game.getFinishDate());
        dto.put("winner", game.getWinner() != null ? createPlayerDTO(game.getWinner()) : null);
        dto.put("result", game.getResult());
        return dto;
    }

    private LinkedHashMap<String, Object> createGamePlayerDTO(GamePlayer gamePlayer) {
        LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getId());
        dto.put("description", gamePlayer.toString());
        dto.put("joined_on", gamePlayer.getJoinedDate());
        dto.put("player", createPlayerDTO(gamePlayer.getPlayer()));
        dto.put("score", createScoreDTO(gamePlayer.getScore()));
        return dto;
    }

    private LinkedHashMap<String, Object> createPlayerDTO(Player player) {
        LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", player.getId());
        dto.put("name", player.getName());
//        dto.put("username", player.getUserName());
        dto.put("email", player.getEmail());
        dto.put("allTimeScore", player.getAllTimeScore());
        dto.put("allTimeResults", player.getAllTimeResults());
        return dto;
    }

    private LinkedHashMap<String, Object> createShipDTO(Ship ship) {
        LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", ship.getType().type());
        dto.put("shipLocations", ship.getShipLocations());
        return dto;
    }

    private LinkedHashMap<String, Object> createGamePlayerSalvoesDTO(GamePlayer gamePlayer) {
        LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
        dto.put("player", gamePlayer.getPlayer().getEmail());
        dto.put("salvoes", gamePlayer.getSalvoes().stream().map(this::createSalvoDTO));
        return dto;
    }

    private LinkedHashMap<String, Object> createSalvoDTO(Salvo salvo) {
        LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", salvo.getTurn());
        dto.put("salvoLocations", salvo.getSalvoLocations());
        return dto;
    }

    private LinkedHashMap<String, Object> createScoreDTO(Score score) {
        LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
        dto.put("description", score.toString());
        if(score.getResults() != null)
            dto.put("score", score.getResults().score());
        else
            dto.put("score", null);
        dto.put("finished_on", score.getFinishDate());
        return dto;
    }

    private LinkedHashMap<String, Object> createLeaderboardDTO(Player player) {
        LinkedHashMap<String, Object> dto = new LinkedHashMap<>();
        dto.put("player", player.getName());
        dto.put("score", player.getAllTimeScore());
        dto.put("wins", player.getAllTimeResults().get("wins"));
        dto.put("ties", player.getAllTimeResults().get("ties"));
        dto.put("defeats", player.getAllTimeResults().get("defeats"));
        return dto;
    }

    private Player getCurrentlyLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || authentication instanceof AnonymousAuthenticationToken)
            return new Player();
        else
            return playerRepository.findByEmail(authentication.getName());
    }
/*
    private Player getCurrentlyLoggedInUser(Authentication authentication) {
        return playerRepository.findByEmail(authentication.getName());
    }

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
*/
}
