class Track2 < XGen::Mongo::Base
  collection_name :rubytest
  fields :artist, :album, :song, :track
  def to_s
    "artist: #{artist}, album: #{album}, song: #{song}, track: #{track ? track.to_i : nil}"
  end
end
