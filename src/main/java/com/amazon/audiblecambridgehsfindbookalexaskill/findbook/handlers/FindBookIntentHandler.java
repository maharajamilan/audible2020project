package com.amazon.audiblecambridgehsfindbookalexaskill.findbook.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.audiblecambridgehsfindbookalexaskill.findbook.repository.GoodReadsAPIRespository;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

/**
 * Handles FindBookIntent
 */
public class FindBookIntentHandler implements RequestHandler {

    // response for book names
    private final String speechTextWithBook = "I found a book called %s by %s";
    private final String speechTextNoBookName = "Hmm, I don't know the book %s";

    /**
     * Determine if this handler can handle the intent (but doesn't actually handle it)
     *
     * This is called by the ASK framework.
     *
     * @param input
     * @return
     */
    @Override
    public boolean canHandle(HandlerInput input) { return input.matches(intentName("FindBookIntent")); }

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

        if(slots.containsKey("BookNameSlot") && null != slots.get("BookNameSlot").getValue()) {
            // get bookName from intent slot
            String bookName = (slots.get("BookNameSlot").getValue());
            Book book = null;

            try {
                book = GoodReadsAPIRespository.findBook(bookName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // in case book not found, say no book found
            if(book ==  null) {
                speechText = String.format(speechTextNoBookName, bookName);
                System.out.printf("No book found called %s found", bookName);
            } else {
                String title = book.getTitle();
                String author = book.getAuthor();

                // speak to customer telling them book title and author
                speechText = String.format(speechTextWithBook, title, author);
            }
        } else {
            speechText = speechTextNoBookName;
            System.out.printf(slots.get("BookNameSlot").getValue() + "slot not found");
        }

        log(input, "Speech text response is " + speechText);

        // response object with a card (shown on devices with a screen) and speech (what alexa says)
        return input.getResponseBuilder()
                .withSpeech(speechText) // alexa says this
                .withSimpleCard("FindBook", speechText) // alexa will show this on a screen
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