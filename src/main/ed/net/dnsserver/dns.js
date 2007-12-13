
function eval( host , type , domain ){
    print( "host: " + host + " type: " + type );
    
    if ( type == "A" || type == "CNAME" ){
        add( host , "A" , 30 , local );
    }

    add( domain , "NS" , 7200 , "ns1.10gen.com." );
    add( domain , "NS" , 7200 , "ns2.10gen.com." );

}
