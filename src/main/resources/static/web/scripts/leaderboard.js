var authentication = "/api/authentication";
var authenticatedUser = {};

var uri = "/api/leaderboard";

var heading = "#heading";
var table = "#table";

var leaderboard = {};


function main(json) {
    leaderboard = json.forEach(x => x.played = x.wins+x.ties+x.defeats);
    leaderboard = sortLeaderboard(json);
    $(heading).append(buildHTMLHeading());
    $(table).append(buildHTMLTable(leaderboard));
};

function sortLeaderboard(playersData) {
    playersData.sort( (x, y) => x.player.localeCompare(y.player));
    playersData.sort( (x, y) => x.played - y.played);
    playersData.sort( (x, y) => y.score - x.score);
    return playersData;
};

function buildHTMLHeading() {
    let content = "";

    //Build heading:
    content += "<h1>Leaderboard</h1>";
    return content;
};

function buildHTMLTable(data) {
    let content = "";

    //Open table:
    content += "<table border=1>";

    //Build table header:
    content += "<thead class='table_header'><tr><th>Player</th><th>Total score</th><th>Won</th><th>Tied</th><th>Lost</th><th>Played</th></tr></thead>";

    //Build table body:
    content += "<tbody>";
    data.forEach(player => {
        content += "<tr>";
        content += "<td>" + player.player + "</td>";
        content += "<td>" + player.score.toFixed(1) + "</td>";
        content += "<td>" + player.wins + "</td>";
        content += "<td>" + player.ties + "</td>";
        content += "<td>" + player.defeats + "</td>";
        content += "<td>" + player.played + "</td>";
        content += "</tr>";
    });

    //Close table:
    content += "</tbody>";
    content += "</table>";

    return content;
};

function login() {
  $.post("/api/login",
         { "email": $('input[name ="email"]').val(),
           "password": $('input[name ="password"]').val() })
   .done(function() { console.log("Logged in successfully"); })
   .fail(function() { console.log("Login error"); })
   .always(function() { getAuthentication(); });
}

function logout() {
  $.post("/api/logout")
   .done(function() { console.log("Logged out successfully"); })
   .fail(function() { console.log("Logout error"); })
   .always(function() { getAuthentication(); });
}

function getAuthentication() {
    $.getJSON(authentication, json => authenticatedUser = json)
        .fail( function( textStatus, error) {
            console.log("Request Failed: " + JSON.stringify(textStatus));
            console.log("Error: " + JSON.stringify(error));
        });
}


$.getJSON(uri, json => main(json))
    .fail( function( textStatus, error) {
        console.log("Request Failed: " + JSON.stringify(textStatus));
        console.log("Error: " + JSON.stringify(error));
    });

getAuthentication();

$('#login').click(login);
$('#logout').click(logout);

