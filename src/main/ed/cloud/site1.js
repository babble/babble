// site1.js


Cloud.Instance = function(){
    this.branch = null;
    this.name = this.branch;
    this.db = null;
    this.pool = null;
};

Cloud.Site = function( name ){
    this.name = name;
    this.created = new Date();
    this.instances = [];
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
