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

public class DeployAndStartInstance {

  private static final Logger LOG = LogManager.getLogger(DeployAndStartInstance.class);

  public static void main(String[] args) {
    try (ZeebeClient client = ZeebeClientFactory.getZeebeClient()) {
      client.newDeployResourceCommand()
          .addResourceFromClasspath("send-email.bpmn")
          .send()
          .join();

      final var objectMapper = new ObjectMapper();

      final var variables = new HashMap<>();
      variables.put("message_content", "Hello from the Java get started");
      variables.put("number_val", 12345678);
      variables.put("double_string_val", "1234.5678");
      variables.put("number_string_val", "12345678");
      variables.put("null_val", null);
      variables.put("bool_val", true);
      variables.put("bool_string_val", "true");
      variables.put("json_val", objectMapper.valueToTree(MyJsonObject.builder()
              .foo("foo")
              .barList(List.of("bar1", "bar2"))
              .fooNum(123L)
              .build())
      );

      final var processStartedEvent = client.newCreateInstanceCommand()
          .bpmnProcessId("send-email")
          .latestVersion()
          .variables(variables)
          .send()
          .join();

      LOG.info(
              "Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
              processStartedEvent.getProcessDefinitionKey(), processStartedEvent.getBpmnProcessId(), processStartedEvent.getVersion(),
              processStartedEvent.getProcessInstanceKey());

      client.newSetVariablesCommand(processStartedEvent.getProcessInstanceKey())
              .variables(Map.of("message_content", "Hello from the Java get started, with new value"))
              .send()
              .join();

      /** Not working at the moment, need to investigate
       *
       * Caused by: com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field "userTaskKey" (class io.camunda.zeebe.client.protocol.rest.UserTaskItem), not marked as ignorable (20 known properties: "dueDate", "priority", "completionDate", "creationDate", "elementInstanceKey", "assignee", "state", "customHeaders", "processDefinitionVersion", "elementId", "followUpDate", "processInstanceKey", "candidateGroup", "processDefinitionId", "key", "candidateUser", "formKey", "processDefinitionKey", "tenantIds", "externalFormReference"])
       *  at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: io.camunda.zeebe.client.protocol.rest.UserTaskSearchQueryResponse["items"]->java.util.ArrayList[0]->io.camunda.zeebe.client.protocol.rest.UserTaskItem["userTaskKey"])
       * 	at com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.from(UnrecognizedPropertyException.java:61)
       *
       * Awaitility.await()
          .pollInterval(Duration.ofSeconds(5))
          .timeout(Duration.ofSeconds(30))
          .until(() -> {
                    var items = client.newUserTaskQuery()
                            .filter(f -> f.processInstanceKey(processStartedEvent.getProcessInstanceKey()))
                            .send()
                            .join()
                            .items();
                    return !items.isEmpty();
                  }
          );
      SearchQueryResponse<UserTask> userTask = client.newUserTaskQuery().filter(userTaskFilter -> userTaskFilter.processInstanceKey(processStartedEvent.getProcessInstanceKey())).send().join();

      userTask.items().forEach(task -> {
        LOG.info("Update user task with key='{}'", task.getKey());
        client.newUserTaskUpdateCommand(task.getKey())
                .candidateGroups(List.of("updatedGroup-"+new Random().nextInt(100)+1, "updatedGroup2-"+new Random().nextInt(100)+1))
                .candidateUsers(List.of("updatedUser-"+new Random().nextInt(100)+1, "updatedUser2-"+new Random().nextInt(100)+1))
                .send()
                .join();
      }); **/

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
