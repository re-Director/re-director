CREATE TABLE IF NOT EXISTS redirect_hit_hourly
(
    redirect_id INTEGER  NOT NULL,
    hour        DATETIME NOT NULL,
    hits        INTEGER  NOT NULL,
    PRIMARY KEY (redirect_id, hour)
);

CREATE INDEX idx_redirect_hit_hourly_redirect ON redirect_hit_hourly (redirect_id);
