-- Sample round for local development so ranking and boulder points have something
-- to show right away. Runs on every start; every statement is idempotent through
-- ON CONFLICT DO NOTHING and leaves existing data untouched.
--
-- Wired up via spring.sql.init.data-locations in application.yaml - for a real
-- competition set spring.sql.init.mode to never there.

-- 15 boulders.
INSERT INTO boulder (number)
SELECT generate_series(1, 15)
ON CONFLICT DO NOTHING;

-- Every competitor has the password "geheim123".
INSERT INTO competitor (name, gender, password_hash)
VALUES ('Alex Gruber', 'MALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm'),
       ('Ben Huber', 'MALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm'),
       ('Chris Maier', 'MALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm'),
       ('David Bauer', 'MALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm'),
       ('Emil Wagner', 'MALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm'),
       ('Felix Berger', 'MALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm'),
       ('Greta Fischer', 'FEMALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm'),
       ('Hanna Weber', 'FEMALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm'),
       ('Ida Schmidt', 'FEMALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm'),
       ('Julia Koch', 'FEMALE', '$2y$10$f1BfsZRcmv3wuyuAPMfEZuSKrsExG5kML6q30bbSnDLh9Ed1JHgEm')
ON CONFLICT DO NOTHING;

-- Ascents: first array is what they sent, second which of those were flashed
-- (always a subset). Deliberately uneven - the low numbers have many ascents and are
-- worth little accordingly, boulder 15 nobody sent and it sits at the full 1000.
INSERT INTO ascent (competitor_id, boulder_id, flashed)
SELECT c.id, b.id, sent.number = ANY (v.flashed)
FROM (VALUES ('Alex Gruber', ARRAY [1,2,3,4,5,6,8,10,12], ARRAY [1,2,4]),
             ('Ben Huber', ARRAY [1,2,3,4,6,7,9], ARRAY [1,3]),
             ('Chris Maier', ARRAY [1,2,3,4,5,6,7,8,9,11,13], ARRAY [1,2,3,5]),
             ('David Bauer', ARRAY [1,2,3,5], ARRAY [1]),
             ('Emil Wagner', ARRAY [1,2,3,4,5,6,7,8,10,12,14], ARRAY [1,2,3,4,6]),
             ('Felix Berger', ARRAY [1,2,4], ARRAY []::int[]),
             ('Greta Fischer', ARRAY [1,2,3,4,5,6,8,11], ARRAY [1,2,3]),
             ('Hanna Weber', ARRAY [1,2,3,4,5,7], ARRAY [1,2]),
             ('Ida Schmidt', ARRAY [1,2,3,4,5,6,7,8,9,10,13], ARRAY [1,2,3,4,6]),
             ('Julia Koch', ARRAY [1,2,4], ARRAY [1])) AS v (name, sent, flashed)
         CROSS JOIN LATERAL unnest(v.sent) AS sent (number)
         JOIN competitor c ON c.name = v.name
         JOIN boulder b ON b.number = sent.number
ON CONFLICT DO NOTHING;
