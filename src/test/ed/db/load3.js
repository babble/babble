db = connect( "foo" );

t = db.t;

for ( var i=0; i<100000; i++ ){
    for ( var j=0; j<5; j++ ){
        var s = md5( Math.random() ).substring( 0 , j );
        t.save( { name : s } );
    }
    
}

t.ensureIndex( { name : 1 } );

