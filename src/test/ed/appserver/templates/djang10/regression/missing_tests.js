
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

var aDate = new OldDate(1981, 12-1, 20, 15, 11, 37);
OtherClass2 = function(x) {
    this.prop = x;
};
OtherClass2.prototype = {
    echo: function(x) {
        return x;
    },
    add: function(x) {
        return new OtherClass2(x.prop + this.prop);
    }
};

ReflexiveClass = function(obj) {
    this.obj = this;
    this.numbers = [0,1,2,3,4,5,6,7,8,9];
    this.arr = [0,1,2,3,4,5,6,7,8,9,this];
    this.arr.push(this.arr);
    
    var i=0;
    var self = this;
    ["zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"].forEach(function(x) { self.arr[x] = i;});
    this.arr["ten"] = this.arr["self"] = this;
    this.arr["eleven"] = this.arr["arr"] = this.arr;
    
    if(obj != null)
        for(var k in obj)
            this[k] = obj[k];
    
    
    this.arr.get = function(x) { return this[x]; };
};
ReflexiveClass.prototype = {
    get: function(x) { return this[x]; },
    getValue: function() { return this.value; },
    getSelf: function() { return this; },
    getArr: function() { return this.arr; },
    echo: function(x) { return x; },
    toString: function() { return this.value; },
};

var EXPR_PREFIX = 'obj.echo(obj).echo( obj ).echo(\tobj\t)["obj"][ "obj" ][\t"obj"\t].arr[10].arr[ 10 ].arr[\t10\t].arr.10.get("obj").get( "obj" ).get(\t"obj"\t)';

var ExprIfChangedMatrix = [];
var ExprIfChangedVarCount = 7;
var ExprIfChangedResult = "";
for(var i=0; i<ExprIfChangedVarCount; i++) {
    var row = [];
    for(var j=0; j<i; j++) {
        row.push(1);
    }
    for(var j=i; j<ExprIfChangedVarCount; j++) {
        row.push(2);
    }
    ExprIfChangedMatrix.push(row);
    ExprIfChangedMatrix.push(row);
    ExprIfChangedResult += 2*i + ",";
};

var NowFormatStr =  "Y-n-j G:i";
var d = new Date();
var NowFormatResult = d.getFullYear() +"-"+ (d.getMonth()+1) +"-"+d.getDate()+" "+d.getHours()+":"+(d.getMinutes()<10?'0':'')+d.getMinutes();
    
