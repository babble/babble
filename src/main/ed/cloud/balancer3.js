// balancer3.js

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

var getNumber = function( str ){
    var p = /(\d+)/;
    var r = p.exec( str );
    if ( ! r )
        return 0;
    return Number( r[1] );
}


var pickBest = function( a , b ){
    if ( ! a )
        return b;

    if ( ! b )
        return a;
    
    if ( getNumber( a ) < getNumber( b ) )
        return b;

    return a;
}

Cloud.Balancer = {};

Cloud.Balancer.getAvailableDB = function(){

    var best = null;

    for each ( var mydb in db.dbs.find().toArray() ){
        if ( mydb.type != "PROD" )
            continue;
        
        best = pickBest( best , mydb.name );
    }
    
    if ( ! best )
        throw "can't find a valid db server";
    
    return best;
}

Cloud.Balancer.getAvailablePool = function(){

    var best = null;

    for each ( var mypool in db.pools.find().toArray() ){

        if ( mypool.type != "PROD" )
            continue;
        
        best = pickBest( best , mypool.name );
    }
    
    if ( ! best )
        throw "can't find a valid pool";
    
    return best;
}
k

