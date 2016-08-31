import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.UrlMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;

import lib.DocParser;
import lib.ResourceModel;
import lib.ResourceParser;

public class ApiProxy {
  public static void main(String[] args) {
    String anyResourcePattern = "[A-Z]{2,}\\w{32}";
    String countryPattern = "\\/[A-Z]{2}";
    String phoneNumberPattern =
        "\\/\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*";
    String escapedPhoneNumberPatter =
        "\\/\\\\s*(?:((?:%2B)|(?:\\\\+))?(\\\\d{1,3}))?(?:\\-|\\.| |\\(|(?:%20)|(?:%28)|(?:%2E)|(?:%2D))*(\\\\d{3})(?:\\-|\\.| |\\)|(?:%20)|(?:%29)|(?:%2E)|(?:%2D))*(\\\\d{3})(?:\\-|\\.| |(?:%20)|(?:%2E)|(?:%2D))*(\\\\d{4})";

    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8089).httpsPort(443)
        .keystorePath("keystore/twilio-store.jks").keystorePassword("twilioFake"));
    wireMockServer.addMockServiceRequestListener(new RequestListener() {
      @Override
      public void requestReceived(Request request, Response response) {
        System.out
            .println(request.getMethod() + ": " + request.getHeader("Host") + request.getUrl());
        if (!response.wasConfigured()) {
          System.out.println(
              "There is no configured response for this route. Please open a ticket here if you"
                  + " think this is an error https://github.com/TwilioDevEd/twilio-api-faker/issues");
        }
      }
    });
    wireMockServer.start();
    System.out.println("Starting Server. Please wait...");
    WireMock wireMock = new WireMock("localhost", 8089);

    DocParser parser = new DocParser(new File("api-descriptors"));
    ArrayList resourceList = parser.getResourceList();

    for (Object resource : resourceList) {
      ResourceModel resourceModel = (ResourceModel) resource;
      HashMap<String, HashMap<String, Object>> resourceTransactions =
          resourceModel.getResourceTransactions();

      for (HashMap<String, Object> transactionProperties : resourceTransactions.values()) {
        String resourcePath = (String) transactionProperties.get(ResourceParser.RESOURCE_PATH);
        String resourceHost = (String) transactionProperties.get(ResourceParser.RESOURCE_HOST);
        resourcePath = resourcePath.replace(".json", "");
        resourcePath = resourcePath.replaceAll(anyResourcePattern, "\\\\w+");
        resourcePath = resourcePath.replaceAll(countryPattern, "\\/[A-Z]{2}");
        resourcePath = resourcePath.replaceAll(phoneNumberPattern, escapedPhoneNumberPatter);
        resourcePath = resourcePath.replace("/", "\\/");
        resourcePath = resourcePath + "(\\.json)?(\\/)?(\\?.+)?";

        String methodString = (String) transactionProperties.get(ResourceParser.RESOURCE_METHOD);
        methodString = methodString.toUpperCase();

        UrlMatchingStrategy urlMatching = urlMatching(resourcePath);
        MappingBuilder method = null;

        JSONObject contentJson =
            (JSONObject) transactionProperties.get(ResourceParser.RESOURCE_CONTENT);
        String responseContent = "{}";
        if (contentJson != null) {
          responseContent = contentJson.toJSONString();
        }

        switch (methodString) {
          case "POST":
            method = post(urlMatching);
            wireMock.register(
                put(urlMatching).withHeader("Host", containing(resourceHost)).willReturn(aResponse()
                    .withHeader("Content-Type", "application/json").withBody(responseContent)));
            break;
          case "GET":
            method = get(urlMatching);
            break;
          case "DELETE":
            method = delete(urlMatching);
            break;
          default:
            System.out.println("No method specified");
        }

        wireMock.register(method.withHeader("Host", containing(resourceHost)).willReturn(
            aResponse().withHeader("Content-Type", "application/json").withBody(responseContent)));

      }
    }
    System.out.println("Server Ready!");
  }
}
