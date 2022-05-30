import React from "react";
import "./header.css";
import wp from "../../assets/twitter-wallpaper.jpg";
import { TweetComponent, TweetSimilarity } from "../../components";
const Header = () => {
  return (
    <div className="header section__padding" id="home">
      <div className="header-content">
        <h1 className="gradient__text">Malaysian Twitter Sentiment Analysis</h1>
        <p>
          KataTwitter can tell how people feel about a certain topic on Twitter.
          Rather than a simple count of mentions or comments, this web
          application considers emotions and opinions. It involves collecting
          and analyzing information in the posts people share about a certain
          topic on Twitter.
        </p>
        <div className="header-content__input">
          <TweetComponent />
        </div>
      </div>
    </div>
  );
};

export default Header;
