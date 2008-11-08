
h = new XML.HTML( "<html><body><div><b>a</div></body></html>" );
assert.eq( "<html><body><div><b>a</b></div></body></html>" , h.toString().toLowerCase().replace( /[\s\r\n]/g , "" ) );
