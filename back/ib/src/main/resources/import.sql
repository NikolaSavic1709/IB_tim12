INSERT INTO AUTHORITY (name) VALUES ('END_USER');  --1
INSERT INTO AUTHORITY (name) VALUES ('ADMIN');      --2

INSERT INTO members (email, name, surname, telephone_number, password, authority_id, is_enabled) VALUES ('mirkovicka01@gmail.com', 'Bandjelo', 'Kumara', '+381648261726', '$2a$12$O/NJDsbdC7Fzs1eZofJUcuH3VHQAwj5hJbyF9PoI5xg9fZrljtQau' ,2, true);
INSERT INTO ADMINS(id) VALUES(1);--admin021

INSERT INTO members (email, name, surname, telephone_number, password, authority_id, is_enabled) VALUES ('miki@gmail.com', 'Tupatu', 'Serbedzija', '123456789', '$2a$12$O/NJDsbdC7Fzs1eZofJUcuH3VHQAwj5hJbyF9PoI5xg9fZrljtQau' ,1, true);
INSERT INTO END_USER(id) VALUES(2);--admin021