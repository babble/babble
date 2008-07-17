// in.js -- test $in queries

db=connect("test");
t=db.intest;
t.drop();

t.save( { x: 3 } );

assert( null == t.findOne({x:{ $in : [1,4]}}) );
assert( t.findOne({x:{ $in : [1,3,4]}}).x == 3 );

 t.save({x:4});               
 t.save({x:"foo"});
 t.save({x:2});

assert( t.find({x:{ $in : [1,4,3,"foo"]}}).length() == 3 );

t.ensureIndex({x:1});

assert( t.find({x:{ $in : [1,4,3,"foo"]}}).length() == 3 );

assert( t.validate().valid );
