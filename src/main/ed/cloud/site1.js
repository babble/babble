// site1.js


Cloud.Environment = function(){
    this.branch = null;
    this.name = this.branch;
    this.db = null;
    this.pool = null;
    this.iid = ObjectId();
};

Cloud.Site = function( name ){
    this.name = name;
    this.created = new Date();
    this.environments = [];
    this.environments._dbCons = Cloud.Environment;
};

Cloud.Site.prototype.findEnvironmentByName = function( name ){
    if ( ! name )
        return null;
    
    var ret = null;
    this.environments.forEach( function(z){
        if ( z.name == name )
            ret = z;
    } );
    return ret;
};

Cloud.Site.prototype.findEnvironmentById = function( iid ){
    if ( ! iid )
        return null;
    
    if ( isString( iid ) )
        iid = ObjectId( iid );
    
    var ret = null;
    this.environments.forEach( function(z){
        if ( z.iid == iid )
            ret = z;
    } );
    return ret;
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
