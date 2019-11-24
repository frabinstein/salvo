var gamePlayerId = queryStringParameter(location.search).gp;
var uri = "/api/game_view/" + gamePlayerId;

var gamePlayerData = "#gamePlayerData";
var shipData = "#shipData";
var salvoData = "#salvoData";

var activePlayer = {};
var opponent = {};
var ships = [];
var playerSalvoShots = {};
var opponentSalvoShots = {};


function queryStringParameter(queryString) {
  var parameterObject = {};
  var regEx = /(?:[?&]([^?&#=]+)(?:=([^&#]*))?)(?:#.*)?/g;

  queryString.replace(regEx, function(match, param, val) {
    parameterObject[decodeURIComponent(param)] = val === undefined ? "" : decodeURIComponent(val);
  });

  return parameterObject;
};

function main(json) {
    activePlayer = json.gamePlayers.filter(gamePlayer => gamePlayer.id === parseInt(gamePlayerId))[0].player;
    opponent = json.gamePlayers.filter(gamePlayer => gamePlayer.id !== parseInt(gamePlayerId))[0].player;
    ships = json.ships;
    playerSalvoShots = json.salvoes.filter(playerSalvoes => playerSalvoes.player === activePlayer.email)[0];
    opponentSalvoShots = json.salvoes.filter(playerSalvoes => playerSalvoes.player === opponent.email)[0];

    $(gamePlayerData).append(buildGamePlayerDataHTML());
    $(shipData).append(buildShipsIntroHTML());
    $(salvoData).append(buildSalvoesIntroHTML());
    $(shipData).append(buildBaseHTMLGrid("ships"));
    $(salvoData).append(buildBaseHTMLGrid("salvoes"));

    populateShips("ships");
    populateSalvoes(opponentSalvoShots, "ships");
    populateSalvoes(playerSalvoShots, "salvoes");
}


function buildGamePlayerDataHTML() {
    let content = "";
    content += "Hello " + activePlayer.name + ",<br><br>";
    content += "You are playing this game against " + opponent.name + "<br><br>";
    return content;
};

function buildShipsIntroHTML() {
    let content = "";
    content += "These are your ships:";
    content += "<ol>" + ships.map(ship => "<li>" + ship.type + " (" + ship.shipLocations + ")</li>").join("") + "</ol>";
    return content;
};

function buildSalvoesIntroHTML() {
    let content = "";
    content += "These are the salvoes you have shot:";
    content += "<ul>";
    content += playerSalvoShots.salvoes.map(salvo => "<li>Turn " + salvo.turn + ": " + salvo.salvoLocations + "</li>").join("") + "</ul>";
    return content;
};

function buildBaseHTMLGrid(tableClass) {
    let content = "";

    //Open table:
    content += "<table border=1 class='" + tableClass + "'><colgroup><col class='table_header'></colgroup>";

    //Build header row:
    content += "<thead class='table_header'><tr>";
    for(let i = 0; i < 11; i++) {
        if(i === 0)
            content += "<th></th>";
        else
            content += "<th>" + (i) + "</th>";
    };
    content += "</tr></thead>";

    //Build table body:
    content += "<tbody>";
    for(let i = 0; i < 10; i++) {
        content += "<tr>";
        for(let j = 0; j < 11; j++) {
            let row = String.fromCharCode('A'.charCodeAt(0)+i);
            let cell = row.concat(j);
            if(j === 0)
                content += "<th>" + row + "</th>";
            else {
                content += "<td id='" + tableClass + cell + "'></td>";
            }
        };
        content += "</tr>";
    };
    content += "</tbody>";

    //Close table:
    content += "</table>";

    return content;
};


function populateShips(tableClass){
    ships.forEach(ship => ship.shipLocations.forEach(cell => $("#"+tableClass+cell).addClass("occupied")));

};

function populateSalvoes(salvoShots, tableClass){
    salvoShots.salvoes.forEach(salvo => salvo.salvoLocations.forEach(cell => {
        $("#"+tableClass+cell).addClass("shot");
        $("#"+tableClass+cell).append(salvo.turn);
        }));
};


$.getJSON(uri, json => main(json))
    .fail( function( textStatus, error) {
        console.log("Request Failed: " + JSON.stringify(textStatus));
        console.log("Error: " + JSON.stringify(error));
    });
