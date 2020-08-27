package com.amazon.audiblecambridgehsfindbookalexaskill.findbook.handlers;

public class Book {
    public String title = "";
    public String author = "";

    //  Constructors
    public Book(String t, String a) {
        title = t;
        author = a;
    }
    public Book() {}

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
