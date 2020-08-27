package com.amazon.audiblecambridgehsfindbookalexaskill.findbook.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
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
 * Handles CreateMultipleListsIntent
 */
public class CreateMultipleListsIntentHandler implements RequestHandler {

    // speechText templates
    private final String speechTextWithBook = "%s  successfully added to %s";
    private final String speechTextNoBookName = "Hmm, I couldn't find that book";
    private final String speechTextNoListName = "Hmm, I couldn't find that list";

    // access the DDB tables
    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    DynamoDB dynamoDB = new DynamoDB(client);
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
    public boolean canHandle(HandlerInput input) { return input.matches(intentName("CreateMultipleListsIntent")); }

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

        if (slots.containsKey("BookNameSlot") && null != slots.get("BookNameSlot").getValue()) {
            // get bookName from intent slot
            String bookName = (slots.get("BookNameSlot").getValue());

            // use findBook to get the author name and title in a Book object
            Book book = null;
            try {
                book = GoodReadsAPIRespository.findBook(bookName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // in case book not found, say no book found
            if(book ==  null) {
                speechText = speechTextNoBookName;
                System.out.printf("No book found called %s found", bookName);
            } else {
                // create an item to add to the DDB table, empty for now
                Item item = new Item();

                // get book title & author
                String title = book.getTitle();
                String author = book.getAuthor();

                // add the item to the proper DDB table, with the Title attribute set to the book title and the
                // Author attribute set to author. Modify speechText to give the correct list the book was added to
                if (slots.get("ListNameSlot").getValue().equals("fiction")){
                    fictionList.putItem(item.with("Title", title).with("Author", author));
                    speechText = String.format(speechTextWithBook, title, "your fiction reading list");
                }
                else if (slots.get("ListNameSlot").getValue().equals("nonfiction")){
                    nonfictionList.putItem(item.with("Title", title).with("Author", author));
                    speechText = String.format(speechTextWithBook, title, "your nonfiction reading list");
                }
                else if (slots.get("ListNameSlot").getValue().equals("school")){
                    schoolList.putItem(item.with("Title", title).with("Author", author));
                    speechText = String.format(speechTextWithBook, title, "your school reading list");
                } else {
                    // in case no valid list name was given or if list name is null;
                    speechText = speechTextNoListName;
                }
            }

        } else {
            // if no slot is a book name
            speechText = speechTextNoBookName;
            System.out.println("No BookNameSlot found");
        }

        log(input, "Speech text response is " + speechText);

        // response object with a card (shown on devices with a screen) and speech (what alexa says)
        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .withSimpleCard("FetchReadingList", speechText) // alexa will show this on a screen
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
