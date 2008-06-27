
/**

db.git
 { name : "corejs" , 
   branches : { 
     master : { hash : "abc" } ,
     www    : { hash : "abc" } 
   }
 }

*/

Cloud.Git = {};

Cloud.Git.ensureHash = function( repos , branch , hash ){
    log.git.debug( "ensureHash " + repos + "/" + branch + "/" + hash );
    
    var g = Cloud.Git.findByName( repos );
    
    if ( ! g ){
	log.git.debug( "\t creating new repos " );
	g = new Cloud.Git.Repository( repos );
    }

    if ( g.isSite() ){
	var s = g.getSite();
    }

    var changed = g.ensureHash( branch , hash );
    if ( changed ){
	log.git.debug( "\t saving" );
	db.git.save( g );
    }
};


Cloud.Git.Repository = function( name ){
    this.name = name;
    this.created = new Date();
};


Cloud.Git.Repository.prototype.isSite = function(){
    return this.name.match( /^sites\// );
};

Cloud.Git.Repository.prototype.getSite = function(){
    if ( ! this.isSite() ) 
	throw "not a site";

    if ( this.site )
	return this.site;
    
    var siteName = this.name.substring( 6 );
    this.site = Cloud.Site.forName( siteName , true );
    return this.site;
};

/**
* @return true if anything changed
*/
Cloud.Git.Repository.prototype.ensureHash = function( branch , hash ){
    if ( ! this.branches )
	this.branches = {};
    
    if ( ! this.branches[ branch ] ){
	this.branches[branch] = { hash : hash , created : new Date() };
	return true;
    }
    
    if ( this.branches[branch].hash == hash )
	return false;

    this.branches[branch].hash = hash;
    return true;

};

Cloud.Git.findByName = function( name ){
    return db.git.findOne( { name : name }  );
};


db.git.setConstructor( Cloud.Git.Repository );
if ( me.real )
    db.git.ensureIndex( { name : 1 } );
