
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
    
}

for ( var val in counts ){
    var num = counts[val];
    var dbcount = t.count( { foo : val } );
    assert( dbcount == num , " dbcount:" + dbcount + " mycount:" + num );
}


for ( var val in counts ){
    
    t.find( { foo : val } ).forEach( 
        function(z){
            z.abc = "asdhasdlahsldhaslkdas";
            z.abc = z.abc + z.abc + z.abc;
            z.abc = z.abc + z.abc + z.abc;
            z.abc = z.abc + z.abc + z.abc;
            t.save( z );
        }
    );
}

for ( var val in counts ){
    var num = counts[val];
    var dbcount = t.count( { foo : val } );
    assert( dbcount == num , " dbcount:" + dbcount + " mycount:" + num );
}