tests=[                                                                                                                                                                            //a   ,A ,b  ,d ,D  ,f   ,F       ,g,G ,h ,H ,i ,j ,l     ,L    ,m ,M  ,n ,N   ,O    ,P        ,r                              ,s ,S ,t ,w      
       { name: "filter-date01",            content: '{% autoescape off %}{{ d|date:"a,A,b,d,D,f,F,g,G,h,H,i,j,l,L,m,M,n,N,O,P,r,s,S,t,w" }}{% endautoescape %}', 
                                           model: { d: aDate }, 
                                           results: "p.m.,PM,dec,20,Sun,3:11,December,3,15,03,15,11,20,Sunday,false,12,Dec,12,Dec.,-0500,3:11 p.m.,Sun, 20 Dec 1981 15:11:37 -0500,37,th,31,0" },
                                                                                                                                                                                   //a   ,A ,f   ,g,G ,h ,H ,i ,P        ,s
       { name: "filter-time01",            content: '{% autoescape off %}{{ d|date:"a,A,f,g,G,h,H,i,P,s" }}{% endautoescape %}', 
                                           model: { d: aDate }, 
                                           results: "p.m.,PM,3:11,3,15,03,15,11,3:11 p.m.,37" },
       
       { name: "if-method chain01",        content: '{% autoescape off %}{% if foo.blah(x).bar %}true{% else %}false{% endif %}{% endautoescape %}', 
                                           model: { }, 
                                           results: "false" },
       
       { name: "if-method chain02",        content: '{% autoescape off %}{% if foo.add(x).prop %}true{% endif %}{% endautoescape %}', 
                                           model: { foo: new OtherClass2(3), x: new OtherClass2(5) }, 
                                           results: "true" },
       
       { name: "if-allow spaces01",        content: '{% autoescape off %}{% if foo   .   add(   x    )   .   prop    %}true{% endif %}{% endautoescape %}', 
                                           model: { foo: new OtherClass2(3), x: new OtherClass2(5) }, 
                                           results: "true" },
       
       //same as the regular django tests but takes into account that js & python have different type representionations
       { name: "filter-make_list-jsout01", content: "{% autoescape off %}{{ a|make_list }}{% endautoescape %}", 
                                           model: { "a": djang10.mark_safe("&") }, 
                                           results: "&" },

       { name: "filter-make_list-jsout02", content: "{{ a|make_list }}", 
                                           model: { "a": djang10.mark_safe("&") }, 
                                           results: "&amp;" },
       
       //HACK ALERT: these 2 unit tests, test for python's style of string representations, while everything else tests for js style
       { name: "filter-make_list-jsout03", content: "{% autoescape off %}{{ a|make_list|stringformat:\"s\"|safe }}{% endautoescape %}",
                                           model: { "a": djang10.mark_safe("&") },
                                           results: "['&']" },

       { name: "filter-make_list-jsout04", content: "{{ a|make_list|stringformat:\"s\"|safe }}",
                                           model: { "a": djang10.mark_safe("&") },
                                           results: "['&']" },    
       
       { name: "filter-make_list-jsout-extra01", content: "{% autoescape off %}{{ a|make_list }}{% endautoescape %}",
                                           model: { "a": djang10.mark_safe("&&&") },
                                           results: "&,&,&" },

       { name: "filter-make_list-jsout-extra02", content: "{{ a|make_list }}",
                                           model: { "a": djang10.mark_safe("&&&") },
                                           results: "&amp;,&amp;,&amp;" },
       
       
       { name: "expression-mangling01",    content: "{{ var.if().1[0].1 }}",
                                           model: { "var": {"if": function() { return [1, [[null, "moo"]]]} } },
                                           results: "moo" },

       { name: "expression-mangling02",    content: "{{ var.if.when.for.continue }}",
                                           model: { "var": { "if": {"when": {"for": {"continue": "baa"}}}}},
                                           results: "baa" }, 

       { name: "now-escape01",             content: '{% now "'+NowFormatStr+' \\Y-\\n-\\j \\G:\\i" %}',
                                           model: {},
                                           results: NowFormatResult+ " Y-n-j G:i"}, 
       
       { name: "literal-escape01",         content: '{% literal_escape on %}{{ "moo\\nbaa" }}{% endliteral_escape %}',
                                           model: {},
                                           results: "moo\nbaa" },

       { name: "literal-escape02",         content: '{% literal_escape off %}{{ "moo\\nbaa" }}{% endliteral_escape %}',
                                           model: {},
                                           results: "moo\\nbaa" },

       { name: "literal-escape03",         content: '{{ "moo\\nbaa" }}',
                                           model: {},
                                           results: "moo\\nbaa" },
       
       { name: "with-filter01",            content: '{% with none.existent.crap( "moo" ) | default:"baa" as var %}{{ var }}{% endwith %}',
                                           model: {},
                                           results: "baa" },
       
       { name: "filter-pprint01",          content: '{{ "moo"|pprint }}',
                                           model: {},
                                           results: '"moo"' },
       { name: "filter-pprint02",          content: '{{ var|pprint }}',
                                           model: {"var":"moo"},
                                           results: '&quot;moo&quot;' },
       { name: "filter-pprint03",          content: '{% autoescape off %}{{ var|pprint }}{% endautoescape %}',
                                           model: {"var":"moo"},
                                           results: '"moo"' },
       
       { name: "filter-join01",            content: '{{ arr|join:"-" }}',
                                           model: {arr: [1,2,3]},
                                           results: "1-2-3"},
       

                       

       //test tag's accepting expressions instead of variables
       { name: "expr-cycle01",             content: '{% cycle '+EXPR_PREFIX+'.echo(1) '+EXPR_PREFIX+'.echo(2)\t'+EXPR_PREFIX+'.echo(3) as c1 %}{% cycle c1 %}{% cycle c1 %}{% cycle c1 %}{% cycle c1 %}',
                                           model: new ReflexiveClass(),
                                           results: "12312" },
       
       { name: "expr-cycle02",             content: '{% for ignored in range %}{% cycle '+EXPR_PREFIX+'.echo(1) '+EXPR_PREFIX+'.echo(2)\t'+EXPR_PREFIX+'.echo(3) %}{% endfor %}',
                                           model: new ReflexiveClass({"range": new Array(5)}),
                                           results: "12312" },
       
       { name: "expr-filtertag01",         content: '{% filter cut:'+EXPR_PREFIX+'.echo("m")|slice:'+EXPR_PREFIX+'.echo("1:-1") | cut:'+EXPR_PREFIX+'.echo("x")\t|\tcut:'+EXPR_PREFIX+'.echo( "z"  ) %}123mxmymoozo321{% endfilter %}',
                                           model: new ReflexiveClass(),
                                           results: "23yooo32"},
       
       { name: "expr-for01",               content: '{% for var in '+EXPR_PREFIX+'.numbers %}{{var }}{% endfor %}',
                                           model: {obj: new ReflexiveClass()},
                                           results:"0123456789" },

       { name: "expr-for02",               content: '{% for var in '+EXPR_PREFIX+'.numbers reversed %}{{var }}{% endfor %}',
                                           model: {obj: new ReflexiveClass()},
                                           results:"9876543210" },
       
       { name: "expr-ifequal01",           content: '{% ifequal '+EXPR_PREFIX+'.numbers   obj.echo(obj).echo( obj )["obj"][ "obj" ].arr[10].arr[ 10 ].get("obj").get( "obj").numbers %}moo!{% endifequal %}',
                                           model: new ReflexiveClass(),
                                           results: "moo!" },
       
       { name: "expr-firstof01",           content: '{% firstof '+EXPR_PREFIX+'.CRAP   '+EXPR_PREFIX+'.numbers[2] %}',
                                           model: new ReflexiveClass(),
                                           results: "2" },
       
       { name: "expr-if01",                content: '{% if '+EXPR_PREFIX+'.echo(true) and '+EXPR_PREFIX+'.arr[ 2   ] and not '+EXPR_PREFIX+'.CRAP["MORE crap"] and '+EXPR_PREFIX+'.arr.2 %}moo!{% endif %}',
                                           model: new ReflexiveClass(),
                                           results: "moo!" },

       { name: "expr-ifchanged",           content: '{% for var in value %}{% ifchanged '+EXPR_PREFIX+'.echo(var)[0] '+EXPR_PREFIX+'.echo(var)[1] %}{{ forloop.counter0 }},{% endifchanged %}{% endfor %}',
                                           model: new ReflexiveClass({value: [[0,0],[0,0],[1,0],[1,0],[1,1],[1,1]]}),
                                           results: "0,2,4," },

       { name: "expr-now",                 content: '{% now '+EXPR_PREFIX+'.format %}',
                                           model: new ReflexiveClass({format: NowFormatStr}),
                                           results: NowFormatResult},

       { name: "expr-now-filter01",        content: '{% now '+EXPR_PREFIX+'.CRAP|default: '+EXPR_PREFIX+'.format %}',
                                           model: new ReflexiveClass({format: NowFormatStr}),
                                           results: NowFormatResult},

       { name: "expr-now-filter02",        content: '{% now '+EXPR_PREFIX+'.CRAP   |   \tdefault: '+EXPR_PREFIX+'.format %}',
                                           model: new ReflexiveClass({format: NowFormatStr}),
                                           results: NowFormatResult},
       
       { name: "expr-regroup01",           content: '{% regroup '+EXPR_PREFIX+'.data by bar as grouped %}{% for group in grouped %}{{ group.grouper }}:{% for item in group.list %}{{ item.foo }}{% endfor %},{% endfor %}',
                                           model: new ReflexiveClass( { "data": [ { "foo": "c", "bar": 1 }, { "foo": "d", "bar": 1 }, { "foo": "a", "bar": 2 }, { "foo": "b", "bar": 2 }, { "foo": "x", "bar": 3 } ] }),
                                           results: "1:cd,2:ab,3:x," },

       { name: "expr-regroup-filter01",    content: '{% regroup '+EXPR_PREFIX+'.CRAP|default:'+EXPR_PREFIX+'.data by bar as grouped %}{% for group in grouped %}{{ group.grouper }}:{% for item in group.list %}{{ item.foo }}{% endfor %},{% endfor %}',
                                           model: new ReflexiveClass( { "data": [ { "foo": "c", "bar": 1 }, { "foo": "d", "bar": 1 }, { "foo": "a", "bar": 2 }, { "foo": "b", "bar": 2 }, { "foo": "x", "bar": 3 } ] }),
                                           results: "1:cd,2:ab,3:x," },

       { name: "expr-regroup-filter01",    content: '{% regroup '+EXPR_PREFIX+'.CRAP\t|\tdefault\t:\t'+EXPR_PREFIX+'.data by bar as grouped %}{% for group in grouped %}{{ group.grouper }}:{% for item in group.list %}{{ item.foo }}{% endfor %},{% endfor %}',
                                           model: new ReflexiveClass( { "data": [ { "foo": "c", "bar": 1 }, { "foo": "d", "bar": 1 }, { "foo": "a", "bar": 2 }, { "foo": "b", "bar": 2 }, { "foo": "x", "bar": 3 } ] }),
                                           results: "1:cd,2:ab,3:x," },
       
       { name: "expr-widthratio",          content: '{% widthratio '+EXPR_PREFIX+'.a '+EXPR_PREFIX+'.b 100 %}',
                                           model: new ReflexiveClass({ "a": 50, "b": 80 }),
                                           results: "63" },

       { name: "expr-widthratio02",        content: '{% widthratio '+EXPR_PREFIX+'.a\t'+EXPR_PREFIX+'.b 100 %}',
                                           model: new ReflexiveClass({ "a": 50, "b": 80 }),
                                           results: "63" },
       
       //backwards compat for django....although the 2 parameters allow filter expressions, spaces are disallowed between filter delimiters(|, :)
       { name: "expr-widthratio-filter01", content: '{% widthratio CRAP|default:a CRAP2|default:b 100 %}',
                                           model: { "a": 50, "b": 80 },
                                           results: "63" },

       { name: "expr-with01",              content: '{% with '+EXPR_PREFIX+'.echo("foo") as var %}{{ var }}{% endwith %}',
                                           model: new ReflexiveClass(),
                                           results: 'foo' },

       { name: "expr-with-filter01",       content: '{% with '+EXPR_PREFIX+'.CRAP|default:'+EXPR_PREFIX+'.echo("123moo321") | slice : '+EXPR_PREFIX+'.echo("1:-1")\t|\tcut\t:\t'+EXPR_PREFIX+'.echo("3") as var %}{{ var }}{% endwith %}',
                                           model: new ReflexiveClass(),
                                           results: '2moo2' },
                                           
];
