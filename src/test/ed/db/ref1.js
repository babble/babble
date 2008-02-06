
db = connect( "tests" ); 

t1 = db.table1; 
t2 = db.table2; 

o1 = {a: 'a', b: "b"}; 
o2 = {e: 'e', o: o1}; 

t1.save(o1); 
t2.save(o2); 

o1 = null; 
o2 = null; 
o2 = t2.findOne(); 

assert( "a" == o2.o.a);
assert( "b" == o2.o.b); 

o1 = o2.o; 
o1.a = "c"; 

delete o1._id; 
delete o1._ns; 

db.asd.save( o1 );


