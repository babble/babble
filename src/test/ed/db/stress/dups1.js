
db = connect( "test" );
t = db.dups1;
t.clean();
t.drop();

t.save( { foo : "sillyasdasdas" } );
t.ensureIndex( { foo : 1 } );


var counts = {};

for ( var i=0; i<100000; i++ ){

    var val = "key" + i % 100;

    var num = counts[val] || 0;
    num++;
    counts[val] = num;
    
    t.save( { foo : val} );
    
    if ( i % 10000 == 0 )
        assert( t.validate().valid ) ;
}

print( "done inserting" );
assert( t.validate().valid );

for ( var val in counts ){
    var num = counts[val];
    var dbcount = t.count( { foo : val } );
    assert( dbcount == num , " dbcount:" + dbcount + " mycount:" + num );
}

assert( t.validate().valid );

print( "going to go crazy" );

for ( var val in counts ){
    
    var num = 0;
    
    t.find( { foo : val } ).forEach( 
        function(z){
            z.abc = "asdhasdlahsldhaslkdas";
            z.abc = z.abc + z.abc + z.abc;
            z.abc = z.abc + z.abc + z.abc;
            z.abc = z.abc + z.abc + z.abc;
            t.save( z );

            
            if ( num % 10000 == 0 )
                assert( t.validate().valid );
        }
    );
}

assert( t.validate().valid );

for ( var val in counts ){
    var num = counts[val];
    var dbcount = t.count( { foo : val } );
    assert( dbcount == num , " dbcount:" + dbcount + " mycount:" + num );
}

assert( t.validate().valid );
