// site1.js


Cloud.Instance = function(){
    this.servers = [];
    this.branch = null;
    this.db = null;
    this.name = this.branch;
};

Cloud.Site = function( name ){
    this.name = name;
    this.created = new Date();
    this.instances = {};
    this.instances._dbCons = Cloud.Instance;
};

Cloud.Site.forName = function( name , create ){
    var s = db.sites.findOne( { name : name } );
    
    if ( s || ! create )
	return s;

    s = new Cloud.Site( name );
    db.sites.apply( s );
    return s;
};


db.sites.setConstructor( Cloud.Site );
db.sites.ensureIndex( { name : 1 } );