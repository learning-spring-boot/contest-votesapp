package de.votesapp.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client to the Yowsup-Rest API
 *
 * Currently only sending and
 */
@Component
@Profile("yowsup")
public class YowsupRestClient implements WhatsAppClient {
	private final String baseUrl;
	private final RestTemplate restTemplate;

	@Autowired
	public YowsupRestClient(@Value("${yowsup.baseUrl}") final String baseUrl, final RestTemplate restTemplate) {
		this.baseUrl = baseUrl;
		this.restTemplate = restTemplate;
	}

	@Override
	public void sendGroupMessage(final GroupMessage messageToSend) {
		// WhatsApp won't return the unique messages IDs, so we don't receive a
		// resource ID for that.

		final Map<String, String> message = new HashMap<>();
		message.put("to", messageToSend.getGroupId());
		message.put("text", messageToSend.getText());

		restTemplate.postForEntity(baseUrl + "/messages/outbox", message, Void.class);
	}

	/**
	 * Receive all new messages and delete them from the inbox.
	 */
	@Override
	public GroupMessage[] fetchGroupMessages() {
		final GroupMessage[] messages = restTemplate.getForObject(baseUrl + "/messages/inbox", GroupMessage[].class);

		deleteMessages(messages);

		return messages;
	}

	private void deleteMessages(final GroupMessage... messages) {
		for (final GroupMessage msg : messages) {
			restTemplate.delete(baseUrl + "/messages/inbox/{id}", msg.getId());
		}
	}

}
