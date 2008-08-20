// site1.js


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

Cloud.Environment = function( name ){
    this.branch = null;
    this.name = name || this.branch;
    this.db = null;
    this.pool = null;
    this.id = ObjectId();
    this.aliases = [];
};

// iid is the old name - should deprecate 
Cloud.Environment.prototype.__defineGetter__( "iid" , function(){ return this.id; } );
Cloud.Environment.prototype.__defineSetter__( "iid" , function( id ){ this.id = id; } );

Cloud.Environment.prototype.toString = function(){
    return this.name;
}

Cloud.SiteDB = function(){
    this.name = null;
    this.server = null;
    this.id = ObjectId();
};

// did is the old name - should deprecate 
Cloud.SiteDB.prototype.__defineGetter__( "did" , function(){ return this.id; } );
Cloud.SiteDB.prototype.__defineSetter__( "did" , function( id ){ this.id = id; } );

Cloud.Site = function( name ){
    this.name = name;
    this.created = new Date();
    this.giturl = null;

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

Cloud.Site.prototype.removeEnvironment = function( identifier ){
    
    var e = findEnvironment( identifier );
    if ( ! e )
        return false;

    this.environments = this.environments.filter( 
        function(z){
            return z != e;    
        }
    );
    
    return true;
}

Cloud.Site.prototype.findEnvironment = function( identifier ){
    if ( identifier && isObject( identifier ) && identifier.id && identifier.name )
        return identifier;
    
    for ( var i=0; i<arguments.length; i++ ){
        var e = this.findEnvironmentByName( arguments[i] ) || this.findEnvironmentById( arguments[i] );
        if ( e )
            return e;
    }
    return null;
}

Cloud.Site.prototype.findEnvironmentByName = function( name ){
    if ( ! name )
        return null;
    
    var ret = null;
    var ali = null;
    this.environments.forEach( function(z){

        if ( z.name == name )
            ret = z;

        if ( z.aliases && z.aliases.contains( name ) )
            ali = z;
    } );

    return ret || ali;
};

Cloud.Site.prototype.findEnvironmentById = function( id ){
    if ( ! id )
        return null;
    
    if ( isString( id ) ){
        if ( ! ObjectId.isValid( id ) )
            return null;
        id = ObjectId( id );
    }
    
    var ret = null;
    this.environments.forEach( function(z){
        if ( z.id == id )
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

Cloud.Site.prototype.findDB = function( identifier ){
    if ( identifier && isObject( identifier ) && identifier.id && identifier.name )
        return identifier;

    for ( var i=0; i<arguments.length; i++ ){
        var db = this.findDBByName( arguments[i] ) || this.findDBById( arguments[i] );
        if ( db )
            return db;
    }
    return null;
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

Cloud.Site.prototype.findDBById = function( id ){
    if ( ! id )
        return null;
    
    if ( isString( id ) ){
        if ( ! ObjectId.isValid( id ) )
            return null;
        id = ObjectId( id );
    }
    
    var ret = null;
    this.dbs.forEach( function(z){
        if ( z.id == id )
            ret = z;
    } );
    return ret;
};

Cloud.Site.prototype.getGitBranchNames = function( force ){

    if ( ! this.giturl || this.giturl.startsWith( "ssh://git.10gen.com/data/gitroot/" ) ){
        var g = db.git.findOne( { name : "sites/" + this.name } );
        if ( ! g ){
            g = new Cloud.Git.Repository();
            g.ensureHash( "master" , "0" );
            db.git.save( g );
        }
        return g.branches.keySet();
    }
    
    var base = javaCreate( "java.io.File" , "/data/tmp/externalgit/" );
    base.mkdirs();
    var root = javaCreate( "java.io.File" , base , this.name );
    
    if ( root.exists() ){
        var conf = javaStatic( "ed.util.GitUtils" , "readConfig" , root );
        if ( conf.remote.origin.url != this.giturl ){
            log.git.info( "giturl changed for site [" + this.name + "]  was [" + conf.remote.origin.url + "] now is [" + this.giturl + "]" );
            javaStatic( "ed.io.FileUtil" , "deleteDirectory" , root );
        }
    }
    
    if ( ! root.exists() ){
        var msg = "clone [" + this.giturl + "] to [" + root + "]";
        
        log.git.info( msg );
        if ( ! javaStatic( "ed.util.GitUtils" , "clone" , this.giturl , base , this.name ) ){
            log.git.error( "failed " + msg );
            throw "couldn't " + msg;
        }
        force = true;
    }
    
    var now = new Date();

    var ts = javaCreate( "java.io.File" , root , ".git/10gents" );
    if ( ! ts.exists() ){
        force = true;
        ts.createNewFile();
    }
    else {
        if ( ( ts.lastModified() + ( 1000 * 60 * 5 ) ) < now.getTime() )
            force = true;
    }

    if ( force ){
        log.git.info( " doing full update for [" + this.name + "]" );
        javaStatic( "ed.util.GitUtils" , "fullUpdate" , root );
        ts.setLastModified( now.getTime() );
    }
    
    return javaStatic( "ed.util.GitUtils" , "getAllBranchAndTagNames" , root );
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
