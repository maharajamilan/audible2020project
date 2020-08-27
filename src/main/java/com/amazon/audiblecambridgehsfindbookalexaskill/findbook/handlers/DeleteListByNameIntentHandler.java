package com.amazon.audiblecambridgehsfindbookalexaskill.findbook.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handles DeleteListByNameIntent
 */
public class DeleteListByNameIntentHandler implements RequestHandler {

    // speechText templates
    private final String speechTextWithList = "%s list successfully deleted";
    private final String speechTextNoListName = "Hmm, I couldn't find that list";

    // access the DDB tables
    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    DynamoDB dynamoDB = new DynamoDB(client);
    Table defaultList = dynamoDB.getTable("DefaultReadingList");
    Table fictionList = dynamoDB.getTable("FictionReadingList");
    Table nonfictionList = dynamoDB.getTable("NonfictionReadingList");
    Table schoolList = dynamoDB.getTable("SchoolReadingList");

    /**
     * Determine if this handler can handle the intent (but doesn't actually handle it)
     *
     * This is called by the ASK framework.
     *
     * @param input
     * @return
     */
    @Override
    public boolean canHandle(HandlerInput input) { return input.matches(intentName("DeleteListByNameIntent")); }

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
        logSlots(input);

        Map<String, Slot> slots = getSlots(input);

        String speechText;

        // wasn't able to figure out how to recreate the table, so the quick and dirty way to clear is just
        // to delete the whole table
        if ((slots.get("ListNameSlot").getValue().equals("default"))
                || ((slots.get("ListNameSlot").getValue().equals("default reading")))) {
            // if the reading list is the default list
            defaultList.delete();
            speechText = String.format(speechTextWithList, "Default");
        } else if (slots.get("ListNameSlot").getValue().equals("fiction")) {
            // if the reading list is the fiction list
            fictionList.delete();
            speechText = String.format(speechTextWithList, "Fiction");
        } else if (slots.get("ListNameSlot").getValue().equals("nonfiction")) {
            // if the reading list is the nonfiction list
            nonfictionList.delete();
            speechText = String.format(speechTextWithList, "Nonfiction");
        } else if (slots.get("ListNameSlot").getValue().equals("school")) {
            // if the reading list is the school list
            schoolList.delete();
            speechText = String.format(speechTextWithList, "School");
        } else {
            // if no valid list name was given or if list name is null;
            speechText = speechTextNoListName;
        }


        log(input, "Speech text response is " + speechText);

        // response object with a card (shown on devices with a screen) and speech (what alexa says)
        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .withSimpleCard("DeleteListByName", speechText) // alexa will show this on a screen
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
        return Collections.unmodifiableMap(intent.getSlots());
    }

    /**
     * Log slots for easier debugging
     * @param input Input passed to handle
     */
    void logSlots(HandlerInput input) {
        Map<String, Slot> slots = getSlots(input);
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
