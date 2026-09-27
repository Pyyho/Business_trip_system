CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL CHECK (char_length(trim(full_name)) >= 3),
    email VARCHAR(180) NOT NULL UNIQUE,
    department VARCHAR(120) NOT NULL CHECK (char_length(trim(department)) >= 2)
);

CREATE TABLE IF NOT EXISTS business_trips (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    destination VARCHAR(180) NOT NULL CHECK (char_length(trim(destination)) >= 2),
    purpose VARCHAR(500) NOT NULL CHECK (char_length(trim(purpose)) >= 3),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    planned_cost NUMERIC(14, 2) NOT NULL CHECK (planned_cost >= 0),
    actual_cost NUMERIC(14, 2) CHECK (actual_cost IS NULL OR actual_cost >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_trip_dates CHECK (end_date >= start_date),
    CONSTRAINT ck_trip_status CHECK (status IN ('PLANNED', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_trip_employee ON business_trips(employee_id);
CREATE INDEX IF NOT EXISTS idx_trip_status ON business_trips(status);
CREATE INDEX IF NOT EXISTS idx_trip_dates ON business_trips(start_date, end_date);

CREATE TABLE IF NOT EXISTS transport_tickets (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES business_trips(id) ON DELETE CASCADE,
    transport_type VARCHAR(30) NOT NULL,
    carrier VARCHAR(150) NOT NULL CHECK (char_length(trim(carrier)) >= 2),
    ticket_number VARCHAR(80) NOT NULL UNIQUE,
    departure_place VARCHAR(180) NOT NULL,
    arrival_place VARCHAR(180) NOT NULL,
    departure_at TIMESTAMP NOT NULL,
    arrival_at TIMESTAMP NOT NULL,
    cost NUMERIC(14, 2) NOT NULL CHECK (cost >= 0),
    CONSTRAINT ck_transport_type CHECK (transport_type IN ('PLANE', 'TRAIN', 'BUS', 'CAR', 'OTHER')),
    CONSTRAINT ck_ticket_times CHECK (arrival_at >= departure_at)
);

CREATE TABLE IF NOT EXISTS accommodations (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES business_trips(id) ON DELETE CASCADE,
    hotel_name VARCHAR(180) NOT NULL CHECK (char_length(trim(hotel_name)) >= 2),
    address VARCHAR(300) NOT NULL CHECK (char_length(trim(address)) >= 3),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    cost NUMERIC(14, 2) NOT NULL CHECK (cost >= 0),
    CONSTRAINT ck_accommodation_dates CHECK (check_out_date >= check_in_date)
);

CREATE TABLE IF NOT EXISTS trip_expenses (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES business_trips(id) ON DELETE CASCADE,
    category VARCHAR(30) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(300) NOT NULL CHECK (char_length(trim(description)) >= 3),
    amount NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    CONSTRAINT ck_expense_category CHECK (category IN ('TRANSPORT', 'ACCOMMODATION', 'MEALS', 'COMMUNICATION', 'OTHER'))
);

CREATE INDEX IF NOT EXISTS idx_ticket_trip ON transport_tickets(trip_id);
CREATE INDEX IF NOT EXISTS idx_accommodation_trip ON accommodations(trip_id);
CREATE INDEX IF NOT EXISTS idx_expense_trip ON trip_expenses(trip_id);
