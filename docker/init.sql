CREATE TABLE IF NOT EXISTS articles (
    id SERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    lead TEXT,
    body TEXT NOT NULL,
    publication_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sections TEXT[] DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_articles_publication_date ON articles(publication_date);

COPY articles (title, lead, body, publication_date, sections) 
FROM '/docker-entrypoint-initdb.d/articles.csv' 
DELIMITER ',' 
CSV HEADER;
