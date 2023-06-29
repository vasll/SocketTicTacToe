# TicTacToe-Android
A ***school-project*** Android TicTacToe game that works locally using Java Sockets.

The clients and servers communicate using Sockets/ServerSockets and JSON messages, each message has a `message_type` to identify the scope of the message. There are two entities, a GameServer and a GameClient.

## GameServer
This is a ServerSocket that handles two GameClient(s) and the game logic/validation. \
There are 5 message types that are sent from the server to the clients:
#### game_start
Sent to both GameClients after they connect to the server
```javascript
{
	message_type: 'game_start',
	player_id: 2,  // player 1 or player 2
	enemy_id: 1,   // player 1 or player 2
	max_rounds: 3  // For how many rounds to play the game
}
```
#### board
Contains the board and the id of the player that has to make the move.
If the player does an illegal move he will receive the board back with the same `next_turn_player_id`
```javascript 
{
	message_type: 'board',
	next_turn_player_id: '1',
	board: [
		[,,], [,,], [,,]
	]
}
```
#### end_round
Marks the end of a round, the board is refreshed
```javascript
{
	message_type: 'end_round',
	player_1_score: 0,
	player_2_score: 1,
	current_round_count: 1
}
```
#### end_game
Marks the end of a game, sends the win conditions
```javascript
{
	message_type: 'end_game',
	player_1_score: 0,
	player_2_score: 1
}
```
#### disconnect
Received from client Y if client X disconnects
```javascript
{
	message_type: 'disconnect'
}
```

## GameClient
There is 1 message that the client sends to the server
#### make_move
Makes a move on the board
```javascript
{
	message_type: 'make_move',
	player_id: 1, // Used to identify who is making the move (1 or 2)
	row: 0,
	col: 0
}
```

