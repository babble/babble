
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

if ( ! Cloud )
    Cloud = {};

var internalDomain = javaStatic( "ed.util.Config" , "getInternalDomain" );
var gridPort = javaStatic( "ed.cloud.Cloud" , "getGridDBPort" );

Cloud.Server = function( name ){
    name = Cloud.Server._fixHostName( name );

    var r = /^(\w{3,7})\-(\w\w+)\-n(\d+)\./.exec( name );
    
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

    this.name = this.location + "-" + this.provider + "." + this.number;
};

Cloud.Server.prototype.gridServer = function( lr ){
    if ( this.bad )
        return null;
    return this.location + "-" + this.provider + "-grid" + ( lr || "" ) + "." + internalDomain;
};

Cloud.Server.prototype.gridServerRight = function(){
    return this.gridServer( "-r" );
}

Cloud.Server.prototype.gridServerLeft = function(){
    return this.gridServer( "-l" );
}

Cloud.Server.prototype.getGridLocation = function(){
    if ( ! this.isMyGridDomainPaired() )
        return ( this.gridServer() || "127.0.0.1" ) + ":" + gridPort;

    return [ this.gridServerLeft() + ":" + gridPort , this.gridServerRight() + ":" + gridPort ];
}

Cloud.Server.prototype.getOtherGridPair = function(){
    assert( this.isMyGridDomainPaired() , "my grid not paired" );

    if ( javaStatic( "ed.net.DNSUtil" , "isLocalAddress" , me.gridServerLeft()  ) )
        return me.gridServerRight();
    
    return me.gridServerLeft();
}

Cloud.Server.prototype.isMyGridDomainPaired = function(){
    if ( this.bad )
        return false;
    return Cloud.Server.isDomainPaired( internalDomain , this.location + "-" + this.provider );
}

Cloud.Server.prototype.isGridServer = function(){
    if ( ! me.real || this.bad )
        return false;

    return javaStatic( "ed.net.DNSUtil" , "isLocalAddress" , me.gridServerLeft() ) ||
        javaStatic( "ed.net.DNSUtil" , "isLocalAddress" , me.gridServerRight() );
}

Cloud.Server.prototype.toString = function(){
    return "{Server.  " + this.location + "-" + this.provider + "-" + this.number + "}";
}

Cloud.Server._fixHostName = function( host ){
    if ( ! host )
        return null;

    // special code for ec2
    var r = /ip\-(\d+)\-(\d+)\-(\d+)\-(\d+)/.exec( host );
    if ( r ){
        // this is probably an ec2 machine
        return javaStatic( "ed.util.Config" , "get" ).getProperty( "ZONE" , "use1c" ) + "-ec2-n" + r[1] + r[2] + r[3] + r[4] + ".";
    }

    if ( host.indexOf( "." ) < 0 )
        host += ".";
    
    return host;
}

/**
* @param domain 10gen.cc
* @param loc  iad-sb
*/
Cloud.Server.isDomainPaired = function( domain , loc ){
    try {
        var l = javaStatic( "ed.net.DNSUtil" , "getByName" , loc + "-grid-l." + domain ).getHostAddress();
        var r = javaStatic( "ed.net.DNSUtil" , "getByName" , loc + "-grid-r." + domain ).getHostAddress();
    }
    catch ( e ){
        return false;
    }
    return l.toString() != r.toString();
}

me = new Cloud.Server( SERVER_NAME );

if ( me.isMyGridDomainPaired() )
    db = connect.ms( "grid" , me.getGridLocation() );
else
    db = connect( "grid" , me.getGridLocation() );


log.info( "SERVER_NAME:" + SERVER_NAME + " me:" + me + " grid server : " + me.getGridLocation() + " amIGrid:" + me.isGridServer() );
