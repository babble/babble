
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

Cloud.Server = function( name ){
    var r = /(\w\w\w)\-(\w\w+)\-n(\d+)\./.exec( name );
    
    if ( ! r ){
        this.bad = true;
        this.location = "unknown";
        this.provider = "unknown";
        this.number = 0;
        return;
    }
    
    this.real = true;

    this.location = r[1];
    this.provider = r[2];
    this.number = parseInt( r[3] );
    
};

Cloud.Server.prototype.gridServer = function(){
    if ( this.bad )
        return null;
    return this.location + "-" + this.provider + "-grid.10gen.cc";
};

Cloud.Server.prototype.toString = function(){
    return "{Server.  location:" + this.location + " provider:" + this.provider + " n:" + this.number + "}";
}

me = new Cloud.Server( SERVER_NAME );
log.info( "SERVER_NAME : " + SERVER_NAME );
log.info( "me : " + me );
log.info( "grid server : " + me.gridServer() );
db = connect( "grid" , me.gridServer() );
