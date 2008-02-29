
db = connect( "test" );
core.db.db();

t = db.load5;
t.remove( {}  );
drop( "load5" );

init = 100000;

for ( var i=0; i<init; i++ )
    t.save( { name : "foo" + i } );

print( t.find().length() );
assert( t.find().length() == init );


for ( var i=0; i<10000; i++ ){
    c = t.find();
    
    for ( var j=0; j<init/2; j++ )
        c.next();
    
    for ( var j=0; j<init/2; j++ ){
        o = t.findOne( { name : "foo" + j } );
        if ( o )
            o.name += "as";
        t.save( o );
    }

    for ( var j=0; c.hasNext() && j<init/2; j++ )
        c.next();        
    
}
