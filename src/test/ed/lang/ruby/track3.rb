class Track3 < XGen::Mongo::Base
  set_collection :rubytest, %w(artist album song track)
  def to_s
    "artist: #{artist}, album: #{album}, song: #{song}, track: #{track ? track.to_i : nil}"
  end
end
