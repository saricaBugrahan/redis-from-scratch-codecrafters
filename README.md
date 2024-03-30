# Redis Server Implementation

This repository contains a Java implementation of a Redis server, providing a lightweight, scalable, and high-performance key-value store. Redis is commonly used as a cache, message broker, and for real-time analytics.

## Overview

This Redis server implementation is designed to mimic the functionality of the popular Redis database system. It includes components for handling Redis commands, managing replication, ensuring data persistence, and configuring server settings.

## Classes Overview

1. **RedisCommandHandler**: Handles incoming Redis commands from clients, processes them, and sends appropriate responses. It also manages replication-related tasks, such as waiting for acknowledgments from replica servers.

2. **RedisEncoder**: Provides utility methods for encoding Redis responses into the RESP (REdis Serialization Protocol) format, making it easy to format responses before sending them to clients.

3. **RedisRDBImpl**: Implements the Redis RDB (Redis DataBase) functionality for persistent storage and retrieval of key-value pairs. It also supports expiration times for keys and manages the storage of Redis streams.

4. **RedisReplicaACKWaitHandler**: Manages the waiting process for acknowledgment from Redis replica servers during replication. It ensures that the expected number of acknowledgments is received within the specified duration.

5. **RedisReplicaServer**: Represents a Redis replica server, responsible for connecting to a master server, sending and receiving commands, and managing replication configuration. Replica servers replicate data from the master server to ensure fault tolerance and high availability.

6. **RedisServerConfiguration**: Manages configuration parameters of the Redis server, including port number, role (master or slave), replication settings, and RDB file settings. It allows users to customize the server's behavior via command-line arguments.

7. **RedisTimeoutListener**: Monitors expiration times of keys in the Redis database and removes expired keys to ensure efficient memory usage and data consistency.

8. **RedisStreamEntryRecord**: A record class representing entries in a Redis stream, which is a powerful data structure for handling real-time event streams.

## Usage

To use the Redis server implementation, follow these steps:

1. Compile the Java source files using your preferred Java compiler or build tool.

2. Run the Redis server by executing the main class, typically `RedisServerConfiguration`. You can specify configuration parameters such as the port number, replication settings, and RDB file settings via command-line arguments.

3. Connect Redis clients to the server using appropriate client libraries or tools. Clients can send Redis commands to the server and receive responses according to the Redis protocol.

## Features

- **Replication**: Supports master-slave replication, allowing for data replication across multiple Redis servers for fault tolerance and scalability.

- **Persistence**: Implements RDB persistence for saving the dataset to disk at regular intervals, ensuring durability and data recovery in case of server crashes or restarts.

- **Key-Value Store**: Provides a flexible key-value store with support for various data types, including strings, hashes, lists, sets, sorted sets, and streams.

- **Real-Time Streaming**: Supports Redis streams, a powerful data structure for handling real-time event streams with features like consumer groups, message acknowledgment, and message retention.

## Contributing

Contributions to this Redis server implementation are welcome! If you encounter any bugs, have suggestions for improvements, or want to add new features, please open an issue or submit a pull request. Your contributions help make this project better for everyone.

## License

This Redis server implementation is licensed under the [MIT License](LICENSE), allowing for both personal and commercial use with attribution.
