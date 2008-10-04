// pool1.js

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

// --- POOL ----

Cloud.Pool = function(){
    this.name = null;
    this.envType = null;
    this.machines = [];
};

Cloud.Pool.findByName = function( name ){
    return db.pools.findOne( { name : name } );
}

Cloud.Pool.getAllNames = function(){
    return db.pools.find().sort( { name : 1 } ).toArray().map( 
        function(z){ 
            return z.name 
        } 
    ); 
}

Cloud.Pool.prototype.toString = function(){
    return "pool [" + this.name + "] machines [" + this.machines + "]";
}

db.pools.setConstructor( Cloud.Pool );

if ( me.real ){
    db.pools.ensureIndex( { name : 1 } );
}

// --- DB ----

Cloud.DB = function(){
    this.name = null;
    this.type = null;
    this.machine = null;
};

Cloud.DB.findByName = function( name ){
    return db.dbs.findOne( { name : name } );    
};



Cloud.DB.getAllNames = function(){
    return db.dbs.find().sort( { name : 1 } ).toArray().map( 
        function(z){ 
            return z.name 
        } 
    ); 
}

Cloud.DB.getAllDBNames = Cloud.DB.getAllNames;

Cloud.findDBByName = function( name ){
    return Cloud.DB.findByName( name );
};

db.dbs.setConstructor( Cloud.DB );

if ( me.real ){
    db.dbs.ensureIndex( { machine : 1 } );
}
