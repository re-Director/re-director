CREATE TABLE IF NOT EXISTS redirect_hit
(
    redirect_id INTEGER  NOT NULL,
    hit_time    DATETIME NOT NULL
);

CREATE INDEX idx_redirect_hit_time ON redirect_hit (hit_time);
