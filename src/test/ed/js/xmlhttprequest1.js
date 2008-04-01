
x = new XMLHttpRequest( "GET" , "http://www.10gen.com/~~/headers" );
assert( x.send() );
assert( x.responseText );
assert( x.responseText.match( /Host/ ) );
assert( x.readyState == 4)

x = new XMLHttpRequest( "GET" , "http://www.10gen.com/~~/headers" );
var last = 0;
x.onreadystatechange = function(){
    last = this.readyState;
}
assert( x.send() );
assert( x.readyState < 4 );

for ( var i=0; i<1000; i++ ){
    if ( last == 4 )
        break;
    sleep( 5 );
}

assert( last == 4 );
