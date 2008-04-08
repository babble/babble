
# !domain
else if ( $hr == "!domainReverse" || string.startsWith( $hr , "!domainReverse." ) ){

    $branch = string.substring( $h , 0 , string.length( $h ) - ( !length + 1 ) );
    if ( string.length( $branch ) == 0 )
        $branch = "www";

    if ( string.endsWith( $branch , "." ) )
        $branch = string.substring( $branch , 0 , string.length( $branch ) - 2 );
    
    
    log.info( "domain [!domain] branch[" . $branch . "]" );

    !envs
}
