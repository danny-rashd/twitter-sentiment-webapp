package com.example.twitterbackend.service;

import com.example.twitterbackend.entity.Tweets;
import com.example.twitterbackend.repository.TwitterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TwitterServiceImpl implements TwitterService {
    @Autowired
    private TwitterRepository twitterRepository;

    @Override
    public Tweets saveTweets(Tweets tweets){
        return twitterRepository.save(tweets);
    }

    @Override
    public List<Tweets> getAllTweets() {return twitterRepository.findAll();
    }
    @Override
    public void delete(){twitterRepository.deleteAll();}
}
