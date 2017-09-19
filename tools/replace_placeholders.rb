Dir.glob('api-descriptors/**/*.json') do |file|
  content = File.read(file)
  values_to_replace = ['/key','/100','/sidOrUniqueName','/sid', '/identity']
  values_to_replace.each do |value_to_replace|
    content.gsub! value_to_replace, '/XXaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
  end
  file = File.open(file, 'w')
  file.write(content)
end
