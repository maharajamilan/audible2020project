package com.amazon.audiblecambridgehsfindbookalexaskill.findbook.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.audiblecambridgehsfindbookalexaskill.findbook.repository.GoodReadsAPIRespository;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handles GetFromReadingListIntent
 */
public class GetFromReadingListIntentHandler implements RequestHandler {

    // TODO see if premade speechText strings are needed

    // access the DDB table
    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    DynamoDB dynamoDB = new DynamoDB(client);
    Table table = dynamoDB.getTable("DefaultReadingList");

    /**
     * Determine if this handler can handle the intent (but doesn't actually handle it)
     *
     * This is called by the ASK framework.
     *
     * @param input
     * @return
     */
    @Override
    public boolean canHandle(HandlerInput input) { return input.matches(intentName("GetFromReadingListIntent")); }

    /**
     * Actually handle the event here.
     *
     * This is called by the ASK framework.
     *
     * @param input
     * @return
     */
    @Override
    public Optional<Response> handle(HandlerInput input) {
        log(input, "Starting request");
        logSlots(input); // NPE

        Map<String, Slot> slots = getSlots(input);

        // first part of the speechText, blank author & title variables
        String speechText = "Your reading list has " ;
        String title;
        String formattedTitle;
        String author;
        String formattedAuthor;

        // iterate through the DDB table
        ScanRequest scanRequest = new ScanRequest().withTableName("DefaultReadingList");
        ScanResult result = client.scan(scanRequest);

        for (Map<String, AttributeValue> item : result.getItems()){
            // grab title & author from the item we are on
            title = item.get("Title").toString();
            author = item.get("Author").toString();

            // remove weird formatting on title and author strings and add to speechText
            formattedTitle = title.substring(4, title.length()-2);
            formattedAuthor = author.substring(4, author.length()-2);
            speechText = String.format((speechText + "%s by %s, "), formattedTitle, formattedAuthor);
        }

        log(input, "Speech text response is " + speechText);

        // response object with a card (shown on devices with a screen) and speech (what alexa says)
        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .withSimpleCard("GetFromReadingList", speechText) // alexa will show this on a screen
                .build();
    }

    /**
     * Get the slots passed into the request
     * @param input The input object
     * @return Map of slots
     */
    Map<String, Slot> getSlots(HandlerInput input) {
        // this chunk of code gets the slots
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        if (intent.getSlots() == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(intent.getSlots());
    }

    /**
     * Log slots for easier debugging
     * @param input Input passed to handle
     */
    void logSlots(HandlerInput input) {
        Map<String, Slot> slots = getSlots(input); // NPE
        // log slot values including request id and time for debugging
        for(String key : slots.keySet()) {
            log(input, String.format("Slot value key=%s, value = %s", key, slots.get(key).toString()));
        }
    }

    /**
     * Logs debug messages in an easier to search way
     * You can also use system.out, but it'll be harder to work with
     */
    void log(HandlerInput input, String message) {
        System.out.printf("[%s] [%s] : %s]\n",
                input.getRequestEnvelope().getRequest().getRequestId().toString(),
                new Date(),
                message);
    }

}
