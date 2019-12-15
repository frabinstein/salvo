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

    public enum Status {
        OPEN("open"), NOT_STARTED("not started"), ONGOING("ongoing"), FINISHED("finished");
        private final String status;
        Status(String status) { this.status = status;}
        public String status() {return this.status; }
    }
    private Status status;

    private Date finishDate;

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private Set<GamePlayer> gamePlayerSet = new LinkedHashSet<>();
    @OneToMany(mappedBy="game", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private Set<Score> scoreSet = new LinkedHashSet<>();


    public Game() {
        this.creationDate = new Date();
        this.status = Status.OPEN;
    }

    public Game(Date creationDate, Player player) {
        this.creationDate = creationDate;
        this.addPlayer(player, creationDate);
        this.status = Status.OPEN;
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

    public void addPlayer(Player player, Date joinedDate) {
        GamePlayer gamePlayer = new GamePlayer(this, player, joinedDate);
        this.gamePlayerSet.add(gamePlayer);
        this.status = Status.ONGOING;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
        this.setStatus(Status.FINISHED);
        this.setFinishDate(new Date());
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Player getWinner() {
        return this.scoreSet
                .stream().filter(score -> score.getResults() == WIN)
                .findFirst()
                .orElse(new Score())
                .getPlayer();
    }

    public String getResult() {
        if(this.getStatus() == Status.FINISHED) {
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
        if (this.getStatus() == Status.FINISHED) message += " Finished at " + this.finishDate
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss, 'on' EEEE MMMM dd yyyy"));
        return message;
    }

}
