
// printing is commented out due to spidermonkey bug

function go( e ){
    var x=unescape(e);
//    print( x );
    print( x.length );
    for( i=0; i<x.length; i++) {
//        print( x[i] );
        print( x.charCodeAt(i) );
    }

    print( "---" );

    var x=unescape( escape ( unescape (e) ) );
//    print( x );
    print( x.length );
    for( i=0; i<x.length; i++) {
//        print( x[i] );
        print( x.charCodeAt(i) );
    }

    print( "---" );

    print( unescape( escape( unescape( e ) ) ) == unescape( e ) );
    
}

go( "%C2%A3" );

go( "%uFF34%uF235%u3423" );
