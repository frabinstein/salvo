package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;
    @ElementCollection
    @Column(name="shipLocations")
    private List<String> shipLocations = new ArrayList<>();

    public enum Type {
        CARRIER(5, "Carrier"), BATTLESHIP(4, "Battleship"), SUBMARINE(3, "Submarine"), DESTROYER(3, "Destroyer"), PATROL_BOAT(2, "Patrol Boat");
        private final int length;
        private final String type;
        Type(int length, String type) { this.length = length; this.type = type;}
        public int length() { return this.length; }
        public String type() {return this.type; }
    }
    private Type type;


    public Ship() { }

    public Ship(Type type, ArrayList<String> shipLocations, GamePlayer gamePlayer) {
        this.type = type;
        this.shipLocations = shipLocations;
        this.gamePlayer = gamePlayer;
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

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<String> getShipLocations() {
        return this.shipLocations;
    }

    public void addShipLocation(String shipLocation) {
        this.shipLocations.add(shipLocation);
    }

}
