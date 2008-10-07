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
    assert.eq( t.find().sort({x:-1}).skip(0)[0].x , 5 );
    assert.eq( t.find().sort({x:-1}).skip(1)[0].x , 3 );
    t.ensureIndex({x:1});

}

assert(t.validate().valid);


db.bar.drop();
db.bar.save({x:'a'});
db.bar.save({x:'aba'});
db.bar.save({x:'zed'});
db.bar.save({x:'foo'});

for( var pass = 0; pass < 2; pass++ ) { 
  assert( db.bar.find().sort({'x': 1}).limit(1).next().x == 'a' );
  assert( db.bar.find().sort({'x': 1}).next().x == 'a' );
  assert( db.bar.find().sort({'x': -1}).limit(1).next().x == 'zed' );
  assert( db.bar.find().sort({'x': -1}).next().x == 'zed' );
}

assert(db.bar.validate().valid);