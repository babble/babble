
jxp.lib.bar();

function mapUrlToJxpFile( uri ){
    if ( uri.match( /a/ ) )
        return "index.jxp";
    
    return null;
}
