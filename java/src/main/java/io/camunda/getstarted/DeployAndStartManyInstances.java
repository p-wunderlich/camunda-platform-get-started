package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.camunda.getstarted.ZeebeHelper.getRandomVariableMap;
import static io.camunda.getstarted.ZeebeHelper.startProcess;

public class DeployAndStartManyInstances {

  private static final Logger LOG = LogManager.getLogger(DeployAndStartManyInstances.class);

  private static final Long START_AMOUNT = 100000L;

  public static void main(String[] args) {
    var timeStart = Instant.now();
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {
      client.newDeployResourceCommand()
          .addResourceFromClasspath("subprocess.bpmn")
          .addResourceFromClasspath("send-email.bpmn")
          .addResourceFromClasspath("simpleTask.bpmn")
          .addResourceFromClasspath("simpleTask2.bpmn")
          .addResourceFromClasspath("simpleIncident.bpmn")
          .addResourceFromClasspath("test-form.form")
          .addResourceFromClasspath("service_tasks_v1.bpmn")
          .send()
          .join();

      for(int i = 0; i < START_AMOUNT; i++) {
        final var variables = getRandomVariableMap();

        startProcess(client, variables, "service_tasks_v1");
        startProcess(client, variables, "simpleTask");
        startProcess(client, variables, "simpleIncident");
        final var processStartedEvent = startProcess(client, variables, "send-email");

        final var setVariablesResponse = client.newSetVariablesCommand(processStartedEvent.getProcessInstanceKey())
                .variables(Map.of("message_content", "Hello from the Java get started, with new value"))
                .send()
                .join();
      }

      double timeElapsed = (double) Duration.between(timeStart, Instant.now()).toMillis() / 1000;
      LOG.info("Time elapsed: {} sec {}", timeElapsed, timeElapsed);
    }
  }
}
