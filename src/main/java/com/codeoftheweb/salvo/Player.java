package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.stream.Collectors.toCollection;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String name;
    private String email;
//    private final String userName;
    private String password;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private Set<GamePlayer> gamePlayerSet = new LinkedHashSet<>();
    @OneToMany(mappedBy="player", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private Set<Score> scoreSet = new LinkedHashSet<>();

    @Embeddable
    public static class AllTimeResults {
        private int wins;
        private int ties;
        private int defeats;
        public AllTimeResults() { }
        AllTimeResults(int wins, int ties, int defeats) { this.wins = wins; this.ties = ties; this.defeats = defeats;}
        public int wins() { return this.wins; }
        public int ties() { return this.ties; }
        public int defeats() { return this.defeats; }
    }
    private AllTimeResults allTimeResults;


    public Player() {
//        this.userName = "";
        this.allTimeResults = new AllTimeResults(0, 0, 0);
    }

    public Player(String name, String email, String password) {
        this.name = name;
        this.email = email;
//        this.userName = email.split("@")[0];
        this.password = password;
        this.allTimeResults = new AllTimeResults(0, 0, 0);
    }


    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

/*
    public String getUserName() {
        return this.userName;
    }
*/

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public LinkedHashSet<Game> getGames() {
        return this.gamePlayerSet
                .stream().map(GamePlayer::getGame)
                .collect(toCollection( LinkedHashSet::new ));
    }

    public Set<GamePlayer> getGamePlayerSet() {
        return this.gamePlayerSet;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setPlayer(this);
        this.gamePlayerSet.add(gamePlayer);
    }

    public Set<Score> getScoreSet() {
        return this.scoreSet;
    }

    public void addScore(Score score) {
        if(this.scoreSet
                .stream().filter(retrievedScore -> retrievedScore
                        .getGame()
                        .equals(score.getGame()))
                .findFirst()
                .orElse(null) == null) {
            score.setPlayer(this);
            this.scoreSet.add(score);
        }
        updateAllTimeResults(score);
    }

    @JsonIgnore
    public LinkedHashMap<String, Integer> getAllTimeResults() {
        LinkedHashMap<String, Integer> allTimeResultsMap = new LinkedHashMap<>();
        {
            allTimeResultsMap.put("wins", this.allTimeResults.wins);
            allTimeResultsMap.put("ties", this.allTimeResults.ties);
            allTimeResultsMap.put("defeats", this.allTimeResults.defeats);
        }
        return allTimeResultsMap;
    }

    public double getAllTimeScore() {
        return this.allTimeResults.wins() + (double) this.allTimeResults.ties() / 2;
    }

    public void updateAllTimeResults(Score score) {
        switch (score.getResults()) {
            case WIN:
                this.allTimeResults.wins +=1;
                break;
            case TIE:
                this.allTimeResults.ties +=1;
                break;
            case DEFEAT:
                this.allTimeResults.defeats +=1;
                break;
        }
    }

    @Override
    public String toString() {
        return this.name + " (" + this.email + ")";
    }

}
