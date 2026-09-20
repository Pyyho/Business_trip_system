INSERT INTO employees(full_name, email, department) VALUES
    ('Анна Петрова', 'a.petrova@example.com', 'Отдел продаж'),
    ('Иван Соколов', 'i.sokolov@example.com', 'ИТ')
ON CONFLICT (email) DO NOTHING;

INSERT INTO business_trips(employee_id, destination, purpose, start_date, end_date, status, planned_cost)
SELECT id, 'Казань', 'Переговоры с заказчиком', CURRENT_DATE + 7, CURRENT_DATE + 9, 'PLANNED', 35000.00
FROM employees WHERE email = 'a.petrova@example.com'
  AND NOT EXISTS (SELECT 1 FROM business_trips WHERE destination = 'Казань' AND purpose = 'Переговоры с заказчиком');
