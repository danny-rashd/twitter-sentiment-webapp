package com.example.twitterbackend.service;

import com.example.twitterbackend.entity.Tweets;

import java.util.List;

public interface TwitterService {
    public Tweets saveTweets(Tweets tweets);
    public List <Tweets> getAllTweets();

    void delete();
}
