// zeus_resolve.js


Cloud.Zeus = {};

Cloud.Zeus.resolveTS = function(){
    var s = openFile( "src/main/ed/cloud/zeus_resolve_setup.ts" ).asString() + "\n";

    var oneFile = openFile( "src/main/ed/cloud/zeus_resolve_one.ts" ).asString();
    var oneEnv = openFile( "src/main/ed/cloud/zeus_resolve_one_env.ts" ).asString();

    s += "if ( 2 == 1 ){}\n";

    db.sites.find().sort( { name : 1 } ).forEach( function(z){

        var n = z.name;
        
        if ( ! n.match( /\./ ) )
            n += ".com";
        
        var nr = n.reverse();
        
        var one = oneFile;
        
        one = one.replace( /!domainReverse/g , nr );        
        one = one.replace( /!domain/g , n );
        one = one.replace( /!length/g , "" + n.length );
        
        var envs = "if ( 2 == 1 ){}\n";

        var best = null;

        z.environments.forEach( function(e){

            var crap = oneEnv.replace( /!branch/g , e.name );
            crap = crap.replace( /!pool/g , e.pool );

            envs += crap;

            if ( ! best )
                best = e;

            if ( e.name == "www" )
                best = e;
            
        } );
        
        if ( best ){
            envs += "else { \n";
            envs += "    pool.use( \"" + best.pool + "\" );\n";
            envs += "}\n" ;
        }
        else {
            log.error( "no environments for [" + z.name + "]" );
        }

        var ne = "";
        envs.trim().split( "[\r\n]+" ).forEach( function( s ){
            ne += "    " + s + "\n";
        } );
            
        one = one.replace( /!envs/g , ne.trim() );
        
        s += one;
        
    } );

    return s;
}
