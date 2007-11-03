
jxp.lib.wiky();

function allowed( req , res , uri ){
    
    var auth = req.getHeader("Authorization");

    if ( auth && auth.match( /^Basic / ) ){
        auth = auth.substring( 6 );
        auth = Base64.decode( auth );
        var idx = auth.indexOf( ":" );

        if ( idx > 0 ){
            var username = auth.substring( 0 , idx );
            var password = auth.substring( idx + 1 );

            print( "user attempt:" + username );
            
            if ( username == "abc" && password == "17" ){
                user = username;
                return;
            }
        }
    }

    
    res.setHeader( "WWW-Authenticate" , "Basic realm=\"LNC\"" );
    return "no";
}
