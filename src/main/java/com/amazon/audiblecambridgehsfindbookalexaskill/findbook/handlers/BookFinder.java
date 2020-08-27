package com.amazon.audiblecambridgehsfindbookalexaskill.findbook.handlers;

import java.util.HashMap;


/* MAJOR NOTE - 90% sure that since the findBook method is now in the GoodReadsAPI repository,
 this class is no longer needed */
public class BookFinder {

    // temp hashmap, to be replaced with Goodreads API
    private static HashMap<String, Book> books = new HashMap<String, Book>();

    public BookFinder() {

        // Create a bunch of Book objects
        Book theMartian = new Book("The Martian", "Andy Weir");
        Book dune = new Book("Dune", "Frank Herbert");
        Book NQNW = new Book("Not Quite Not White: Losing and Finding Race in America", "Sharmila Sen");
        Book frankenstein = new Book("Frankenstein", "Mary Shelley");
        Book colorOfLaw =  new Book ("The Color of Law: A Forgotten History of How Our Government Segregated America", "Richard Rothstein");
        Book undergroundRailroad = new Book("The Underground Railroad", "Colson Whitehead");
        Book gunFight = new Book("Gunfight: The Battle Over the Right to Bear Arms in America", "Adam Winkler");

        // add the Book objects to the "book" hashmap
        books.put("The Martian", theMartian);
        books.put("Dune", dune);
        books.put("Frankenstein", frankenstein);
        books.put("The Color of Law", colorOfLaw);
        books.put("The Underground Railroad", undergroundRailroad);

        // accounting for different title variations
        books.put("Not Quite Not White", NQNW);
        books.put("Not Quite Not White: Losing and Finding Race in America", NQNW); // redundancy
        books.put ("Gun Fight", gunFight);
        books.put("Gunfight", gunFight);

        // all in lowercase for search purposes
        books.put("the martian", theMartian);
        books.put("dune", dune);
        books.put("frankenstein", frankenstein);
        books.put("the color of law", colorOfLaw);
        books.put("the underground railroad", undergroundRailroad);
        books.put("not quite not white", NQNW);
        books.put ("gun fight", gunFight);
        books.put("gunfight", gunFight);
    }

    // TODO replace this with a call to Goodreads API, passing the title string
    // if the title is in the "books" hashmap, return the corresponding value (a Books object)
    public static Book findBook(String title) {
        // TODO initialize an instance of reposiitory class to handl stuff, where merging occurs (UNNEEDED)
        // remove hashmap, then BF is only 1 method - could just use the repository
        if(books.containsKey(title.toLowerCase())) {
            return books.get(title);
        } else {
            return null; // TODO return something like "no book found", other cleanup
        }
    }

}
