-- Run once against PostgreSQL after making a backup and while PennyWise is stopped.
-- Existing data must be empty (as confirmed before this ownership change).
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE monthly_budgets ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE category_monthly_budgets ADD COLUMN IF NOT EXISTS user_id BIGINT;

ALTER TABLE expenses ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE monthly_budgets ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE category_monthly_budgets ALTER COLUMN user_id SET NOT NULL;

-- Remove any Hibernate-generated ownership FKs so this script can also be
-- applied after the application has already added the new columns.
DO $$
DECLARE constraint_row RECORD;
BEGIN
    FOR constraint_row IN
        SELECT conrelid::regclass AS table_name, conname
        FROM pg_constraint
        WHERE contype = 'f'
          AND conrelid IN ('expenses'::regclass, 'monthly_budgets'::regclass,
                           'category_monthly_budgets'::regclass)
          AND conkey = ARRAY[(SELECT attnum FROM pg_attribute
                              WHERE attrelid = conrelid AND attname = 'user_id')]::smallint[]
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', constraint_row.table_name, constraint_row.conname);
    END LOOP;
END $$;

ALTER TABLE expenses ADD CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES app_users(id);
ALTER TABLE monthly_budgets ADD CONSTRAINT fk_monthly_budgets_user FOREIGN KEY (user_id) REFERENCES app_users(id);
ALTER TABLE category_monthly_budgets ADD CONSTRAINT fk_category_monthly_budgets_user FOREIGN KEY (user_id) REFERENCES app_users(id);

-- Remove the former global uniqueness constraints so each user can have a
-- budget for the same month and category.
DO $$
DECLARE constraint_row RECORD;
BEGIN
    FOR constraint_row IN
        SELECT conrelid::regclass AS table_name, conname
        FROM pg_constraint
        WHERE contype = 'u'
          AND conrelid IN ('monthly_budgets'::regclass, 'category_monthly_budgets'::regclass)
    LOOP
        EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', constraint_row.table_name, constraint_row.conname);
    END LOOP;
END $$;

ALTER TABLE monthly_budgets
    ADD CONSTRAINT uk_monthly_budget_user_month UNIQUE (user_id, budget_month);
ALTER TABLE category_monthly_budgets
    ADD CONSTRAINT uk_category_budget_user_month_category UNIQUE (user_id, budget_month, category);
