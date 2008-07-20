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

print( "LINE_1_" );
Rails.mapURI = function( uri ){
    
    var mime = MimeTypes.get( uri );
    
    if ( (  mime && 
            ( mime.startsWith( "image/" ) 
              || mime.startsWith( "video/" )
            ) )
         ||
         uri.match( /\.(css|js)$/ )
       )
        return "/public" + uri;
         
    return "/~~/rails/rails.jxp";
};

ActionController.Base = function(){
    this.shortName = null;
    this.className = null;
};

ActionController.Base.prototype.__magic = 17;

ActionController.Base.prototype.dispatch = function( request , response , matchingRoute ){

    var f = this[matchingRoute.action];
    if ( ! f ){
        print( "can't find [" + matchingRoute.action + "] in [" + this.className + "]" );
        return;
    }
    
    var appResponse = new ApplicationResponse( this , matchingRoute.action );

    // --- setup scope
    
    var funcScope = f.getScope( true );

    funcScope.render_text = function(s){
        print( s );
        appResponse.anythingRendered = true;
    };
    
    funcScope.respond_to = function( b ){
        b.call( appResponse.requestThis , appResponse );
    };

    funcScope.redirect_to = function( thing ){
        appResponse.anythingRendered = true;
        print( "<script>window.location = \"" + Rails.routes.getLinkFor( thing ) + "\";</script>" );
        return true;
    };
    
    funcScope.params = new Rails.Params( request , matchingRoute );

    // --- invoke action

    f.call( appResponse.requestThis );
    
    if ( ! appResponse.anythingRendered ){
        
        if ( ! local.app.views )
            throw "no views directory";
        
        if ( ! local.app.views[ this.shortName ] )
            throw "no views directory for " + this.shortName;
        
        var view = local.app.views[ this.shortName ][matchingRoute.action];
        if ( ! view )
            view = local.app.views[ this.shortName ][matchingRoute.action + ".html" ];
        if ( ! view )
            throw "no view for " + this.shortName + "." + matchingRoute.action;
        
        view.call( appResponse.requestThis );
    }

    print( "\n <!-- " + this.className + "." + method + " -->" );
};

ActionController.Base.prototype.toString = function(){
    return "{ shortName : " + this.shortName + " , className : " + this.className + " }";
};



function ApplicationResponse( controller , method ){

    this.controller = controller;
    assert( this.controller );

    this.method = method;
    assert( this.method );

    this.anythingRendered = false;

    this.requestThis = {};
    this.requestThis.prototype = controller;

};

ApplicationResponse.prototype.html = function(){
    var blah = this.requestThis;

    blah.__notFoundHandler = function( thing ){
        if ( thing.endsWith( "_path" ) ){
            return function(z){
                return "BROKEN : " + z;
            }
        }
        return null;
    }


    if ( ! local.app.views )
        throw "no views directory";
    
    if ( ! local.app.views[ this.controller.shortName ] )
        throw "no view directory for : " + this.controller.shortName;
   
    var template = 
        local.app.views[ this.controller.shortName ][ this.method + ".html" ] || 
        local.app.views[ this.controller.shortName ][ this.method  ];
    
    if ( ! template )
        throw "no template for " + this.controller.shortName + ":" + this.method;
    log.rails.response.debug( template + ".html" + called );
    

    if ( Rails.helpers.application ){
        Object.extend( this.requestThis , Rails.helpers.application );
        SYSOUT ( "HERE : " + this.requestThis.keySet() );
    }
    
    if ( Rails.helpers[ this.controller.shortName ] ){
        Object.extend( this.requestThis , Rails.helpers[ this.controller.shortName ] );
    }

    if ( arguments.length > 0 && isFunction( arguments[0] ) ){
        arguments[0].call( this.requestThis );
    }

    
    // layour

    var layout = null;
    var appLayout = null;
    if ( local.app.views.layouts ){
        layout = local.app.views.layouts[ this.controller.shortName + ".html" ];
        appLayout = 
            local.app.views.layouts.application || 
            local.app.views.layouts["application.html"];
    }
    
    SYSOUT( "layout : " + layout );
    SYSOUT( "appLayout : " + appLayout );
    if ( appLayout ){
        this.requestThis.content_for( "layout" , template );
        assert( this.requestThis.content_for_layout != null );
        appLayout.apply( this.requestThis );
    }
    else if ( layout ){
        // TODO: fix this...
        layout.getScope( true ).controller = { action_name : this.method };
        
        layout( function(){
            template.apply( blah , arguments );
            return "";
        } );
    }
    else {
        template.apply( blah );
    }
        
    this.anythingRendered = true;
};

ApplicationResponse.prototype.xml = function(){
    return false;
};


// ---------
// data model
// ---------

function caches_page( name ){
    SYSOUT( "ignore caches_page [" + name + "]" );
};

function before_filter( name ){
    SYSOUT( "ignore before_filter [" + name + "]" );
};
