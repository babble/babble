// test sorting, mainly a test ver simple with no index

db = connect( "test" );
t = db.sorrrt;
t.drop();

t.save({x:3,z:33});
t.save({x:5,z:33});
t.save({x:2,z:33});
t.save({x:3,z:33});
t.save({x:1,z:33});

for( var pass = 0; pass < 2; pass++ ) {
    assert( t.find().sort({x:1})[0].x == 1 );
    assert( t.find().sort({x:1}).skip(1)[0].x == 2 );
    assert( t.find().sort({x:-1})[0].x == 5 );
    assert( t.find().sort({x:-1})[1].x == 3 );
    t.ensureIndex({x:1});

}

assert(t.validate().valid);
