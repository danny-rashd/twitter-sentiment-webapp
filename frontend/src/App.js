import React from "react";
import { Brand, ButtonAppBar } from "./components";
import { Footer, Header } from "./containers";
import "./App.css";
const App = () => {
  return (
    <div className="App">
      <div className="gradient__bg">
        <ButtonAppBar />
        <Header />
        <Brand />
      </div>
      <Footer />
    </div>
  );
};

export default App;
