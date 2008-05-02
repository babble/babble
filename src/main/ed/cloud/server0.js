
Cloud.Server = function( name ){
    var r = /(\w\w\w)\-(\w\w+)\-n(\d+)\./.exec( name );

    if ( ! r ){
        this.bad = true;
        this.location = "unknown";
        this.provider = "unknown";
        this.number = 0;
        return;
    }
    
    this.location = r[1];
    this.provider = r[2];
    this.number = parseInt( r[3] );

};

Cloud.Server.prototype.gridServer = function(){
    if ( this.bad )
        return null;
    return this.location + "-" + this.provider + "-grid.10gen.com";
};

me = new Cloud.Server( SERVER_NAME );
log.debug( "me : " + tojson( me ) );
log.debug( "grid server : " + me.gridServer() );
db = connect( "grid" , me.gridServer() );
