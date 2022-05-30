package com.example.twitterbackend.repository;

import com.example.twitterbackend.entity.Tweets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwitterRepository extends JpaRepository <Tweets, Long> {
}
