
db = connect( "tests" ); 

t1 = db.table1; 
t2 = db.table2; 

t1.remove( {} );
t2.remove( {} );

o1 = {a: 'a', b: "b"}; 
o2 = {e: 'e', o: o1}; 

t1.save(o1); 
t2.save(o2); 

origo1id = o1._id;

o1 = null; 
o2 = null; 
o2 = t2.findOne(); 

//print( tojson( o2 ) );

assert( "a" == o2.o.a);
assert( "b" == o2.o.b); 

o1 = o2.o; 
o1.a = "c"; 

t2.save(o2);

assert( t2.findOne( o2._id ).o.a == "c" );
assert( t2.findOne( o2._id ).o._id == origo1id );
assert( t1.findOne( origo1id ).a == "c" );


