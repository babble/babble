
db = connect( "tests" ); 

db.$cmd.findOne({reseterror:1});
assert( db.$cmd.findOne({getlasterror:1}).err == null );
assert( db.$cmd.findOne({getpreverror:1}).err == null );

db.$cmd.findOne({forceerror:1});
assert( db.$cmd.findOne({getlasterror:1}).err != null );
assert( db.$cmd.findOne({getpreverror:1}).err != null );
db.foo.findOne();
assert( db.$cmd.findOne({getlasterror:1}).err == null );
assert( db.$cmd.findOne({getpreverror:1}).err != null );
assert( db.$cmd.findOne({getpreverror:1}).nPrev == 2 );

db.$cmd.findOne({reseterror:1});
assert( db.$cmd.findOne({getpreverror:1}).err == null );
