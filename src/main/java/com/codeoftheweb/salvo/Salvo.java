package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Salvo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;
    private int turn;
    @ElementCollection
    @Column(name="salvoLocations")
    private List<String> salvoLocations = new ArrayList<>();


    public Salvo() { }

    public Salvo(int turn, GamePlayer gamePlayer, ArrayList<String> salvoLocations) {
        this.turn = turn;
        this.gamePlayer = gamePlayer;
        this.salvoLocations = salvoLocations;
    }


    public long getId() {
        return this.id;
    }

    public GamePlayer getGamePlayer() {
        return this.gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Player getPlayer() {
        return this.gamePlayer.getPlayer();
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public List<String> getSalvoLocations() {
        return this.salvoLocations;
    }

    public void addSalvoLocation(String salvoLocation) {
        this.salvoLocations.add(salvoLocation);
    }

}
