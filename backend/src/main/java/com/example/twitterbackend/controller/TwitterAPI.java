package com.example.twitterbackend.controller;

import com.example.twitterbackend.entity.Tweets;
import com.example.twitterbackend.service.TwitterService;
import com.example.twitterbackend.tweetfilter.TweetFilter;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.common.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import twitter4j.*;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/twitter")
@CrossOrigin
public class TwitterAPI {

    @Autowired
    private TwitterService twitterService;
    ComputationGraph net = ModelSerializer.restoreComputationGraph(new ClassPathResource("cnn_sentiment.net").getFile(), true);
    WordVectors vec = WordVectorSerializer.loadStaticModel(new ClassPathResource("ms-updated2.vector").getFile());
    TweetSentiment inference = new TweetSentiment(net, vec);

    public TwitterAPI() throws IOException {
    }

    @PostMapping(path = "/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String add(@RequestParam("keyword") String keyword) throws TwitterException{
        try {
            QueryResult tweet = new Tweets4j().tweets4j(keyword);
            int count=0;
            Random rand = new Random();
            for (Status status : tweet.getTweets()) {
                Tweets tweets = new Tweets();
                String preTweetText=status.getText();
                String postTweetText= TweetFilter.tweetFilter(preTweetText);

                if (!postTweetText.isEmpty()) {
                    System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                    tweets.setId(status.getId());
                    tweets.setTweetHandle(status.getUser().getScreenName());
                    tweets.setTweetText(status.getText());
                    tweets.setTweetSentiment(inference.getSentiment(postTweetText).getLeft());
                    tweets.setTweetScore(inference.getSentiment(postTweetText).getRight());
                    twitterService.saveTweets(tweets);
                    System.out.println("@" + tweets.getTweetHandle() + " - " + tweets.getTweetText()+" "+tweets.getTweetSentiment());
                    System.out.println(count);
                    System.out.println("=======================================================================================================");
                    count++;
                }
                else{
                    continue;
                }
            }
            System.out.println(count);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "New tweets is added";
    }

    @GetMapping( path = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tweets> get() {
        return twitterService.getAllTweets();
    }

    @DeleteMapping(path="/clear")
    public String deleteTweets() throws Exception{
        twitterService.delete();
        return "Tweets is cleared";
    }
}