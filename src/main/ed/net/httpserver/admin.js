// admin js

// --- CommandProcesor ---

function CommandProcesor(){
    this.command = "";
}

CommandProcesor.prototype.gotLetter = function( letter ){
    if ( letter == "\n" )
        letter = "<br>";
    this.command += letter;
    debug( this.command );
}

CommandProcesor.it = new CommandProcesor();

// -----------------------

function debug( msg ){
    document.getElementById( "debugjs" ).innerHTML = msg;
}

function adminOnLoad(){
    
}

window.onkeypress = function( a ){
    CommandProcesor.it.gotLetter( String.fromCharCode( a.which ) );
}

