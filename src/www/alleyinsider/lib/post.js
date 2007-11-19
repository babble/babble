
function Post(){

};

Post.prototype.getTeaserContent = function(){
    return this.content.replace( /---JUMP---.*/m , "" );
};

Post.prototype.getFullContent = function(){
    return this.content.replace( /---JUMP---[\r\n]*/ , "" );
};

Post.prototype.getContent = function( full ){
    if ( full )
        return this.getFullContent();
    return this.getTeaserContent();
};

Post.prototype.hasJump = function(){

    var idx = this.content.indexOf( "---JUMP---" );
    
    if ( idx < 0 ) return false;
    
    idx = idx + 10;
    
    return ( idx + 10 ) < this.content.length;
};


Post.prototype.getNumComments = function(){
    if ( ! this.comments )
        return 0;
    
    return this.comments.length;
}
