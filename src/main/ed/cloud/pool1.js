// pool1.js

Cloud.Pool = function(){
    this.name = null;
    this.envType = null;
    this.machines = [];
};

db.pools.ensureIndex( { name : 1 } );
db.dbs.ensureIndex( { machine : 1 } );

