
db = connect( "test" );
t = db.readOnly1;
t.remove( {} );

assert( t.find().length() == 0 );

t.save( { name : "a" } );
assert( t.find().length() == 1 );

db.setReadOnly( true );

myException = null;
try {
    t.save( { name : "b" } );
}
catch ( e ){
    myException = e;
}
assert( myException );


dbStrict = false;
t.save( { name : "b" } );
assert( t.find().length() == 1 );


dbStrict = true;
myException = null;
try {
    t.save( { name : "b" } );
}
catch ( e ){
    myException = e;
}
assert( myException );
