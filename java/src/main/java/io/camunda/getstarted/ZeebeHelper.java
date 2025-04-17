package io.camunda.getstarted;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZeebeHelper {

    private static final Logger LOG = LogManager.getLogger(ZeebeHelper.class);

    public static ProcessInstanceEvent startProcess(ZeebeClient client, HashMap<Object, Object> variables, String processId) {
        final var processStartedEvent = client.newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        LOG.info(
                "Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
                processStartedEvent.getProcessDefinitionKey(), processStartedEvent.getBpmnProcessId(), processStartedEvent.getVersion(),
                processStartedEvent.getProcessInstanceKey());
        return processStartedEvent;
    }

    public static HashMap<Object, Object> getRandomVariableMap() {
        final var objectMapper = new ObjectMapper();

        final var variables = new HashMap<>();
        variables.put("message_content", "Hello from the Java get started");
        variables.put("number_val", 12345678);
        variables.put("double_val", 1234.5678);
        variables.put("number_string_val", "12345678");
        variables.put("double_string_val", "1234.5678");
        variables.put("null_val", null);
        variables.put("bool_val", true);
        variables.put("bool_string_val", "true");
        variables.put("list_val", List.of("list1", "list2"));
        variables.put("map_val", Map.of("key1", "value1", "key2", "value2"));
        variables.put("json_val", objectMapper.valueToTree(DeployAndStartInstance.MyJsonObject.builder()
                .foo("foo")
                .barList(List.of("bar1", "bar2"))
                .fooNum(123L)
                .build())
        );
        variables.put("object_val", DeployAndStartInstance.MyJsonObject.builder()
                .foo("foo")
                .barList(List.of("bar1", "bar2"))
                .fooNum(123L)
                .build()
        );
        return variables;
    }

}
