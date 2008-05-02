# find the real host name

$h = http.getHostHeader();

## check for things like
## http://static.10gen.com/www.alleyinsider.com/foo
if ( $h == "origin.10gen.com" 
     || $h == "secure.10gen.com" 
     || $h == "static.10gen.com" ){
   $h = http.getPath();
   while ( string.startsWith( $h , "/" ) ) 
       $h = string.substring( $h , 1 , string.len( $h ) );
   $idx = string.find( $h , "/" );
   if ( $idx > 0 )
       $h = string.substring( $h , 0 , $idx - 1 );
}

## handle alleyinsider.10gen.com
$h = string.iReplace( $h , ".10gen.com" , ".com" );
$hr = string.reverse( $h );

#log.info( "use host : [" . $h  . "]" );
