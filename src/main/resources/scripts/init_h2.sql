-- Initialize a simple table for the DB demo
DROP TABLE IF EXISTS demo_users;

CREATE TABLE demo_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- Insert an initial record using a variable from the test
INSERT INTO demo_users (username, status) VALUES ('${init_user_name}', 'ACTIVE');
