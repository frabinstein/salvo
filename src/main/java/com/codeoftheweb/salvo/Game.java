package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.codeoftheweb.salvo.Score.Results.*;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    final private Date creationDate;
    private boolean finished = false;
    private Date finishDate;
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private Set<GamePlayer> gamePlayerSet = new LinkedHashSet<>();
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private Set<Score> scoreSet = new LinkedHashSet<>();


    public Game() {
        this.creationDate = new Date();
    }

    public Game(Date creationDate) {
        this.creationDate = creationDate;
    }


    public long getId() {
        return this.id;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public Set<GamePlayer> getGamePlayerSet() {
        return this.gamePlayerSet;
    }

    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setGame(this);
        this.gamePlayerSet.add(gamePlayer);
    }

    public LinkedHashSet<Player> getPlayers() {
        return this.gamePlayerSet
                .stream().map(GamePlayer::getPlayer)
                .collect(toCollection( LinkedHashSet::new ));
    }

    public Set<Score> getScoreSet() {
        return this.scoreSet;
    }

    public void addScore(Score score) {
        score.setGame(this);
        this.scoreSet.add(score);
    }

    public boolean isFinished() {
        return finished;
    }

    public void finishGame(String winner) {
        switch(winner) {
            case "Player 1":
                this.getScoreSet().stream().collect(toList()).get(0).setResults(WIN);
                this.getScoreSet().stream().collect(toList()).get(1).setResults(DEFEAT);
                break;
            case "Player 2":
                this.getScoreSet().stream().collect(toList()).get(0).setResults(DEFEAT);
                this.getScoreSet().stream().collect(toList()).get(1).setResults(WIN);
                break;
            case "Tie":
                this.getScoreSet().forEach(score -> score.setResults(TIE));
                break;
        }
        this.finished = true;
        this.setFinishDate(new Date());
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    private Player getWinner() {
        return this.scoreSet
                .stream().filter(score -> score.getResults().score()==1)
                .findFirst()
                .orElse(null)
                .getPlayer();
    }

    public String getResult() {
        if(this.isFinished()) {
            switch (this.getScoreSet()
                    .stream().findAny()
                    .orElse(new Score())
                    .getResults()) {
                case WIN:
                case DEFEAT:
                    return "Game won by " + this.getWinner().getName();
                case TIE:
                    return "Game was a tie";
            }
        }
        return "Game is still pending";
    }

    @Override
    public String toString() {
        String message = "";
        message += "Game #" + this.id;
        message += ". Created at " + this.creationDate
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss, 'on' EEEE MMMM dd yyyy"));
        message += ". Players: " + this.getPlayers()
                .stream().map(Player::getName)
                .reduce(((player, player2) -> player+" and "+player2))
                .orElse(null)
                + ". ";
        message += this.getResult() + ".";
        if (this.isFinished()) message += " Finished at " + this.finishDate
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss, 'on' EEEE MMMM dd yyyy"));
        return message;
    }

}
