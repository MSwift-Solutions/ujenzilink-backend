-- ============================================================
-- UjenziLink Search: PostgreSQL Full-Text Search Setup
-- ============================================================
-- Run this script ONCE on your PostgreSQL database.
-- It enables pg_trgm for fuzzy/trigram search and adds
-- GIN indexes on computed tsvector columns so all FTS queries
-- use index scans instead of sequential scans.
--
-- NOTE: All search queries in SearchRepository.java use
-- inline to_tsvector() calls (no stored columns) so this
-- migration only adds the extension + GIN indexes on
-- individual text columns for ILIKE / trigram operations.
-- ============================================================

-- 1. Enable trigram extension (required for ILIKE to use GIN indexes)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ─── USERS ───────────────────────────────────────────────────────────────────

-- Trigram index on full name concatenation (supports ILIKE '%..%' efficiently)
CREATE INDEX IF NOT EXISTS idx_users_fts_name
    ON users USING GIN (
        (LOWER(COALESCE(first_name, '') || ' ' || COALESCE(middle_name, '') || ' ' || COALESCE(last_name, '')))
        gin_trgm_ops
    );

-- Trigram index on username for fast ILIKE lookups
CREATE INDEX IF NOT EXISTS idx_users_fts_username
    ON users USING GIN (LOWER(COALESCE(username, '')) gin_trgm_ops);

-- GIN index on the full-text search vector used in to_tsvector queries
-- Covering: first_name, middle_name, last_name, username, bio, skills, location
CREATE INDEX IF NOT EXISTS idx_users_search_vector
    ON users USING GIN (
        to_tsvector('english',
            COALESCE(first_name, '') || ' ' ||
            COALESCE(middle_name, '') || ' ' ||
            COALESCE(last_name, '') || ' ' ||
            COALESCE(username, '') || ' ' ||
            COALESCE(bio, '') || ' ' ||
            COALESCE(skills, '') || ' ' ||
            COALESCE(location, '')
        )
    );

-- ─── PROJECTS ────────────────────────────────────────────────────────────────

-- Trigram index on project title
CREATE INDEX IF NOT EXISTS idx_projects_fts_title
    ON projects USING GIN (LOWER(COALESCE(title, '')) gin_trgm_ops);

-- Trigram index on project location
CREATE INDEX IF NOT EXISTS idx_projects_fts_location
    ON projects USING GIN (LOWER(COALESCE(location, '')) gin_trgm_ops);

-- GIN index on the full-text search vector for projects
-- Covering: title, description, location, project_type, project_status
CREATE INDEX IF NOT EXISTS idx_projects_search_vector
    ON projects USING GIN (
        to_tsvector('english',
            COALESCE(title, '') || ' ' ||
            COALESCE(description, '') || ' ' ||
            COALESCE(location, '') || ' ' ||
            COALESCE(CAST(project_type AS TEXT), '') || ' ' ||
            COALESCE(CAST(project_status AS TEXT), '')
        )
    );

-- ─── POSTS ───────────────────────────────────────────────────────────────────

-- GIN index on the full-text search vector for posts
CREATE INDEX IF NOT EXISTS idx_posts_search_vector
    ON posts USING GIN (
        to_tsvector('english', COALESCE(content, ''))
    );

-- Trigram index on post content (supports ILIKE '%..%' efficiently)
CREATE INDEX IF NOT EXISTS idx_posts_fts_content
    ON posts USING GIN (LOWER(COALESCE(content, '')) gin_trgm_ops);
