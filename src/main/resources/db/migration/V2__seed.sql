INSERT INTO doctors (name, specialty, email, phone) VALUES
    ('Dr Gregory House', 'Diagnostics', 'house@example.com', '+880100000001'),
    ('Dr Lisa Cuddy', 'Endocrinology', 'cuddy@example.com', '+880100000002');

INSERT INTO patients (id, name, location, email, date_of_birth, registered_date) VALUES
    (UUID_TO_BIN('11111111-1111-1111-1111-111111111111'), 'John Doe', 'Dhaka', 'john.doe@example.com', '1990-01-01', '2026-10-01'),
    (UUID_TO_BIN('22222222-2222-2222-2222-222222222222'), 'Jane Roe', 'Chattogram', 'jane.roe@example.com', '1992-05-15', '2026-10-02');
