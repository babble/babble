
var o = { a : 1 }
assert( o.a == 1 );
o.a = 2;
assert( o.a == 2 );

o.lock();
try {
    o.a = 3;
}
catch ( e ){}

assert( o.a == 2 );


try {
    o.setReadOnly( false );
    assert( false );
}
catch ( e ){}
