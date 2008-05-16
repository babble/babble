
x = {}

try {
    print( x.a.b );
}
catch ( e ){
    assert( e.getStackTrace() );
}

try {
    throw 5;
}
catch ( e ){
    assert( 5 == e );
    assert( scope.currentException() );
    assert( scope.currentException().getStackTrace() );
    assert( scope.currentException().getMessage() == "5" );
}


    

try {
    throw 5;
}
catch ( e ){
    assert( 5 == e );
    assert( scope.currentException() );
    assert( scope.currentException().getStackTrace() );
    assert( scope.currentException().getMessage() == "5" );

    try {
        throw 9;
    }
    catch ( z ){
        assert( z == 9 );
        assert( scope.currentException().getMessage() == "9" );
    }

    assert( 5 == e );
    assert( scope.currentException() );
    assert( scope.currentException().getStackTrace() );
    assert( scope.currentException().getMessage() == "5" );
}


    
