// blog1.js

db = connect( "testblog" );

core.user.user();
core.blog.urls();

user = new User( "eliot" , "eliot" );
db.users.save( user );

db.blog.posts.remove( {} );
randGen = core.util.random().getRandom(1);

posts = [];
comments = 0;

for(var i=1; i<=1000; i++) {
    
    if ( i < 10 ){
        db.blog.posts.resetIndexCache();
        core.blog.post();
    }
    
    var post = new Post();
    
    post.name = "some_post_title_" + i;
    post.content = "Story: transfer from savings to checking account   As a savings account holder  I want to transfer money from my savings account to my checking accounte  So that I can get cash easily from an ATM  Scenario: savings account has sufficient funds    Given my savings account balance is $100    And my checking account balance is $10    When I transfer $20 from savings to checking    Then my savings account balance should be $80    And my checking account balance should be $30  Scenario: savings account has insufficient funds    Given my savings account balance is $50e    And my checking account balance is $10    When I transfer $60 from savings to checking    Then my savings account balance should be $50    And my checking account balance should be $10";
    post.excerpt = "Story: transfer from savings to checking account   As a savings account holder  I want to transfer money from my savings account to my checking accounte";
    post.ts = new Date();
    post.author = "eliot";
    post.live = true;
    post.commentsEnabled = true;
    post.categories = [ "_home" ];
    
    db.blog.posts.save( post );
    posts.push( post );

    // comments
    var randNum = randGen.nextFloat() * 2;
    for ( var j=0; j<randNum; j++ ){
        addComment( post );
    }
    
    for ( var j=0; j<2; j++ )
        addComment();

    if ( i % 10 == 5 ){
        assert( db.blog.posts.count() == posts.length );
        print( i );
        var vRes = db.blog.posts.validate();
        assert( vRes.valid || vRes.isValid );
 //       print("posts = " + i + " comments = " + comments  + " : " + printCommentDistribution());
    }
}

function printCommentDistribution(){ 
    var max = -1;
    var min = 1000000000;
    var unique = 0;
    for (var i = 0; i < posts.length; i++) {
        var n = posts[i].comments.length;
        if (n > 0) {
            unique++;
            if (n > max) {
                max = n;
            }
            if (n < min) {
                min = n;
            }            
        }
    }
    
    return "uniq = " + unique + " max/min = " + max + "/" + min;
    
}
function addComment( post ){
    
    if ( ! post ){
        var randNum = Math.floor(randGen.nextFloat()*posts.length);
        post = posts[randNum];        
        if ( ! post )
            print( randNum );
    }
    
    assert( post );

    var r = { 
        addComment : "yes", 
        txt: "comment. " +  (new Array(1000)).map( function(z){ return "z"; } ).toString() ,
        getRemoteIP :  function(x) {
            return "127.0.0.1";
        }
    };

    Blog.handlePosts( r , post , user );
    
    comments++;
    
    if ( db.blog.posts.findOne({_id : id} ) )
        return;
    
    throw "POST DISAPPEARED! id: " + post._id;
}
