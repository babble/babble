
db = connect( "test" );
t = db.readOnly1;
t.remove( {} );

assert( t.find().length() == 0 );

t.save( { name : "a" } );
assert( t.find().length() == 1 );

db.setReadOnly( true );
t.save( { name : "b" } );
assert( t.find().length() == 1 );

e = null;
dbStrict = true;
try {
    t.save( { name : "b" } );
}
catch ( e ){
    myException = e;
}
assert( myException );
