package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeployAndStartInstance {

  private static final Logger LOG = LogManager.getLogger(DeployAndStartInstance.class);

  public static void main(String[] args) {
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {
      client.newDeployResourceCommand()
          .addResourceFromClasspath("send-email.bpmn")
          .send()
          .join();

      final var processStartedEvent = client.newCreateInstanceCommand()
          .bpmnProcessId("send-email")
          .latestVersion()
          .variables(Map.of(
                  "message_content", "Hello from the Java get started",
                  "number_val", 12345678)
          )
          .send()
          .join();

      LOG.info(
              "Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
              processStartedEvent.getProcessDefinitionKey(), processStartedEvent.getBpmnProcessId(), processStartedEvent.getVersion(),
              processStartedEvent.getProcessInstanceKey());

      final var setVariablesResponse = client.newSetVariablesCommand(processStartedEvent.getProcessInstanceKey())
              .variables(Map.of("message_content", "Hello from the Java get started, with new value"))
              .send()
              .join();

      LOG.info("Update variable in instance for processInstanceKey='{}'", processStartedEvent.getProcessInstanceKey());

    }
  }
}
