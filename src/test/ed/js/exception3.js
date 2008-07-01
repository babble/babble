
try {
    throw Exception( "abc1" );
}
catch( e ){
    assert( "abc1" == e.toString() );
}


try {
    throw new Exception( "abc2" );
}
catch( e ){
    assert( "abc2" == e.toString() , e.toString() );
}


