package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class UpdateInstance {

  private static final Logger LOG = LogManager.getLogger(UpdateInstance.class);

  public static void main(String[] args) {
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {

      client.newUserTaskUpdateCommand(2251799813685321L)
              .candidateGroups(List.of("updatedGroup-"+new Random().nextInt(100)+1, "updatedGroup2-"+new Random().nextInt(100)+1))
              .candidateUsers(List.of("updatedUser-"+new Random().nextInt(100)+1, "updatedUser2-"+new Random().nextInt(100)+1))
              .send()
              .join();

    }
  }
}
