// pool1.js

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

