// zeus_resolve.js


/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
            crap = crap.replace( /!extra/g , "" );

            envs += crap;
            
            if ( ! best )
                best = e;

            if ( e.name == "www" )
                best = e;
            
            if ( e.aliases ){
                for ( var aliasNumber in e.aliases ){
                    var alias = e.aliases[aliasNumber];
                    var crap = oneEnv.replace( /!branch/g , alias );
                    crap = crap.replace( /!pool/g , e.pool );
                    crap = crap.replace( /!extra/g , "http.setHeader( \"Host\" , string.replace( http.getHostHeader() , \"" +  alias  + "\" , \"" + e.name  + "\" ) );" );

                    envs += crap;
                }

            }

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
