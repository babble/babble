 // count2.js

db = connect( "test" );
t = db.counttest2;

t.drop();

function c(q) { 
    return db.$cmd.findOne({count:"counttest2", query:q}).n;
}

for( var i = 0; i < 512; i++ )
    t.save( { i:i, j:i%4, k:i%8 } );

for( var pass = 0; pass < 2; pass++ ) {
    assert( c() == 512 );
    t.ensureIndex({_id:ObjId()});
}

assert( t.validate().valid );
