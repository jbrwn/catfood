CREATE OR REPLACE FUNCTION update_modified_on()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified_on = now();
    RETURN NEW;
END;
$$ language 'plpgsql';
