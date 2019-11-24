package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    final private Date joinedDate;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;
    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private Set<Ship> ships = new LinkedHashSet<>();
    @OneToMany(mappedBy="gamePlayer", fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private Set<Salvo> salvoes = new LinkedHashSet<>();


    public GamePlayer() {
        this.joinedDate = new Date();
    }

    public GamePlayer(Game game, Player player, Date joinedDate) {
        this.game = game;
        this.player = player;
        this.joinedDate = joinedDate;
        this.game.addScore(new Score(this));
    }


    public long getId() {
        return this.id;
    }

    public Date getJoinedDate() {
        return this.joinedDate;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Set<Ship> getShips() {
        return this.ships;
    }

    public void addShip(Ship ship) {
        ship.setGamePlayer(this);
        this.ships.add(ship);
    }

    public Set<Salvo> getSalvoes() {
        return this.salvoes;
    }

    public void addSalvo(Salvo salvo) {
        salvo.setGamePlayer(this);
        this.salvoes.add(salvo);
    }

    public Score getScore() {
        return this.game
                .getScoreSet()
                .stream().filter(score -> score
                        .getPlayer()
                        .equals(this.player))
                .findFirst()
                .orElse(null);
    }

    public Date getFinishDate() {
        return this.getGame().getFinishDate();
    }

    @Override
    public String toString() {
        return this.player.getName() + " started playing game #" + this.game.getId() + " at " + this.joinedDate
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss, 'on' EEEE MMMM dd yyyy"));
    }

}
