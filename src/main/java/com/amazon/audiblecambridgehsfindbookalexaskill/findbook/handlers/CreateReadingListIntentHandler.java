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

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handles CreateReadingListIntent
 */
public class CreateReadingListIntentHandler implements RequestHandler {
    // response for book names
    private final String speechTextWithBook = "I’ve added %s by %s to your reading list";
    private final String speechTextNoBookName = "Hmm, I don't know the book %s";

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
    public boolean canHandle(HandlerInput input) { return input.matches(intentName("CreateReadingListIntent")); }

    /**
     * Actually handle the event here.
     *
     * This is called by the ASK framework.
     *
     * @param input
     * @return
     */
    @Override
    // TODO resolve error with DDB not being authorized
    public Optional<Response> handle(HandlerInput input) {
        log(input, "Starting request");
        logSlots(input);

        Map<String, Slot> slots = getSlots(input);

        String speechText;

        if(slots.containsKey("BookNameSlot") && null != slots.get("BookNameSlot").getValue()) {
            // get bookName from intent slot
            String bookName = (slots.get("BookNameSlot").getValue());

            // use findBook to get the author name and title in a Book object
            Book book = null;
            try {
                book = GoodReadsAPIRespository.findBook(bookName);

                // create an item to add to the DDB table, empty for now
                Item item = new Item();

                // get book title & author
                String title = book.getTitle();
                String author = book.getAuthor();

                // add the item to the DDB table, with the Title attribute set to the book title
                // and the Author attribute set to author
                table.putItem(item.with("Title", title).with("Author", author));

            } catch (IOException e) {
                e.printStackTrace();
            }

            // in case book not found, say no book found
            if(book ==  null) {
                speechText = String.format(speechTextNoBookName, bookName);
                System.out.printf("No book found called %s found", bookName);
            } else {

                // otherwise get the book info and return repeat it in the specified format
                String title = book.getTitle();
                String author = book.getAuthor();
                speechText = String.format(speechTextWithBook, title, author);
            }

        } else {
            // if no slot is a book name
            speechText = speechTextNoBookName;
            System.out.printf(slots.get("BookNameSlot").getValue() + "slot not found");
        }

        log(input, "Speech text response is " + speechText);

        // response object with a card (shown on devices with a screen) and speech (what alexa says)
        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .withSimpleCard("CreateReadingList", speechText) // alexa will show this on a screen
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
