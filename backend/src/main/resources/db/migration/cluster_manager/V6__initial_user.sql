INSERT INTO users (name,password_hash,enabled,created_at,updated_at)
VALUES ('superuser','$2a$12$XQdirQ8iMErP0f4yISzFSeAZa20QCqwpi3KlQxZgvjWvm0hcI4f0S', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- Password is 'su1234'

INSERT INTO user_group_relations (user_id,group_id,added_at) 
VALUES ((SELECT id FROM "users" WHERE name='superuser'),
        (SELECT id FROM "groups" WHERE name='Superuser'),
        CURRENT_TIMESTAMP);
