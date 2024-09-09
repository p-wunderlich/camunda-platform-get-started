package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;

public class ZeebeClientFactory {

  // Local zeebe
  public static ZeebeClient getZeebeClient() {
    return ZeebeClient.newClientBuilder()
            .gatewayAddress("0.0.0.0:26500")
            .usePlaintext()
            .build();
  }

}
