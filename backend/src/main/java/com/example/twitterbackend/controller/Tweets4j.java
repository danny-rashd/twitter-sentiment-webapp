package com.example.twitterbackend.controller;

import twitter4j.*;

import java.time.LocalDate;
import java.time.ZoneId;


public class Tweets4j {
    public QueryResult tweets4j(String keyword) throws TwitterException {
        TwitterFactory twitterFactory=  new TwitterFactory();
        Twitter twitter= twitterFactory.getSingleton();
        //Get yesterday's date
        LocalDate prevDate = LocalDate.now(ZoneId.of("Asia/Kuala_Lumpur")).minusDays(7);
        System.out.println("Yesterday Date: "+ prevDate);

        Query query = new Query(keyword.concat(" -filter:retweets -filter:links -filter:replies lang:en"));
        query.setSince(String.valueOf(prevDate));
        //set tweet limit <=x; default =15 tweets
        query.setCount(10);

        QueryResult result = twitter.search(query);

        return result;
//        Twitter twitter = new TwitterFactory().getInstance();
//        Query query = new Query(keyword + " -is:reply -is:retweet -is:quote -has:media -has:images -has:videos");
//        QueryResult result = twitter.search(query);
//        return result;
    }
}
