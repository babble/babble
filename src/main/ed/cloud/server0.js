
Cloud.Server = function( name ){
    var r = /(\w\w\w)\-(\w\w+)\-n(\d+)\./.exec( name );
    if ( ! r )
	throw "bad server name : " + name;
    
    this.location = r[1];
    this.provider = r[2];
    this.number = parseInt( r[3] );

};

Cloud.Server.prototype.gridServer = function(){
    return this.location + "-" + this.provider + "-grid.10gen.com";
};

me = new Cloud.Server( SERVER_NAME );
log.debug( "me : " + tojson( me ) );
log.debug( "grid server : " + me.gridServer() );
db = connect( "grid" , me.gridServer() );