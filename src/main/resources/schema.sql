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
