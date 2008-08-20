#!/usr/bin/env ./bin/jssh

if(! arguments || arguments.length < 1){
    print("usage: getConnectPoint site");
    return;
}

var site = arguments[0];
if(! site.startsWith('http://')) site = 'http://' + site;
if(! site.endsWith('/')) site = site + '/';

var req = new XMLHttpRequest("GET", site+'admin/_ugly_hack_getDB');
var res = req.send();
print( "export WTCONNECT='"+res.responseText+"'" );
