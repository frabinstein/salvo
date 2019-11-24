package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    public enum Results {
        WIN(1, "won"), TIE(0.5, "tied"), DEFEAT(0, "lost");
        private final double score;
        private final String result;
        Results(double score, String result) { this.score = score; this.result = result;}
        public double score() { return this.score; }
        public String result() {return this.result; }
    }
    private Results results;


    public Score() { }

    public Score(Game game, Player player) {
        this.game = game;
        this.player = player;
    }

    public Score(GamePlayer gamePlayer) {
        this.game = gamePlayer.getGame();
        this.player = gamePlayer.getPlayer();
    }


    public long getId() {
        return this.id;
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return this.game
                .getGamePlayerSet()
                .stream().filter(gamePlayer -> gamePlayer
                        .getPlayer()
                        .equals(this.player))
                .findFirst()
                .orElse(null);
    }

    public Date getFinishDate() {
        return this.getGame().getFinishDate();
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
        this.getPlayer().addScore(this);
    }

    @Override
    public String toString() {
        Player opponent = this.game
                .getPlayers()
                .stream().filter(player -> player != this.getPlayer())
                .findFirst()
                .orElse(null);
        String message = "";
        message += "Score for " + this.player.getName();
        message += " on game #" + this.game.getId();
        if(opponent == null)
            message += ", waiting for an opponent to join. ";
        else
            message += " against " + opponent.getName() + ". ";
        message += this.game.getResult();
        return message;
    }

}
