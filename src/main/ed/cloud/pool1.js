// pool1.js

Cloud.Pool = function(){
    this.name = null;
    this.envType = null;
    this.machines = [];
};

db.pools.ensureIndex( { name : 1 } );
db.pools.setConstructor( Cloud.Pool );

Cloud.DB = function(){
    this.name = null;
    this.type = null;
    this.machine = null;
};

Cloud.findDBByName = function( name ){
    return db.dbs.findOne( { name : name } );
}

db.dbs.setConstructor( Cloud.DB );
db.dbs.ensureIndex( { machine : 1 } );

