CREATE TABLE IF NOT EXISTS redirect_hit_daily
(
    redirect_id INTEGER NOT NULL,
    day         DATE    NOT NULL,
    hits        INTEGER NOT NULL,
    PRIMARY KEY (redirect_id, day)
);
