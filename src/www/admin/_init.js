jxp.lib.wiky();
function allowed( req , res , uri ){
    res.setHeader( "WWW-Authenticate" , "Basic realm=\"LNC\"" );
    return "no";
}
