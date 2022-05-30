import React, { Component } from "react";
import MaterialBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import IconButton from "@mui/material/IconButton";
import { Twitter } from "@mui/icons-material";
import { ThemeProvider, createTheme } from "@mui/material/styles";

export default function ButtonAppBar() {
  const theme = createTheme({
    palette: {
      secondary: {
        main: "#00acee",
        contrastText: "#fff",
      },
    },
  });

  return (
    <Box sx={{ flexGrow: 1 }}>
      <ThemeProvider theme={theme}>
        <MaterialBar position="static" color="secondary">
          <Toolbar>
            <IconButton
              size="large"
              edge="start"
              color="inherit"
              aria-label="menu"
              sx={{ mr: 2 }}
            >
              <Twitter />
            </IconButton>
            <Typography variant="h6" component="div" sx={{ flexGrow: 2 }}>
              KataTwitter
            </Typography>
          </Toolbar>
        </MaterialBar>
      </ThemeProvider>
    </Box>
  );
}
