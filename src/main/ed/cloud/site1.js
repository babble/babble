// site1.js


Cloud.Environment = function(){
    this.branch = null;
    this.name = this.branch;
    this.db = null;
    this.pool = null;
    this.iid = ObjectId();
};

Cloud.SiteDB = function(){
    this.name = null;
    this.server = null;
    this.did = ObjectId();
};

Cloud.Site = function( name ){
    this.name = name;
    this.created = new Date();
    
    this.environments = [];
    this.environments._dbCons = Cloud.Environment;

    this.dbs = [];
    this.dbs._dbCons = Cloud.SiteDB;
};

Cloud.Site.prototype.environmentNames = function(){
    return this.environments.map( function(z){ return z.name } );
}

Cloud.Site.prototype.dbNames = function(){
    return this.dbs.map( function(z){ return z.name } );
}

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

/**
this returns the 10gen server name (prod1)
*/
Cloud.Site.prototype.getDatabaseServerForEnvironmentName = function( name ){
    name = name || "www";

    var env = this.findEnvironmentByName( name );

    if ( ! env )
        throw "can't find environment [" + name + "] choices [" + this.environmentNames() + "] site:" + this.name;
    
    
    var db = this.findDBByName( env.db );
    if ( ! db )
        throw "something is wrong, can't find db [" + env.db + "] choices [" + this.dbNames() + "]";

    if ( ! db.server )
	throw "we found db [" + env.db + "] but its misconfigured";

    return db.server;
}

Cloud.Site.prototype.findDBByName = function( name ){
    if ( ! name )
        return null;
    
    var ret = null;
    this.dbs.forEach( function(z){
        if ( z.name == name )
            ret = z;
    } );
    return ret;
};

Cloud.Site.prototype.findDBById = function( did ){
    if ( ! did )
        return null;
    
    if ( isString( did ) )
        did = ObjectId( did );
    
    var ret = null;
    this.dbs.forEach( function(z){
        if ( z.did == did )
            ret = z;
    } );
    return ret;
};

Cloud.Site.prototype.toString = function(){
    return "Site: " + this.name;
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
if ( me.real ){
    db.sites.ensureIndex( { name : 1 } );
}

