class Course < XGen::Mongo::Base

  # Declare Mongo collection name and ivars to be saved
  collection_name :rubytest_courses
  field :name

  def to_s
    "Course #{name}"
  end
end
