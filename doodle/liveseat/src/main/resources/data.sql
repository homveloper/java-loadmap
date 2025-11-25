-- 초기 공연 데이터
INSERT INTO concerts (title, description, start_date, end_date, price_vip, price_r, price_s, total_seats, available_seats, version, created_at, updated_at)
VALUES
    ('뮤지컬 레미제라블',
     '빅토르 위고의 대작을 원작으로 한 감동적인 뮤지컬. 혁명의 시대를 살아가는 인물들의 사랑과 용기, 희생을 그린 작품입니다.',
     '2025-01-15', '2025-03-31',
     150000, 100000, 70000,
     200, 200, 0,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('콘서트: BTS 월드투어',
     '전 세계적으로 사랑받는 K-POP 그룹 BTS의 서울 공연. 최고의 무대와 퍼포먼스를 경험하세요!',
     '2025-02-10', '2025-02-12',
     200000, 150000, 100000,
     500, 500, 0,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('오페라 라 트라비아타',
     '베르디의 명작 오페라. 아름다운 선율과 비극적인 사랑 이야기가 어우러진 클래식 공연입니다.',
     '2025-03-01', '2025-04-30',
     120000, 80000, 50000,
     150, 150, 0,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('연극: 햄릿',
     '셰익스피어의 4대 비극 중 하나. 복수와 광기, 사랑과 배신을 다룬 불후의 명작을 무대에서 만나보세요.',
     '2025-01-20', '2025-02-28',
     80000, 60000, 40000,
     100, 100, 0,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    ('발레: 백조의 호수',
     '차이콥스키의 음악과 함께하는 고전 발레의 정수. 우아하고 환상적인 무대를 선사합니다.',
     '2025-04-01', '2025-05-15',
     130000, 90000, 60000,
     180, 180, 0,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 샘플 예매 데이터 (테스트용)
INSERT INTO bookings (concert_id, customer_name, customer_email, booking_date, seat_grade, seat_count, total_price, status, created_at, updated_at)
VALUES
    (1, '홍길동', 'hong@example.com', '2025-02-15', 'VIP', 2, 300000, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1, '김철수', 'kim@example.com', '2025-02-20', 'R', 4, 400000, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, '이영희', 'lee@example.com', '2025-02-10', 'S', 3, 300000, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, '박민수', 'park@example.com', '2025-03-10', 'VIP', 1, 120000, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, '최지연', 'choi@example.com', '2025-01-25', 'R', 2, 120000, 'CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 예매에 따른 잔여 좌석 업데이트
UPDATE concerts SET available_seats = available_seats - 2 WHERE id = 1; -- 홍길동 예매
UPDATE concerts SET available_seats = available_seats - 4 WHERE id = 1; -- 김철수 예매
UPDATE concerts SET available_seats = available_seats - 3 WHERE id = 2; -- 이영희 예매
UPDATE concerts SET available_seats = available_seats - 1 WHERE id = 3; -- 박민수 예매
-- 최지연 예매는 취소되었으므로 좌석 차감 안 함
