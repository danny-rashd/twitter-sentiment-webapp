import React, { useState, useEffect } from "react";
import TextField from "@mui/material/TextField";
import Container from "@mui/material/Container";
import Button from "@mui/material/Button";
import Grid from "@mui/material/Grid";
import emoji from "emoji-dictionary";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import axios from "axios";

export default function BasicTextFields() {
  const [keyword, setKeyword] = useState("");
  const [post, setPost] = useState([]);

  const POST_url = "http://localhost:8080/twitter/add";
  const GET_url = "http://localhost:8080/twitter/getAll";
  const DEL_url = "http://localhost:8080/twitter/clear";
  const WAIT_TIME = 100;
  const theme = createTheme({
    palette: {
      primary: {
        main: "#1DA1F2",
        contrastText: "#000000",
      },
      secondary: {
        main: "#FF0000",
        contrastText: "#fff",
      },
    },
  });

  const searchClick = (e) => {
    e.preventDefault();
    const tweets = { keyword };
    console.log(tweets);
    setKeyword("");
    fetch(POST_url + "?keyword=" + keyword, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(tweets),
    }).then(() => {
      console.log("Added Tweets");
    });
  };

  const deleteClick = (e) => {
    e.preventDefault();
    setKeyword("");
    axios
      .delete(DEL_url)
      .then((res) => console.log("Deleted Tweets", res))
      .catch((err) => console.log(err));
  };

  useEffect(() => {
    const id = setInterval(() => {
      axios
        .get(GET_url)
        .then((res) => {
          setPost(res.data);
        })
        .catch((err) => {
          console.log(err);
        });
    }, WAIT_TIME);
    return () => clearInterval(id);
  }, [post]);

  return (
    <Container maxWidth="fixed">
      <Grid
        container
        spacing="5"
        display="flex"
        flexDirection={"row"}
        justifyContent={"center"}
      >
        <Grid item>
          <TextField
            style={{
              color: "black",
            }}
            variant="filled"
            label="Any keyword"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
        </Grid>
        <ThemeProvider theme={theme}>
          <Grid item>
            <Button
              style={{
                padding: "10px",
                fontSize: "18px",
              }}
              variant="contained"
              color="primary"
              size="large"
              onClick={searchClick}
            >
              Search
            </Button>
          </Grid>
          <Grid item>
            <Button
              style={{
                padding: "10px",
                fontSize: "18px",
              }}
              variant="outlined"
              color="secondary"
              size="large"
              onClick={deleteClick}
            >
              Clear
            </Button>
          </Grid>
        </ThemeProvider>
      </Grid>

      <div
        className="container"
        style={{
          width: "100%",
          height: "550px",
          minWidth: "900px",
          overflowY: "auto",
          padding: "10px",
        }}
      >
        <table className="table table-dark">
          <thead
            className="thead-dark"
            style={{ position: "sticky", top: "0" }}
          >
            <tr>
              <th>User @</th>
              <th>Tweet</th>
              <th>Sentiment</th>
            </tr>
          </thead>
          <tbody>
            {post.map((twitter) =>
              twitter.tweetSentiment === "Positive" ? (
                <tr key={twitter.id}>
                  <td className="bg-success">{twitter.tweetHandle}</td>
                  <td className="bg-success">{twitter.tweetText}</td>
                  <td className="bg-success">{emoji.getUnicode("smile")}</td>
                </tr>
              ) : twitter.tweetSentiment === "Negative" ? (
                <tr key={twitter.id}>
                  <td className="bg-danger">{twitter.tweetHandle}</td>
                  <td className="bg-danger">{twitter.tweetText}</td>
                  <td className="bg-danger">
                    {emoji.getUnicode("frowning_face")}
                  </td>
                </tr>
              ) : twitter.tweetSentiment === "Neutral" ? (
                <tr key={twitter.id}>
                  <td className="bg-secondary">{twitter.tweetHandle}</td>
                  <td className="bg-secondary">{twitter.tweetText}</td>
                  <td className="bg-secondary">
                    {emoji.getUnicode("neutral_face")}
                  </td>
                </tr>
              ) : (
                <tr></tr>
              )
            )}
          </tbody>
        </table>
      </div>
    </Container>
  );
}
