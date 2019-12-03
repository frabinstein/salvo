var authentication = "/api/authentication";
var authenticatedUser = {};

var uri = "/api/games";

var openGames = [];
var ongoingGames = [];
var finishedGames = [];

var open = "#open";
var ongoing = "#ongoing";
var finished = "#finished";


function main(json) {
    json.forEach( game => {
        switch(game.status) {
            case "open":
                openGames.push(game);
                break;
            case "not started":
            case "ongoing":
                ongoingGames.push(game);
                break;
            case "finished":
                finishedGames.push(game);
                break;
        };
    });

    $(open).html(buildGamesTable("Open", openGames));
    $(ongoing).html(buildGamesTable("Ongoing", ongoingGames));
    $(finished).html(buildGamesTable("Finished", finishedGames));

    getAuthentication();
};


function buildGamesTable(heading, games) {
    let content = "<thead><tr><th colspan='3'>" + heading + " games</th></tr></thead>";
    content += "<tbody>";
    content += games.map(game => buildGameRow(game));
    content += "</tbody>";
    return content;
}

function buildGameRow(game) {
    let content = "";
    content += "<tr><td class='col1'>Game #" + game.id + "</td><td class='col2'>";
    switch(game.status) {
        case "open":
            content += "<b>" + game.gamePlayers[0].player.name + "</b> is waiting for an opponent";
            content += "</td><td class='col3'><button type='button' class='join' hidden>Join game</button></td></tr>";
            break;
        case "not started":
        case "ongoing":
            content += "<b>" + game.gamePlayers.map(gamePlayer => gamePlayer.player.name).join("</b> vs. <b>") + "</b>";
            content += "</td><td class='col3'>"
                + game.gamePlayers.map(gamePlayer =>
                    "<button type='button' class='return' player='"
                    + gamePlayer.player.id
                    + "' onclick='redirectTo(\"/web/game.html?gp="
                    + gamePlayer.id
                    + "\")' hidden>Return to your game</button>")
                    .join("")
                + "</td></tr>";
            break;
        case "finished":
            content += "<b>" + game.gamePlayers.map(gamePlayer => gamePlayer.player.name).join("</b> vs. <b>") + "</b>";
            content += "<b>" + game.result + "</b>";
            content += "</td><td class='col3'></td></tr>";
            break;
    };
    return content;
}


function getAuthentication() {
    $.getJSON(authentication, json => authenticatedUser = json)
        .done(function() {
            buildHeader();
            if(!$.isEmptyObject(authenticatedUser))
                showButtons();
        })
        .fail( function( textStatus, error) {
            console.log("Request Failed: " + JSON.stringify(textStatus));
            console.log("Error: " + JSON.stringify(error));
        });
}

function buildHeader() {
    let content = "";
    if($.isEmptyObject(authenticatedUser)) {
        content += "<p>Welcome! Please login or sign up:</p>";

        content += "<div class='forms'>";
        content += "<form name='login-form' onsubmit='return false'>";
        content += "<div class='warning'></div>";
        content += "<label>E-mail: <input type='text' name='email'></label>";
        content += "<label>Password: <input type='password' name='password'></label>";
        content += "<input type='submit' value='Log in' id='login'>";
        content += "</form>";

        content += "<form name='signup-form' onsubmit='return false'>";
        content += "<div class='warning'></div>";
        content += "<label>Name: <input type='text' name='name'></label>";
        content += "<label>E-mail: <input type='text' name='email'></label>";
        content += "<label>Password: <input type='password' name='password'></label>";
        content += "<label>Retype password: <input type='password' name='password2'></label>";
        content += "<input type='submit' value='Sign up' id='signup'>";
        content += "</form>";
        content += "</div>";
    }
    else {
        content += "<p>Hello " + authenticatedUser.name + "!</p>";

        content += "<div class='forms'>";
        content += "<form onsubmit='return false'>";
        content += "<button type='button' id='logout'>Log out</button>";
        content += "</form>";
        content += "</div>";
    }
    $("header").html(content);
}

function showButtons() {
    $('#open .col3 .join').removeAttr("hidden");
    $('#ongoing .col3 .return[player="'+authenticatedUser.id+'"]').removeAttr("hidden");
}


function trimField(form, field) {
    $('[name="'+form+'"] [name="'+field+'"]').val($('[name="'+form+'"] [name="'+field+'"]').val().trim());

}

function validateForm(form) {
    if(form == "signup-form")
        if(!validateField(form, "name"))
            return false;
    if(!validateField(form, "email"))
        return false;
    if(!validateField(form, "password"))
        return false;
    if(form == "signup-form") {
        if(($('[name="'+form+'"] [name="password"]').val().localeCompare($('[name="'+form+'"] [name="password2"]').val())) !== 0) {
            alert("Passwords don't match");
            return false;
        }
    }
    return true;
}

function validateField(form, field) {
    if($('[name="'+form+'"] [name="'+field+'"]').val() == "") {
        alert(field + " can't be blank");
        return false;
    }
    if((field == "email" || field == "password") && $('[name="'+form+'"] [name="'+field+'"]').val().indexOf(" ") != -1) {
        alert(field + " can't contain blank spaces");
        return false;
    }
    if(field == "email" && $('[name="'+form+'"] [name="'+field+'"]').val().indexOf("@") == -1) {
        alert("incorrect " + field + " format");
        return false;
    }
    return true;
}


function login(email, password) {
    $.post("/api/login", { "email": email, "password": password })
        .done(function() {
            getAuthentication("");
        })
        .fail(function() { $('[name="login-form"] .warning').html("<p><span class='error'>Login error!</span> Please check your credentials</p>"); })
}

function logout() {
    $.post("/api/logout")
        .done(function() {
            getAuthentication("You have logged out successfully");
            redirectTo("/web/games.html");
        })
        .fail(function() { $("header").prepend("<p class='error'>Logout error!</p>"); })
}

function signup(name, email, password) {
    $.post("/api/players", { "name": name, "email": email, "password": password } )
        .done(function() { login(email, password); })
        .fail(function(xhr) { $('[name="signup-form"] .warning').html("<p><span class='error'>There was an error:</span> " + xhr.responseJSON.error + ". Please try again.</p>"); })
}


function redirectTo(target) {
    window.location.href = target;

}


$.getJSON(uri, json => main(json))
    .fail( function( textStatus, error) {
        console.log("Request Failed: " + JSON.stringify(textStatus));
        console.log("Error: " + JSON.stringify(error));
    });

$('header').on('click', '#login', "login-form", function(event) {
    trimField(event.data, "email");
    if(validateForm(event.data))
        login($('[name="'+event.data+'"] [name="email"]').val(), $('[name="'+event.data+'"] [name="password"]').val())
});

$('header').on('click', '#signup', "signup-form", function(event) {
    trimField(event.data, "email");
    if(validateForm(event.data))
        signup($('[name="'+event.data+'"] [name="name"]').val(), $('[name="'+event.data+'"] [name="email"]').val(), $('[name="'+event.data+'"] [name="password"]').val())
});

$('header').on('click', '#logout', function() { logout() });
