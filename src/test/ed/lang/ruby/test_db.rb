require 'ruby_test'

class DBTest < RubyTest

  def setup
    super
    run_js <<EOS
db = connect('test');
db.rubytest.remove({});
db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'The Ability to Swing'});
db.rubytest.save({artist: 'Thomas Dolby', album: 'Aliens Ate My Buick', song: 'Budapest by Blimp'});
db.rubytest.save({artist: 'Thomas Dolby', album: 'The Golden Age of Wireless', song: 'Europa and the Pirate Twins'});
db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'Garden Of Earthly Delights', track: 1});
song_id = db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'The Mayor Of Simpleton', track: 2});
db.rubytest.save({artist: 'XTC', album: 'Oranges & Lemons', song: 'King For A Day', track: 3});
EOS
  end

  def teardown
    # AHA! is this showing that db is not defined?
    run_js <<EOS
print("db = " + db); // DEBUG
db.rubytest.remove({});
EOS
    super
  end

  def test_db_exists
    assert_not_nil($db)
  end

  def test_song_id_exists
    run_js 'x = song_id._id.toString()'
    assert_equal($song_id._id, $x)
  end

  def test_collection_wrapper
    coll = $db.rubytest
    assert_not_nil(coll)
    assert coll.respond_to?(:findOne)
    assert coll.respond_to?(:find)
  end

end
