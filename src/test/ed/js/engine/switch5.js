
a = 5;
b = 5;

switch ( a ){
case b: print( "here" ); break;
case 6: print( "6" );
default: print( "d" );
}


function foo( z ){
    a = 5;
    switch( z ){
    case 1 : print( "a" ); break;
    case 2 : print( "b" );
    case 3 : print( "c" ); break;
    case 4 : print( "d" );
    case a : print( "d" );
    default: print( "ZZ" );
    }
}

foo( 0 );
foo( 1 );
foo( 2 );
foo( 3 );
foo( 4 );
foo( 6 );
foo( 5 );


function foo( z ){
    e = "e";
    switch( z ){
    case "a" : print( "a" ); break;
    case "b" : print( "b" );
    case "c" : print( "c" ); break;
    case "d" : print( "d" );
    case e : print( "d" );
    default: print( "ZZ" );
    }
}

foo( "a" );
foo( "b" );
foo( "c" );
foo( "d" );
foo( "e" );
foo( "sad" );
