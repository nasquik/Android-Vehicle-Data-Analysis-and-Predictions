CREATE DATABASE trafficDatabase;
USE trafficDatabase;
CREATE TABLE trafficData(
	timestep FLOAT,
	device_id INT,
	real_lat DOUBLE NOT NULL,
	real_long DOUBLE NOT NULL,
	predicted_lat DOUBLE,
	predicted_long DOUBLE,
	real_RSSI DOUBLE NOT NULL,
	real_throughput DOUBLE NOT NULL,
	predicted_RSSI DOUBLE,
	predicted_throughput DOUBLE,
	primary key(timestep,device_id)
);
