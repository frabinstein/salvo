var authentication = "/api/authentication";
var authenticatedUser = {};

var uri = "/api/games";

var games = "#games";


function buildHTML(json) {
    $(games).append("<ol>" + json.map(game => "<li>" + game.description + "<br>Players in this game: " + game.gamePlayers.map(gamePlayer => gamePlayer.player.email).join(", ") + "<br><br></li>").join("") + "</ol>");
};

function getAuthentication(firstLine) {
    $.getJSON(authentication, json => authenticatedUser = json)
        .done(function() {
            buildHeader(firstLine);
        })
        .fail( function( textStatus, error) {
            console.log("Request Failed: " + JSON.stringify(textStatus));
            console.log("Error: " + JSON.stringify(error));
        });
}

function buildHeader(firstLine) {
    let content = "";
    if(jQuery.isEmptyObject(authenticatedUser)) {
        content += "<p>" + firstLine + "</p>";

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
        content += "<button id='logout'>Log out</button>";
        content += "</form>";
        content += "</div>";
    }
    $("header").html(content);
};

function validateForm(form) {
    if(form == "signup-form")
        if(!validateField(form, "name"))
            return false;
    if(!validateField(form, "email"))
        return false;
    if(!validateField(form, "password"))
        return false;
    if(form == "signup-form") {
        if(($('[name="'+form+'"] > > [name="password"]').val().localeCompare($('[name="'+form+'"] > > [name="password2"]').val())) !== 0) {
            alert("Passwords don't match");
            return false;
        }
    }
    return true;
}

function validateField(form, field) {
    if($('[name="'+form+'"] > > [name="'+field+'"]').val() == "") {
        alert(field + " can't be blank");
        return false;
    }
    if((field == "email" || field == "password") && $('[name="'+form+'"] > > [name="'+field+'"]').val().indexOf(" ") != -1) {
        alert(field + " can't contain blank spaces");
        return false;
    }
    if(field == "email" && $('[name="'+form+'"] > > [name="'+field+'"]').val().indexOf("@") == -1) {
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
        .fail(function() { $('[name="login-form"] > .warning').html("<p><span class='error'>Login error!</span> Please check your credentials</p>"); })
}

function logout() {
    $.post("/api/logout")
        .done(function() {
            getAuthentication("You have logged out successfully");
            window.location.href = "/web/games.html";
        })
        .fail(function() { $("header").prepend("<p class='error'>Logout error!</p>"); })
}

function signup(name, email, password) {
    $.post("/api/players", { "name": name, "email": email, "password": password } )
        .done(function() { login(email, password); })
        .fail(function(xhr) { $('[name="signup-form"] > .warning').html("<p><span class='error'>There was an error:</span> " + xhr.responseJSON.error + ". Please try again.</p>"); })
}


$.getJSON(uri, json => buildHTML(json))
    .fail( function( textStatus, error) {
        console.log("Request Failed: " + JSON.stringify(textStatus));
        console.log("Error: " + JSON.stringify(error));
    });

getAuthentication("Welcome! Please login or sign up:");

$('header').on('click', '#login', "login-form", function(event) {
    $('[name="'+event.data+'"] > > [name="email"]').val($('[name="'+event.data+'"] > > [name="email"]').val().trim());
    if(validateForm(event.data))
        login($('[name="'+event.data+'"] > > [name="email"]').val(), $('[name="'+event.data+'"] > > [name="password"]').val())
});

$('header').on('click', '#signup', "signup-form", function(event) {
    $('[name="'+event.data+'"] > > [name="email"]').val($('[name="'+event.data+'"] > > [name="email"]').val().trim());
    if(validateForm(event.data))
        signup($('[name="'+event.data+'"] > > [name="name"]').val(), $('[name="'+event.data+'"] > > [name="email"]').val(), $('[name="'+event.data+'"] > > [name="password"]').val())
});

$('header').on('click', '#logout', function() { logout() });
