
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

tests=[                                                                                                                                                                            //a   ,A ,b  ,d ,D  ,f   ,F       ,g,G ,h ,H ,i ,j ,l     ,L    ,m ,M  ,n ,N   ,O    ,P        ,r                              ,s ,S ,t ,w      
       { name: "filter-date01", content: '{% autoescape off %}{{ d|date:"a,A,b,d,D,f,F,g,G,h,H,i,j,l,L,m,M,n,N,O,P,r,s,S,t,w" }}{% endautoescape %}', model: { d: aDate }, results: "p.m.,PM,dec,20,Sun,3:11,December,3,15,03,15,11,20,Sunday,false,12,Dec,12,Dec.,-0500,3:11 p.m.,Sun, 20 Dec 1981 15:11:37 -0500,37,th,31,0" },
                                                                                                                                                                                   //a   ,A ,f   ,g,G ,h ,H ,i ,P        ,s
       { name: "filter-time01", content: '{% autoescape off %}{{ d|date:"a,A,f,g,G,h,H,i,P,s" }}{% endautoescape %}', model: { d: aDate }, results: "p.m.,PM,3:11,3,15,03,15,11,3:11 p.m.,37" },
       
       { name: "if-method chain01", content: '{% autoescape off %}{% if foo.blah(x).bar %}true{% else %}false{% endif %}{% endautoescape %}', model: { }, results: "false" },
       { name: "if-method chain02", content: '{% autoescape off %}{% if foo.add(x).prop %}true{% endif %}{% endautoescape %}', model: { foo: new OtherClass2(3), x: new OtherClass2(5) }, results: "true" },
       
       { name: "if-allow spaces01", content: '{% autoescape off %}{% if foo   .   add(   x    )   .   prop    %}true{% endif %}{% endautoescape %}', model: { foo: new OtherClass2(3), x: new OtherClass2(5) }, results: "true" },
       
       //same as the regular django tests but takes into account that js & python have different type representionations
       { name: "filter-make_list-jsout01", content: "{% autoescape off %}{{ a|make_list }}{% endautoescape %}", model: { "a": djang10.mark_safe("&") }, results: "&" },
       { name: "filter-make_list-jsout02", content: "{{ a|make_list }}", model: { "a": djang10.mark_safe("&") }, results: "&amp;" },
       
       //HACK ALERT: these 2 unit tests, test for python's style of string representations, while everything else tests for js style
       { name: "filter-make_list-jsout03", content: "{% autoescape off %}{{ a|make_list|stringformat:\"s\"|safe }}{% endautoescape %}", model: { "a": djang10.mark_safe("&") }, results: "['&']" },
       { name: "filter-make_list-jsout04", content: "{{ a|make_list|stringformat:\"s\"|safe }}", model: { "a": djang10.mark_safe("&") }, results: "['&']" },    
       
       { name: "filter-make_list-jsout-extra01", content: "{% autoescape off %}{{ a|make_list }}{% endautoescape %}", model: { "a": djang10.mark_safe("&&&") }, results: "&,&,&" },
       { name: "filter-make_list-jsout-extra02", content: "{{ a|make_list }}", model: { "a": djang10.mark_safe("&&&") }, results: "&amp;,&amp;,&amp;" },
       
       
       { name: "expression-mangling01", content: "{{ var.if().1[0].1 }}", model: { "var": {"if": function() { return [1, [[null, "moo"]]]} } }, results: "moo" },
       { name: "expression-mangling02", content: "{{ var.if.when.for.continue }}", model: { "var": { "if": {"when": {"for": {"continue": "baa"}}}}}, results: "baa" }, 

       { name: "now-escape01", content: '{% now "D M j Y H:i:s \\G\\M\\TO (T) Z" %}', model: {}, results: (new Date()).toString() + " -14400" }, 
       
       { name: "literal-escape01", content: '{% literal_escape on %}{{ "moo\\nbaa" }}{% endliteral_escape %}', model: {}, results: "moo\nbaa" },
       { name: "literal-escape02", content: '{% literal_escape off %}{{ "moo\\nbaa" }}{% endliteral_escape %}', model: {}, results: "moo\\nbaa" },
       { name: "literal-escape03", content: '{{ "moo\\nbaa" }}', model: {}, results: "moo\\nbaa" },
       
       { name: "with-filter01", content: '{% with none.existent.crap( "moo" ) | default:"baa" as var %}{{ var }}{% endwith %}', model: {}, results: "baa" },
       
];