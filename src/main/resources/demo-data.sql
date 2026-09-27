INSERT INTO employees(full_name, email, department) VALUES
    ('Анна Петрова', 'a.petrova@example.com', 'Отдел продаж'),
    ('Иван Соколов', 'i.sokolov@example.com', 'ИТ-отдел'),
    ('Мария Волкова', 'm.volkova@example.com', 'Бухгалтерия'),
    ('Алексей Морозов', 'a.morozov@example.com', 'Отдел закупок'),
    ('Елена Новикова', 'e.novikova@example.com', 'Юридический отдел')
ON CONFLICT (email) DO NOTHING;

INSERT INTO business_trips(employee_id, destination, purpose, start_date, end_date, status, planned_cost)
SELECT e.id, 'Казань', 'Переговоры с заказчиком', CURRENT_DATE + 7, CURRENT_DATE + 9, 'PLANNED', 35000.00
FROM employees e
WHERE e.email = 'a.petrova@example.com'
  AND NOT EXISTS (SELECT 1 FROM business_trips t WHERE t.employee_id = e.id AND t.purpose = 'Переговоры с заказчиком');

INSERT INTO business_trips(employee_id, destination, purpose, start_date, end_date, status, planned_cost)
SELECT e.id, 'Санкт-Петербург', 'Внедрение информационной системы', CURRENT_DATE + 3, CURRENT_DATE + 5, 'APPROVED', 48000.00
FROM employees e
WHERE e.email = 'i.sokolov@example.com'
  AND NOT EXISTS (SELECT 1 FROM business_trips t WHERE t.employee_id = e.id AND t.purpose = 'Внедрение информационной системы');

INSERT INTO business_trips(employee_id, destination, purpose, start_date, end_date, status, planned_cost)
SELECT e.id, 'Екатеринбург', 'Аудит филиала', CURRENT_DATE - 1, CURRENT_DATE + 2, 'IN_PROGRESS', 42000.00
FROM employees e
WHERE e.email = 'm.volkova@example.com'
  AND NOT EXISTS (SELECT 1 FROM business_trips t WHERE t.employee_id = e.id AND t.purpose = 'Аудит филиала');

INSERT INTO business_trips(employee_id, destination, purpose, start_date, end_date, status, planned_cost, actual_cost)
SELECT e.id, 'Новосибирск', 'Переговоры с поставщиками', CURRENT_DATE - 10, CURRENT_DATE - 7, 'COMPLETED', 65000.00, 63250.00
FROM employees e
WHERE e.email = 'a.morozov@example.com'
  AND NOT EXISTS (SELECT 1 FROM business_trips t WHERE t.employee_id = e.id AND t.purpose = 'Переговоры с поставщиками');

INSERT INTO business_trips(employee_id, destination, purpose, start_date, end_date, status, planned_cost)
SELECT e.id, 'Сочи', 'Участие в конференции', CURRENT_DATE + 14, CURRENT_DATE + 18, 'CANCELLED', 55000.00
FROM employees e
WHERE e.email = 'e.novikova@example.com'
  AND NOT EXISTS (SELECT 1 FROM business_trips t WHERE t.employee_id = e.id AND t.purpose = 'Участие в конференции');

INSERT INTO transport_tickets(trip_id, transport_type, carrier, ticket_number, departure_place, arrival_place, departure_at, arrival_at, cost)
SELECT t.id, 'TRAIN', 'РЖД', 'DEMO-KZN-001', 'Москва', 'Казань',
       (CURRENT_DATE + 7) + TIME '07:30', (CURRENT_DATE + 7) + TIME '14:10', 6200.00
FROM business_trips t JOIN employees e ON e.id = t.employee_id
WHERE e.email = 'a.petrova@example.com' AND t.purpose = 'Переговоры с заказчиком'
  AND NOT EXISTS (SELECT 1 FROM transport_tickets WHERE ticket_number = 'DEMO-KZN-001');

INSERT INTO transport_tickets(trip_id, transport_type, carrier, ticket_number, departure_place, arrival_place, departure_at, arrival_at, cost)
SELECT t.id, 'PLANE', 'Аэрофлот', 'DEMO-SPB-002', 'Москва', 'Санкт-Петербург',
       (CURRENT_DATE + 3) + TIME '09:00', (CURRENT_DATE + 3) + TIME '10:30', 8900.00
