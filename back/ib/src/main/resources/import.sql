INSERT INTO AUTHORITY (name) VALUES ('END_USER');  --1
INSERT INTO AUTHORITY (name) VALUES ('ADMIN');      --2

INSERT INTO members (email, name, surname, telephone_number, password, authority_id, is_enabled, last_password_reset_date) VALUES ('jnizvodno@gmail.com', 'Bandjelo', 'Kumara', '+381621131440', '$2a$12$MQAddY3inyWPRpEVTRtNDu.TvnS6b/kUwt/6XMOG6xEfKYSJgikCm' ,2, true, '2023-05-22 18:33:28.330677');
INSERT INTO ADMINS(id) VALUES(1);--admin021
INSERT INTO password (user_id, previous_passwords) VALUES (1, '$2a$10$j.RMb2Bzttn1x/Z.EDPNduWMzNLidG8Q.768w2qt484aC/E9Ha9am');

INSERT INTO members (email, name, surname, telephone_number, password, authority_id, is_enabled) VALUES ('miki@gmail.com', 'Tupatu', 'Serbedzija', '123456789', '$2a$12$O/NJDsbdC7Fzs1eZofJUcuH3VHQAwj5hJbyF9PoI5xg9fZrljtQau' ,1, true);
INSERT INTO END_USER(id) VALUES(2);--admin021