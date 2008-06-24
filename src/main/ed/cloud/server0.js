
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

Cloud.Server.prototype.toString = function(){
    return "{Server.  location:" + this.location + " provider:" + this.provider + " n:" + this.number + "}";
}

me = new Cloud.Server( SERVER_NAME );
log.info( "SERVER_NAME : " + SERVER_NAME );
log.info( "me : " + me );
log.info( "grid server : " + me.gridServer() );
db = connect( "grid" , me.gridServer() );
