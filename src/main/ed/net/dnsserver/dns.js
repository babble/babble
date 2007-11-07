
function eval( response , host , type ){
    //print( "host:" + host );
    //print( "type:" + type );
    
    add( host , "C" , 30 , "foo.com." );
    add( host , "C" , 3600 , "bar.com" );
    add( "asdom.shop.com." , "A" , 3600 , "127.0.0.1" );
}
