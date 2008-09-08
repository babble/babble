
function go( e ){
    print( unescape( e ) );
    print( unescape( escape( unescape( e ) ) ) );
    print( unescape( escape( unescape( e ) ) ) == unescape( e ) );
    
}

go( "%C2%A3" );

