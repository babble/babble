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

// -----  Basic Structure Stuff -----------

Cloud.Environment = function( name , branch , db , pool ){
    this.name = name;
    this.branch = branch;
    this.db = db;
    this.pool = pool;

    this.id = ObjectId();
    this.aliases = [];
};

// iid is the old name - should deprecate 
Cloud.Environment.prototype.__defineGetter__( "iid" , function(){ return this.id; } );
Cloud.Environment.prototype.__defineSetter__( "iid" , function( id ){ this.id = id; } );

Cloud.Environment.prototype.__defineGetter__( "aliases" , function(){ return this._aliases; } );
Cloud.Environment.prototype.__defineSetter__( "aliases" , 
                                              function( aliases ){ 
                                                  if ( ! aliases )
                                                      return this._aliases = [];

                                                  if ( isArray( aliases ) )
                                                      return this._aliases = aliases;
                                                  
                                                  if ( isString( aliases ) )
                                                      return ( this._aliases = aliases.split( /[, ]+/ ) );
                                                  
                                                  throw "what?";
                                              } )
;

Cloud.Environment.prototype.toString = function(){
    return this.name;
}

Cloud.SiteDB = function( name , server ){
    this.name = name;
    this.server = server;
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

Cloud.Site.prototype.toString = function(){
    return "Site: " + this.name;
};

// -----   Environment Stuff -----------

Cloud.Site.prototype.environmentNames = function(){
    return this.environments.map( function(z){ return z.name } );
}

Cloud.Site.prototype.removeEnvironment = function( identifier ){
    
    var e = this.findEnvironment( identifier );
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

Cloud.Site.prototype.upsertEnvironment = function( name , branch , db , pool , aliases ){
    if ( isObject( name ) && branch == null ){
        var o = name;
        var tempID = o.iid || o.id;

        var e = {};
        if ( tempID )
            e = this.findEnvironmentById( tempID ) || {};

        name = o.name || e.name;
        branch = o.branch || e.branch;
        db = o.db || e.db;
        pool = o.pool || e.pool;
        aliases = o.aliases || e.aliases;

            
    }
        
    if ( ! name )
        throw "envinroment must have a name";

    var e = this.findEnvironmentByName( name );
    
    if ( ! branch ){
        if ( e ) branch = e.branch;
        if ( ! branch )
            throw "envinroment must have a branch";
    }
    
        
    if ( ! db ){
        if ( e ) db = e.db;

        if ( this.findDBByName( name ) )
            db = name;

        if ( ! db )
            throw "envinroment must have a db and can't find a db called [" + name + "]";
    }
    
    if ( ! this.findDBByName( db ) )
        throw "no db with name [" + db + "]";

    if ( ! pool ){
        if ( e ) pool = e.pool;
        if ( ! pool )
            pool = Cloud.Balancer.getAvailablePool();
    }
    
    if ( ! Cloud.Pool.findByName( pool ) ){
        throw "no pool with name [" + pool + "]";
    }

    if ( e ){
        var changed = false;

        if ( e.branch != branch ){
            e.branch = branch;
            changed = true;
        }

        if ( e.db != db ){
            e.db = db;
            changed = true;
        }

        if ( e.pool != pool ){
            e.pool = pool;
            changed = true;
        }

        if ( e.aliases != aliases ){
            var old = e.aliases;
            e.aliases = aliases;
            if ( old == null || old.hashCode() != e.aliases.hashCode() )
                changed = true;
        }

        return changed;
    }
    
    e = new Cloud.Environment( name , branch , db , pool );
    e.aliases = aliases;
    this.environments.add( e );
    return true;
};

// -----   DB Stuff -----------

Cloud.Site.prototype.dbNames = function(){
    return this.dbs.map( function(z){ return z.name } );
}

Cloud.Site.prototype.removeDB = function( identifier ){
    var db = this.findDB( identifier );
    if ( ! db )
        return false;

    this.environments.forEach( 
        function(z){
            if ( z.db == db.name )
                throw "you can't delete db[" + db.name + "] because being used by [" + z.name + "]";
        }
    );
    
    this.dbs = this.dbs.filter(
        function(z){
            return z != db;
        }
    );

    return true;
}

/**
* @return this returns the 10gen server name (prod1)
*/
Cloud.Site.prototype.getDBForEnvironment = function( name ){
    name = name || "www";

    var env = this.findEnvironmentByName( name );

    if ( ! env )
        throw "can't find environment [" + name + "] choices [" + this.environmentNames() + "] site:" + this.name;
    
    
    var db = this.findDBByName( env.db );
    if ( ! db )
        throw "something is wrong, can't find db [" + env.db + "] choices [" + this.dbNames() + "]";
    
    if ( ! db.server )
	throw "we found db [" + env.db + "] but its misconfigured";
    
    return db;
}


Cloud.Site.prototype.getDBUrlForEnvironment = function( envName ){
    return this.getDBUrl( this.getDBForEnvironment( envName ) );
}

Cloud.Site.prototype.getDBUrl = function( dbName ){
    var db = this.findDB( dbName );
    if ( ! db )
        throw "can't find db [" + dbName + "]";

    var url = db.server + "/" + this.name;
    if ( db.envParition )
        url += ":" + db.name;
    return url;

}

Cloud.Site.getDBConnection = function( dbName ){
    return connect( this.getDBUrl( dbName ) );
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

/**
* @return true if a change was made.  (either created or server changed)
*/
Cloud.Site.prototype.upsertDB = function( name , server , userToInsert ){

    if ( isObject( name ) && server == null && name.name ){
        var o = name;
        name = o.name;
        server = o.server;
    }
    

    if ( ! name )
        throw "need to specify db name";
        
    if ( ! server )
        server = Cloud.Balancer.getAvailableDB();
    
    if ( ! Cloud.findDBByName( server ) )
        throw "can't find db server [" + server + "]";
    
    if ( this.findDBByName( name ) ){
        var db = this.findDBByName( name );
        if ( db.server == server )
            return false;
        
        db.server = server;
        return true;
    }

    log( "creating new db [" + name + "] for [" + this.name + "]" );
    
    var db = new Cloud.SiteDB( name , server );
    //db.envParition = true;
    
    this.dbs.add( db );
    
    if ( userToInsert || this.defaultUsers ){
        var conn = this.getDBConnection( db );
        
        if ( this.defaultUsers ){
            for each ( u in this.defaultUsers ){
                log( "storing user [" + u.email + "] in [" + conn + "]" );
                conn.users.save( u );
            }
        }

        if ( userToInsert ){
	    if ( ! db.users.findOne( { email : userToInsert.email } ) ){
	        conn.users.save( userToInsert );
            }
        }

    }

    return true;
};

// -----  Util Stuff -----------

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

Cloud.Site.prototype.updateEnvironment = function( envName , fullReset , dryRun ){
    var command = fullReset ? "reset" : "update";

    var env = this.findEnvironment( envName );
    if ( ! env )
        throw "can't find environment [" + envName + "]";

    var p = db.pools.findOne( { name : env.pool } );
    if ( ! p )
        throw "couldn't find pool [" + env.pool + "]";
    
    var threads = [];

    var hostName = env.name + "." + this.name + ".10gen.com";

    var res = { ok : true };
    for ( var i=0; i<p.machines.length; i++ ){
        var machine = p.machines[i];
	threads.push(
	    fork( 
		function(){
		    try {
			res[machine] = Cloud.Site.resetSiteOnHost( machine , hostName , command , dryRun );
		    }
		    catch ( e ){
			res.ok = false;
			res[machine] = e;
		    }
		}
            )
        );
    }

    for ( var i=0; i<threads.length; i++ ) threads[i].start();
    for ( var i=0; i<threads.length; i++ ) threads[i].join();

    return res;
}

Cloud.Site.resetSiteOnHost = function( machine , hostName , command , dryRun ){
    command = command || "reset";
    var cmd = "ssh " + machine + " \"curl -D - -s -H 'Host: " + hostName + "'\" local.10gen.com:8080/~" + command;
    var res = dryRun ? {} : sysexec( cmd );
    res.cmd = cmd;
    return res;
}

// ---- Static Stuff -------

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
