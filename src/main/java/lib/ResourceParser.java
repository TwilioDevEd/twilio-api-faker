package lib;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ResourceParser {
  public static String RESOURCE_METHOD = "method";
  public static String RESOURCE_NAME = "name";
  public static String RESOURCE_CONTENT = "content";
  public static String RESOURCE_URL = "url";
  public static String RESOURCE_PATH = "path";
  public static String RESOURCE_HOST = "host";

  private String resourceName;
  private JSONObject resourceRequest;
  private JSONObject resourceResponse;
  private Map<String, String> pathAndHost;

  public ResourceParser(String name, JSONArray resourceArrayJson) {
    JSONObject resourceJson = (JSONObject) resourceArrayJson.get(0);
    this.resourceName = name;
    this.resourceRequest = (JSONObject) resourceJson.get("request");
    this.resourceResponse = (JSONObject) resourceJson.get("response");
    this.pathAndHost = getResourceRequestPathAndHost();
  }

  public String getResourceName() {
    return resourceName;
  }

  public JSONObject getResourceRequest() {
    return this.resourceRequest;
  }

  public JSONObject getResourceResponse() {
    return this.resourceResponse;
  }

  public String getResourceRequestMethod() {
    return (String) getResourceRequest().get(RESOURCE_METHOD);
  }

  public JSONObject getResourceResponseContent() {
    return (JSONObject) getResourceResponse().get(RESOURCE_CONTENT);
  }

  public Map<String, String> getResourceRequestPathAndHost() {
    String url = (String) getResourceRequest().get(RESOURCE_URL);

    String pattern = "^https:\\/\\/(.+twilio.com)(.+$)";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(url);
    Map<String, String> pathAndHost = new HashMap<>();

    if (m.find()) {
      pathAndHost.put(RESOURCE_HOST, m.group(1));
      pathAndHost.put(RESOURCE_PATH, m.group(2));
    }

    return pathAndHost;
  }

  public HashMap<String, Object> getResourceProperties() {
    HashMap<String, Object> resourceProperties = new HashMap<>();
    resourceProperties.put(RESOURCE_NAME, getResourceName());
    resourceProperties.put(RESOURCE_METHOD, getResourceRequestMethod());
    resourceProperties.put(RESOURCE_CONTENT, getResourceResponseContent());
    resourceProperties.put(RESOURCE_PATH, this.pathAndHost.get(RESOURCE_PATH));
    resourceProperties.put(RESOURCE_HOST, this.pathAndHost.get(RESOURCE_HOST));

    return resourceProperties;
  }
}
