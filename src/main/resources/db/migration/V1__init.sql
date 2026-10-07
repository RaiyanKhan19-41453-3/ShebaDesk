CREATE TABLE IF NOT EXISTS patients (
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    registered_date DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_patients_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS doctors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_doctors_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL,
    patient_id BINARY(16) NOT NULL,
    appointment_date DATE NOT NULL,
    notes VARCHAR(1000),
    PRIMARY KEY (id),
    CONSTRAINT uk_appointments_doctor_date UNIQUE (doctor_id, appointment_date),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
);
