
var o = { a : 1 }
assert( o.a == 1 );
o.a = 2;
assert( o.a == 2 );

o.setReadOnly( true );
try {
    o.a = 3;
}
catch ( e ){}

assert( o.a == 2 );


o.setReadOnly( false );
o.a = 3;
assert( o.a == 3 );