FROM business_trips t JOIN employees e ON e.id = t.employee_id
WHERE e.email = 'i.sokolov@example.com' AND t.purpose = 'Внедрение информационной системы'
  AND NOT EXISTS (SELECT 1 FROM transport_tickets WHERE ticket_number = 'DEMO-SPB-002');

INSERT INTO transport_tickets(trip_id, transport_type, carrier, ticket_number, departure_place, arrival_place, departure_at, arrival_at, cost)
SELECT t.id, 'PLANE', 'S7 Airlines', 'DEMO-EKB-003', 'Москва', 'Екатеринбург',
       (CURRENT_DATE - 1) + TIME '06:40', (CURRENT_DATE - 1) + TIME '09:10', 11200.00
FROM business_trips t JOIN employees e ON e.id = t.employee_id
WHERE e.email = 'm.volkova@example.com' AND t.purpose = 'Аудит филиала'
  AND NOT EXISTS (SELECT 1 FROM transport_tickets WHERE ticket_number = 'DEMO-EKB-003');

INSERT INTO accommodations(trip_id, hotel_name, address, check_in_date, check_out_date, cost)
SELECT t.id, 'Отель Казань', 'ул. Баумана, 10', CURRENT_DATE + 7, CURRENT_DATE + 9, 14000.00
FROM business_trips t JOIN employees e ON e.id = t.employee_id
WHERE e.email = 'a.petrova@example.com' AND t.purpose = 'Переговоры с заказчиком'
  AND NOT EXISTS (SELECT 1 FROM accommodations a WHERE a.trip_id = t.id AND a.hotel_name = 'Отель Казань');

INSERT INTO accommodations(trip_id, hotel_name, address, check_in_date, check_out_date, cost)
SELECT t.id, 'Невский Отель', 'Невский проспект, 55', CURRENT_DATE + 3, CURRENT_DATE + 5, 18500.00
FROM business_trips t JOIN employees e ON e.id = t.employee_id
WHERE e.email = 'i.sokolov@example.com' AND t.purpose = 'Внедрение информационной системы'
  AND NOT EXISTS (SELECT 1 FROM accommodations a WHERE a.trip_id = t.id AND a.hotel_name = 'Невский Отель');

INSERT INTO accommodations(trip_id, hotel_name, address, check_in_date, check_out_date, cost)
SELECT t.id, 'Урал Бизнес Отель', 'ул. Малышева, 40', CURRENT_DATE - 1, CURRENT_DATE + 2, 16000.00
FROM business_trips t JOIN employees e ON e.id = t.employee_id
WHERE e.email = 'm.volkova@example.com' AND t.purpose = 'Аудит филиала'
  AND NOT EXISTS (SELECT 1 FROM accommodations a WHERE a.trip_id = t.id AND a.hotel_name = 'Урал Бизнес Отель');

INSERT INTO trip_expenses(trip_id, category, expense_date, description, amount)
SELECT t.id, 'MEALS', CURRENT_DATE, 'Питание во время аудита', 1850.00
FROM business_trips t JOIN employees e ON e.id = t.employee_id
WHERE e.email = 'm.volkova@example.com' AND t.purpose = 'Аудит филиала'
  AND NOT EXISTS (SELECT 1 FROM trip_expenses x WHERE x.trip_id = t.id AND x.description = 'Питание во время аудита');

INSERT INTO trip_expenses(trip_id, category, expense_date, description, amount)
SELECT t.id, 'TRANSPORT', CURRENT_DATE - 9, 'Такси до офиса поставщика', 1350.00
FROM business_trips t JOIN employees e ON e.id = t.employee_id
WHERE e.email = 'a.morozov@example.com' AND t.purpose = 'Переговоры с поставщиками'
  AND NOT EXISTS (SELECT 1 FROM trip_expenses x WHERE x.trip_id = t.id AND x.description = 'Такси до офиса поставщика');

INSERT INTO trip_expenses(trip_id, category, expense_date, description, amount)
SELECT t.id, 'MEALS', CURRENT_DATE - 8, 'Представительский обед', 3200.00
FROM business_trips t JOIN employees e ON e.id = t.employee_id
WHERE e.email = 'a.morozov@example.com' AND t.purpose = 'Переговоры с поставщиками'
  AND NOT EXISTS (SELECT 1 FROM trip_expenses x WHERE x.trip_id = t.id AND x.description = 'Представительский обед');
