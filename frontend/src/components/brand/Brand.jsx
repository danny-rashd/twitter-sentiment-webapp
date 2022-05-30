import React from "react";
import "./brand.css";
import { google, twitter, github } from "./imports";
const Brand = () => {
  return (
    <div className="brand section__padding">
      <div>
        <a href="https://google.com/">
          <img src={google} alt="google" />
        </a>
      </div>
      <div>
        <a href="https://twitter.com/explore">
          <img src={twitter} alt="twitter" />
        </a>
      </div>
      <div>
        <a href="https://github.com/">
          <img src={github} alt="github" />
        </a>
      </div>
    </div>
  );
};

export default Brand;
