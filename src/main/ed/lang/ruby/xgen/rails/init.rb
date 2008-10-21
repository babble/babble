# Look for the requested URI in the public directory. If not found, pass it on
# to the 10gen Rails dispatcher.
$mapUrlToJxpFile = Proc.new do |uri, req|
  uri = '/index.html' if uri == '/'
  if File.exist?(File.join($local.getRoot.getPath, 'public', uri[1..-1]))
    "/public" + uri
  else
    "public/xgen_dispatch.rbcgi"
  end
end
