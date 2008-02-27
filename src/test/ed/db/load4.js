

db = connect( "test" );
core.db.db();

t = db.load4;

t.remove( {}  );
drop( "load4" );

t.save( { name : "foo" } );
t.ensureIndex( { name : 1 } );

var max = 100000;

for ( var i=0; i<max; i++ ){

    var o = { name : "name" + i };
    t.save( o );
    

    j = Math.floor( Math.random() * i );

    var c = t.find( { name : "name" + j } ).toArray();
    assert( c.length == 1 );
    c[ "a" + Math.random() ] = Math.random();
    t.save( c );
    
    assert( t.find( { name : "name" + j } ).length() == 1 );
    assert( t.find( { name : "name" + j } ).sort( { name : 1 } ).length() == 1 );
    assert( t.find( { name : "name" + j } ).sort( { name : -1 } ).length() == 1 );
    
    if ( i > 0 && i % ( max / 100 ) == 0 )
        print( ( 100 * i / max ) + "%" );

    t.find().limit(2).next();
}
