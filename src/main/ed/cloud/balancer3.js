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

Cloud.Balancer = {};

Cloud.Balancer._getArray = function( cursor ){
    if ( ! cursor )
        return [];
    return cursor.toArray();
}

Cloud.Balancer._find = function( coll ){
    var all = Cloud.Balancer._getArray( coll.find( { type : "PROD" , assignable : true } ) );

    if ( all.length == 0 )
        all = Cloud.Balancer._getArray( coll.find( { assignable : true } ) );

    return all;
}

Cloud.Balancer._getAvail = function( coll , name ){
    var all = Cloud.Balancer._find( coll );
    if ( all.length == 0 )
        throw "can't find a valid " + name + " server";
    return all[ Math.floor( all.length * Math.random() ) ];    
}

Cloud.Balancer.getAvailableDB = function(){
    return Cloud.Balancer._getAvail( db.dbs , "db" ).name;
}

Cloud.Balancer.getAvailablePool = function(){
    return Cloud.Balancer._getAvail( db.pools , "appserver pool" ).name;
}


