package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.codeoftheweb.salvo.Ship.Type.*;

@SpringBootApplication
public class SalvoApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
        return (args) -> {
            //Sample players:
            String[][] players = {{"Jack Bauer","j.bauer@ctu.gov","24"},{"Chloe O'Brian","c.obrian@ctu.gov","42"},{"Kim Bauer","kim_bauer@gmail.com","kb"},{"Tony Almeida","t.almeida@ctu.gov","mole"}};
            for(int i = 0; i < players.length; i++){
                playerRepository.save(new Player(players[i][0], players[i][1], passwordEncoder.encode(players[i][2])));
            }

            //Sample gamePlayers and games:
                //(GamePlayer constructor creates score too)
            Date date = new Date();
            String[][] gamePlayersPlayers = { {"j.bauer@ctu.gov","j.bauer@ctu.gov","c.obrian@ctu.gov","c.obrian@ctu.gov","t.almeida@ctu.gov","kim_bauer@gmail.com","t.almeida@ctu.gov","kim_bauer@gmail.com"}, {"c.obrian@ctu.gov","c.obrian@ctu.gov","t.almeida@ctu.gov","j.bauer@ctu.gov","j.bauer@ctu.gov","N/A","N/A","t.almeida@ctu.gov"} };
            for(int i = 0; i < gamePlayersPlayers[0].length; i++){
                Game game = new Game(Date.from(date.toInstant().plusSeconds(3600 * i)));
                gameRepository.save(game);
                Player player1 = playerRepository
                        .findByEmail(gamePlayersPlayers[0][i]);
                if(player1 != null) {
                    GamePlayer gamePlayer1 = new GamePlayer(game, player1, game.getCreationDate());
                    gamePlayerRepository.save(gamePlayer1);
                    playerRepository.save(player1);
                }
/*                    game.addScore(new Score(gamePlayer1));*/
                Player player2 = playerRepository
                        .findByEmail(gamePlayersPlayers[1][i]);
                if(player2 != null) {
                    GamePlayer gamePlayer2 = new GamePlayer(game, player2, game.getCreationDate());
                    gamePlayerRepository.save(gamePlayer2);
                    playerRepository.save(player2);
                }
/*                    game.addScore(new Score(gamePlayer2));*/
                gameRepository.save(game);
            }

            //Sample ships:
            int[] shipGames = {1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 8, 8, 8, 8};
            String[] shipPlayers = {"j.bauer", "j.bauer", "j.bauer", "c.obrian", "c.obrian", "j.bauer", "j.bauer", "c.obrian", "c.obrian", "c.obrian", "c.obrian", "t.almeida", "t.almeida", "c.obrian", "c.obrian", "j.bauer", "j.bauer", "t.almeida", "t.almeida", "j.bauer", "j.bauer", "kim_bauer", "kim_bauer", "kim_bauer", "kim_bauer", "t.almeida", "t.almeida"};
            Ship.Type[] shipTypes = {DESTROYER, SUBMARINE, PATROL_BOAT, DESTROYER, PATROL_BOAT, DESTROYER, PATROL_BOAT, SUBMARINE, PATROL_BOAT, DESTROYER, PATROL_BOAT, SUBMARINE, PATROL_BOAT, DESTROYER, PATROL_BOAT, SUBMARINE, PATROL_BOAT, DESTROYER, PATROL_BOAT, SUBMARINE, PATROL_BOAT, DESTROYER, PATROL_BOAT, DESTROYER, PATROL_BOAT, SUBMARINE, PATROL_BOAT};
            String[][] shipLocations = { {"H2", "H3", "H4"}, {"E1", "F1", "G1"}, {"B4", "B5"}, {"B5", "C5", "D5"}, {"F1", "F2"}, {"B5", "C5", "D5"}, {"C6", "C7"}, {"A2", "A3", "A4"}, {"G6", "H6"}, {"B5", "C5", "D5"}, {"C6", "C7"}, {"A2", "A3", "A4"}, {"G6", "H6"}, {"B5", "C5", "D5"}, {"C6", "C7"}, {"A2", "A3", "A4"}, {"G6", "H6"}, {"B5", "C5", "D5"}, {"C6", "C7"}, {"A2", "A3", "A4"}, {"G6", "H6"}, {"B5", "C5", "D5"}, {"C6", "C7"}, {"B5", "C5", "D5"}, {"C6", "C7"}, {"A2", "A3", "A4"}, {"G6", "H6"} };

            for(int i = 0; i < shipGames.length; i++){
                int finalI = i;
                shipRepository.save(
                        new Ship(shipTypes[i],
                                new ArrayList<>(Arrays.asList(shipLocations[i])),
                                gamePlayerRepository
                                        .findByGame(gameRepository
                                                .findById((long) shipGames[i])
                                                .orElse(null))
                                        .stream().filter(gamePlayer -> gamePlayer
                                                .getPlayer()
                                                .getEmail().split("@")[0]
                                                .equals(shipPlayers[finalI]))
                                        .findFirst()
                                        .orElse(null)));
            }

            //Sample salvoes:
            int[] salvoGames = {1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5};
            int[] salvoTurns = {1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 3};
            String[] salvoPlayerAs = {"j.bauer", "j.bauer", "j.bauer", "j.bauer", "c.obrian", "c.obrian", "c.obrian", "c.obrian", "t.almeida", "t.almeida", ""};
            String[][] salvoPlayerALocations = { {"B5", "C5", "F1"}, {"F2", "D5"}, {"A2", "A4", "G6"}, {"A3", "H6"}, {"G6", "H6", "A4"}, {"A2", "A3", "D8"}, {"A3", "A4", "F7"}, {"A2", "G6", "H6"}, {"A1", "A2", "A3"}, {"G6", "G7", "G8"}, {} };
            String[] salvoPlayerBs = {"c.obrian", "c.obrian", "c.obrian", "c.obrian", "t.almeida", "t.almeida", "j.bauer", "j.bauer", "j.bauer", "j.bauer", "j.bauer"};
            String[][] salvoPlayerBLocations = { {"B4", "B5", "B6"}, {"E1", "H3", "A2"}, {"B5", "D5", "C7"}, {"C5", "C6"}, {"H1", "H2", "H3"}, {"E1", "F2", "G3"}, {"B5", "C6", "H1"}, {"C5", "C7", "D5"}, {"B5", "B6", "C7"}, {"C6", "D6", "E6"}, {"H1", "H8"} };

            for(int i = 0; i < salvoGames.length; i++){
                int finalI = i;

                if(!salvoPlayerAs[i].equals("")) {
                    salvoRepository.save(
                            new Salvo(salvoTurns[i],
                                    gamePlayerRepository
                                            .findByGame(gameRepository
                                                    .findById((long) salvoGames[i])
                                                    .orElse(null))
                                            .stream().filter(gamePlayer -> gamePlayer
                                                    .getPlayer()
                                                    .getEmail().split("@")[0]
                                                    .equals(salvoPlayerAs[finalI]))
                                            .findFirst()
                                            .orElse(null),
                                    new ArrayList<>(Arrays.asList(salvoPlayerALocations[i]))));
                }

                if(!salvoPlayerBs[i].equals("")) {
                    salvoRepository.save(
                            new Salvo(salvoTurns[i],
                                    gamePlayerRepository
                                            .findByGame(gameRepository
                                                    .findById((long) salvoGames[i])
                                                    .orElse(null))
                                            .stream().filter(gamePlayer -> gamePlayer
                                                    .getPlayer()
                                                    .getEmail().split("@")[0]
                                                    .equals(salvoPlayerBs[finalI]))
                                            .findFirst()
                                            .orElse(null),
                                    new ArrayList<>(Arrays.asList(salvoPlayerBLocations[i]))));
                }
            }

            //Apply sample results:
            String[] scores = {"Player 1","Tie","Player 1","Tie","N/A","N/A","N/A","N/A"};
            for(int i = 0; i < scores.length; i++){
                if(!scores[i].equals("N/A")) {
                    Game game = gameRepository.findById((long) i+1)
                            .orElse(null);
                    game.finishGame(scores[i]);
                    gameRepository.save(game);
                    game.getPlayers()
                            .forEach(player -> playerRepository.save(player));
                }
            }

        };
    }

}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inputName-> {
            Player player = playerRepository
                    .findByEmail(inputName);
            if (player != null) {
                return new User(player.getEmail(), player.getPassword(),
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                throw new UsernameNotFoundException("Unknown user: " + inputName);
            }
        });
    }
}

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
//                .antMatchers("/**").hasAuthority("ADMIN")
//                .antMatchers("/api/players").anonymous()
                .antMatchers("/api/authentication", "/api/login", "/api/logout", "/api/players").permitAll()
                .antMatchers("/web/games.html", "/api/games", "/web/leaderboard.html", "/api/leaderboard", "/web/styles/**", "/web/scripts/**", "/favicon.ico").permitAll()
//                .antMatchers("/rest/**").denyAll()
                .antMatchers("/**").authenticated();
//                .anyRequest().denyAll();
//                .anyRequest().authenticated()
//                .and().formLogin();
        http.formLogin()
                .usernameParameter("email")
                .passwordParameter("password")
                .loginPage("/api/login");
        http.logout().logoutUrl("/api/logout");

        // turn off checking for CSRF tokens
        http.csrf().disable();
        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));
        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));
        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));
        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}
