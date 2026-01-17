# Tunneling Research

### 1. Node keeps sending http(s) requests to CM every X seconds

Pro: Sounds somewhat easy

Con: slow response. FE requests to CM need be like queued and sent as response.

### 2. Node sends http(s) request to CM if no connection is open. CM keeps connection open

Pro: Still somewhat easy

Con: Seems to require a bit of custom handling still.

### 3. websocket

Node sends an http(s) request to CM if no connection is open. Request contains an upgrade request to websocket.
Websocket as a permanent binary connection type allows for bidirectional communication.

Pro: appears to be rather easy to implement in Spring Boot.

Con: no real ssh

### 4. SSH

Could be implemented via JSch Library.

Pro: real ssh

Con: Sounds like a whole can of worms