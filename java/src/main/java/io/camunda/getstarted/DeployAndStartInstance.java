package io.camunda.getstarted;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.camunda.zeebe.client.api.search.response.SearchQueryResponse;
import io.camunda.zeebe.client.api.search.response.UserTask;
import lombok.Builder;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;

import static io.camunda.getstarted.ZeebeHelper.getRandomVariableMap;
import static io.camunda.getstarted.ZeebeHelper.startProcess;

public class DeployAndStartInstance {

  private static final Logger LOG = LogManager.getLogger(DeployAndStartInstance.class);

  public static void main(String[] args) {
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {
      client.newDeployResourceCommand()
          .addResourceFromClasspath("subprocess.bpmn")
          .addResourceFromClasspath("send-email.bpmn")
          .addResourceFromClasspath("simpleTask.bpmn")
          .addResourceFromClasspath("simpleTask2.bpmn")
          .addResourceFromClasspath("test-form.form")
          .send()
          .join();

      final var variables = getRandomVariableMap();

      startProcess(client, variables, "simpleTask");
      final var processStartedEvent = startProcess(client, variables, "send-email");

      client.newSetVariablesCommand(processStartedEvent.getProcessInstanceKey())
              .variables(Map.of("message_content", "Hello from the Java get started, with new value"))
              .send()
              .join();

      LOG.info("Update variable in instance for processInstanceKey='{}'", processStartedEvent.getProcessInstanceKey());

    }
  }



  @Data
  @Builder
  static class MyJsonObject {
    private String foo;
    private List<String> barList;
    private Long fooNum;
  }
}
