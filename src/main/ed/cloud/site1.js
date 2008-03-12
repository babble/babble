// site1.js

Cloud.Site = function( name ){
    this.name = name;
};

db.sites.setConstructor( Cloud.Site );
db.sites.ensureIndex( { name : 1 } );