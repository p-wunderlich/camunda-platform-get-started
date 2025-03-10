package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class DeployAndStartManyInstances {

  private static final Logger LOG = LogManager.getLogger(DeployAndStartManyInstances.class);

  private static final Long START_AMOUNT = 100L;

  public static void main(String[] args) {
    var timeStart = Instant.now();
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {
      client.newDeployResourceCommand()
          .addResourceFromClasspath("subprocess.bpmn")
          .addResourceFromClasspath("send-email.bpmn")
          .addResourceFromClasspath("test-form.form")
          .send()
          .join();

      for(int i = 0; i < START_AMOUNT; i++) {
        final var variables = new HashMap<>();
        variables.put("message_content", "Hello from the Java get started "+i);
        variables.put("number_val", 12345678+i);
        variables.put("null_val", null);
        variables.put("bool_val", true);
        variables.put("bool_string_val", "true");

        final var processStartedEvent = client.newCreateInstanceCommand()
                .bpmnProcessId("send-email")
                .latestVersion()
                .variables(variables)
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
