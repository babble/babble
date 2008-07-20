
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

register = new djang10.Library();

register.filter("moo", function() {
    return "moo";
});

var MyTagNode = function() { };
MyTagNode.prototype = {
    __proto__: djang10.Node.prototype,
    __render: function(context, printer) {
        printer("moo");
    }
}

register.tag("myTag", function(parser, token) {
    return new MyTagNode();
});


var MyTagWithChildNode = function(children) {
    this.children = children;
};
MyTagWithChildNode.prototype = {
    __proto__: djang10.Node.prototype,
    __render: function(context, printer) {
        printer("before_" + this.children.render(context) + "_after");
    }
};

register.tag("myTagWithChild", function(parser, token) {
    var children = parser.parse(["endmyTagWithChild"]);
    parser.delete_first_token();
    
    return new MyTagWithChildNode(children);
});
