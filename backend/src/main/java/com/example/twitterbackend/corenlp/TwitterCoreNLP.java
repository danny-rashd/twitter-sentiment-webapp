package com.example.twitterbackend.corenlp;

import com.example.twitterbackend.entity.Tweets;
import com.example.twitterbackend.service.TwitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import twitter4j.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

public class TwitterCoreNLP {

    @Autowired
    private TwitterService twitterService;

    @PostMapping(path = "/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String add(@RequestParam("keyword") String keyword) throws TwitterException{
        try {
            TwitterFactory twitterFactory=  new TwitterFactory();
            Twitter twitter= twitterFactory.getSingleton();
            //Get yesterday's date
            LocalDate yesterDate = LocalDate.now(ZoneId.of("Asia/Kuala_Lumpur")).minusDays(1);
            System.out.println("Yesterday Date: "+ yesterDate);

            int count=0;
            Query query = new Query(keyword.concat(" -filter:retweets -filter:links -filter:replies lang:en"));
            query.setSince(String.valueOf(yesterDate));
            //set tweet limit <=x; default =15 tweets
            query.setCount(10);

            QueryResult result;
            result = twitter.search(query);
            List<Status> tweet_result = result.getTweets();
            Random rand = new Random();
            for (Status tweet : tweet_result) {
                Tweets tweets = new Tweets();
                System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                tweets.setId(tweet.getId());
                tweets.setTweetHandle(tweet.getUser().getScreenName());
                tweets.setTweetText(tweet.getText());

//                //set score
//                tweets.setTweetScore(rand.nextInt((1 - 0) + 1) + 0);
//                //set sentiment
//                if(tweets.getTweetScore()==1){
//                    tweets.setTweetSentiment("Positive");
//                }
//                else {
//                    tweets.setTweetSentiment("Negative");
//                }

                StanfordCoreNLP stanfordCoreNLP = SentimentPipeline.getPipeline();

                CoreDocument coreDocument = new CoreDocument(tweet.getText());

                stanfordCoreNLP.annotate(coreDocument);

                List<CoreSentence> sentences = coreDocument.sentences();

                for(CoreSentence sentence : sentences) {

                    String sentiment = sentence.sentiment();

                    System.out.println(sentiment + "\t" + sentence);
                    tweets.setTweetSentiment(sentiment);
                }

                twitterService.saveTweets(tweets);
                System.out.println("@" + tweets.getTweetHandle() + " - " + tweets.getTweetText()+" "+tweets.getTweetSentiment());
                System.out.println(count);
                System.out.println("=======================================================================================================");
                count++;
            }
            System.out.println(count);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
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