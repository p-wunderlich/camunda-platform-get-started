package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class DeployAndStartManyInstances {

  private static final Logger LOG = LogManager.getLogger(DeployAndStartManyInstances.class);

  private static final Long START_AMOUNT = 10000L;

  public static void main(String[] args) {
    var timeStart = Instant.now();
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {
      client.newDeployResourceCommand()
          .addResourceFromClasspath("send-email.bpmn")
          .send()
          .join();

      for(int i = 0; i < START_AMOUNT; i++) {
        final var processStartedEvent = client.newCreateInstanceCommand()
                .bpmnProcessId("send-email")
                .latestVersion()
                .variables(Map.of("message_content", "Hello from the Java get started"))
                .send()
                .join();

        LOG.info(
                "Started instance {}/{} for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
                i, START_AMOUNT, processStartedEvent.getProcessDefinitionKey(), processStartedEvent.getBpmnProcessId(), processStartedEvent.getVersion(),
                processStartedEvent.getProcessInstanceKey());

        final var setVariablesResponse = client.newSetVariablesCommand(processStartedEvent.getProcessInstanceKey())
                .variables(Map.of("message_content", "Hello from the Java get started, with new value"))
                .send()
                .join();

        //LOG.info("Update variable in instance for processInstanceKey='{}'", processStartedEvent.getProcessInstanceKey());
      }

      double timeElapsed = (double) Duration.between(timeStart, Instant.now()).toMillis() / 1000;
      LOG.info("Time elapsed: {} sec {}", timeElapsed, timeElapsed);
    }
  }
}
