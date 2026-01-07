<!-- # Handshake
- use a handshake like in TCP : SYN,SYN-ACK, SYN-ACK-SYN
- once handshake successfully done, connect using HTTPS
- after data exchange, close the HTTPS connection 
- next time for data fetch redo the handshake or other methods like certificates or something. -->

# Control Plane 
    Controls the node connection initiation 
    - Technologies
      -- WebSocket
      -- MQTT
      -- NATS

# Data plane
    Actual data transfer between node and clustermanager
    - Technologies
    -- HTTPS
    -- gRPC streaming

- Data plane connections are NOT always open
- Control plane stays open but carries almost no data
- Node still initiates the data connection