import React from "react";
import "./footer.css";
import logo from "../../assets/twitter-logo.svg";
const Footer = () => {
  return (
    <div className="footer section__padding">
      <div className="footer-heading">
        <h1 className="gradient__text">Do you want to know more?</h1>
      </div>
      <a href="https://developer.twitter.com/en">
        <div className="footer-btn">
          <p>Request Twitter API</p>
        </div>
      </a>
      <div className="footer-links">
        <div className="footer-links_logo">
          <img src={logo} alt="logo" />
          <p>KUALA LUMPUR</p>
          <p>All rights reserved</p>
        </div>
        <div className="footer-links_div">
          <h4>Links</h4>
          <p>Social Media</p>
          <p>Contact</p>
        </div>
        <div className="footer-links_div">
          <h4>Company</h4>
          <p>Terms & Conditions</p>
          <p>Privacy Policy</p>
          <p>Contact</p>
        </div>
        <div className="footer-links_div">
          <h4>Get in Touch</h4>
          <p>Danny Rashd</p>
          <p>Malaysia</p>
          <p>123@google.com</p>
        </div>
      </div>
      <div className="footer-copyright">
        <p>Twitter Sentiment. All rights reserved.</p>
      </div>
    </div>
  );
};

export default Footer;
