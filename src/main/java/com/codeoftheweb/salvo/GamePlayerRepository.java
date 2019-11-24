package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Date;
import java.util.List;

@RepositoryRestResource
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    List<GamePlayer> findByJoinedDate(Date joinedDate);
    List<GamePlayer> findByPlayer(Player player);
    List<GamePlayer> findByGame(Game game);

}
