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

Cloud.Pool = function(){
    this.name = null;
    this.envType = null;
    this.machines = [];
};


Cloud.DB = function(){
    this.name = null;
    this.type = null;
    this.machine = null;
};

Cloud.findDBByName = function( name ){
    return db.dbs.findOne( { name : name } );
}

db.pools.setConstructor( Cloud.Pool );
db.dbs.setConstructor( Cloud.DB );

if ( me.real ){
    db.pools.ensureIndex( { name : 1 } );
    db.dbs.ensureIndex( { machine : 1 } );
}
