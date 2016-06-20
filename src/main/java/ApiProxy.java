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
    String phoneNumberPattern = "\\/\\s*(?:\\+?(\\d{1,3}))?[-. (]*(\\d{3})[-. )]*(\\d{3})[-. ]*(\\d{4})(?: *x(\\d+))?\\s*";
    String escapedPhoneNumberPatter = "\\/\\\\s*(?:\\\\+?(\\\\d{1,3}))?[-. (]*(\\\\d{3})[-. )]*(\\\\d{3})[-. ]*(\\\\d{4})(?: *x(\\\\d+))?\\\\s*";

    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().port(8089).httpsPort(443)
        .keystorePath("keystore/twilio-store.jks").keystorePassword("twilioFake"));
    wireMockServer.addMockServiceRequestListener(new RequestListener() {
      @Override
      public void requestReceived(Request request, Response response) {
        System.out.println(request.getMethod() + ": " + request.getUrl());
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
        String resourceUrl = (String) transactionProperties.get(ResourceParser.RESOURCE_URL);
        resourceUrl = resourceUrl.replace(".json", "");
        resourceUrl = resourceUrl.replaceAll(anyResourcePattern, "\\\\w+");
        resourceUrl = resourceUrl.replaceAll(countryPattern, "\\/[A-Z]{2}");
        resourceUrl = resourceUrl.replaceAll(phoneNumberPattern, escapedPhoneNumberPatter);
        resourceUrl = resourceUrl.replace("/", "\\/");
        resourceUrl = resourceUrl + "(\\.json)?(\\/)?(\\?.+)?";

        String methodString = (String) transactionProperties.get(ResourceParser.RESOURCE_METHOD);
        methodString = methodString.toUpperCase();

        UrlMatchingStrategy urlMatching = urlMatching(resourceUrl);
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
            wireMock.register(put(urlMatching).willReturn(aResponse()
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

        wireMock.register(method.willReturn(
            aResponse().withHeader("Content-Type", "application/json").withBody(responseContent)));
      }
    }
    System.out.println("Server Ready!");
  }
}
