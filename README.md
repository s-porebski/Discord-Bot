# Discord-Bot
[![CodeFactor](https://www.codefactor.io/repository/github/s-porebski/discord-bot/badge)](https://www.codefactor.io/repository/github/s-porebski/discord-bot)

## Overview
Java Discord Bot that displays League of Legends ranking for specific players.

The bot uses the Riot API and makes sure not to exceed the limit of API requests.

## Commands
`!add <summoner-name>` - Adds the player to the ranking database

`!remove <summoner-name>`  - Removes the player from the ranking database
  
`!ranking` - Shows the ranking of the added players

`!streak <summoner-name>`  - Displays the win or loss streak for the specified player

`!streak <summoner-name>/<summoner-name>` - Displays the win ratio for specific players
