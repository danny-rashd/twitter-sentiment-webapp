package com.example.twitterbackend.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Tweets {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tweetHandle;
    private String tweetText;
    private String tweetSentiment;
    private double tweetScore;

    public Tweets() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTweetHandle() {
        return tweetHandle;
    }

    public void setTweetHandle(String tweetHandle) {
        this.tweetHandle = tweetHandle;
    }

    public String getTweetText() {
        return tweetText;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }

    public String getTweetSentiment() {
        return tweetSentiment;
    }

    public void setTweetSentiment(String tweetSentiment) {
        this.tweetSentiment = tweetSentiment;
    }
    public int getTweetScore() {
        return (int) tweetScore;
    }

    public void setTweetScore(Double tweetScore) {
        this.tweetScore = tweetScore;
    }
}
