require 'json'

result = {}

Dir.glob('api-descriptors/**/*.json') do |file|
  content = File.read(file)
  descriptor = JSON.parse(content)
  descriptor.keys.each do |key|
    resource_request = descriptor[key][0]['request']
    resource_method = resource_request['method']
    resource_url = resource_request['url']

    result[resource_url] = { methods: [] } unless result.key?(resource_url)

    if result.key?(resource_url)
      existing_resource = result[resource_url]
      existing_methods = existing_resource[:methods]

      puts "duplicate: #{file} : #{resource_url} : #{resource_method}" if existing_methods.include?(resource_method)
      existing_methods.push(resource_method) unless existing_methods.include?(resource_method)
    end
  end
end