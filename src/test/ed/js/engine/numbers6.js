

for ( var i=1; i<50; i++ ){
    var n = 2;
    for ( var j=0; j<i; j++ )
        n *= 2;
    
    print( n );

    for ( var j=0; j<i; j++ )
        n = n / 2;
    
    print( n );
}
