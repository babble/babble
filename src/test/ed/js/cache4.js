
counters = {
    print : 0 ,
    call : 0
}

counters.print = 0;

function myprint(){
    counters.print++;
}

print = myprint;
assert.eq( 0 , counters.print );

counters.call = 0;

function foo(){
    counters.call++;
    print( "asd" );
    return 17;
}

assert.eq( 17 , foo() );
assert.eq( 1 , counters.print );
assert.eq( 1 , counters.call );

assert.eq( 17 , foo() );
assert.eq( 2 , counters.print );
assert.eq( 2 , counters.call );

assert.eq( 17 , foo.cache( 100000 ) );
assert.eq( 3 , counters.print );
assert.eq( 3 , counters.call );

assert.eq( 17 , foo.cache( 100000 ) );
assert.eq( 4 , counters.print );
assert.eq( 3 , counters.call );

assert.eq( 17 , foo.cache( 100000 , 5 ) );
assert.eq( 5 , counters.print );
assert.eq( 4 , counters.call );


assert.eq( 17 , foo.cache( 5 , 17 ) );
assert.eq( 6 , counters.print );
assert.eq( 5 , counters.call );

assert.eq( 17 , foo.cache( 5 , 17 ) );
assert.eq( 7 , counters.print );
assert.eq( 5 , counters.call );

sleep( 7 );

assert.eq( 17 , foo.cache( 5 , 17 ) );
assert.eq( 8 , counters.print );
assert.eq( 6 , counters.call );


