CREATE OR REPLACE FUNCTION update_sector_occupancy()
    RETURNS TRIGGER AS
$$
BEGIN
    RAISE NOTICE 'Trigger: update_sector_occupancy activated for Sector ID: %', COALESCE(NEW.sector_id, OLD.sector_id);


    IF TG_OP = 'INSERT' AND NEW.sector_id IS NOT NULL THEN
        UPDATE sector
        SET occupancy = occupancy + 1
        WHERE id = NEW.sector_id;


    ELSIF TG_OP = 'DELETE' AND OLD.sector_id IS NOT NULL THEN
        UPDATE sector
        SET occupancy = occupancy - 1
        WHERE id = OLD.sector_id;


    ELSIF TG_OP = 'UPDATE' THEN

        IF OLD.sector_id IS NULL AND NEW.sector_id IS NOT NULL THEN
            UPDATE sector
            SET occupancy = occupancy + 1
            WHERE id = NEW.sector_id;

        ELSIF OLD.sector_id IS NOT NULL AND NEW.sector_id IS NULL THEN
            UPDATE sector
            SET occupancy = occupancy - 1
            WHERE id = OLD.sector_id;

        ELSIF OLD.sector_id IS NOT NULL AND NEW.sector_id IS NOT NULL AND OLD.sector_id <> NEW.sector_id THEN

            UPDATE sector
            SET occupancy = occupancy - 1
            WHERE id = OLD.sector_id;

            UPDATE sector
            SET occupancy = occupancy + 1
            WHERE id = NEW.sector_id;
        END IF;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION update_sector_availability()
    RETURNS TRIGGER AS
$$
BEGIN

    IF NEW.capacity <= NEW.occupancy THEN
        NEW.is_available := false;
    ELSE
        NEW.is_available := true;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION update_slot_availability()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE available_slots
        SET is_available = FALSE
        WHERE id = NEW.slot_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE available_slots
        SET is_available = TRUE
        WHERE id = OLD.slot_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- (не оч нужна или заменить потом на isAvailable проверку)
CREATE OR REPLACE FUNCTION check_sector_capacity()
    RETURNS TRIGGER AS
$$
BEGIN
    IF (SELECT COUNT(*) FROM pet WHERE sector_id = NEW.sector_id) >=
       (SELECT capacity FROM sector WHERE id = NEW.sector_id) THEN
        RAISE EXCEPTION 'Sector capacity exceeded.';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

