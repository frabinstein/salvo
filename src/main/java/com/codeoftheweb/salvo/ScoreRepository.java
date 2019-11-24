package com.codeoftheweb.salvo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findByPlayer(Player player);
    List<Score> findByGame(Game game);
    List<Score> findByResults(Score.Results results);

}
