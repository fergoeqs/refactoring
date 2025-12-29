CREATE OR REPLACE TRIGGER trigger_update_sector_occupancy
    AFTER INSERT OR DELETE OR UPDATE
    ON Pet
    FOR EACH ROW
EXECUTE FUNCTION update_sector_occupancy();

CREATE TRIGGER trigger_update_sector_availability
    BEFORE UPDATE
    ON Sector
    FOR EACH ROW
EXECUTE FUNCTION update_sector_availability();

CREATE TRIGGER trigger_update_slot_availability
    AFTER INSERT OR DELETE ON Appointment
    FOR EACH ROW
EXECUTE FUNCTION update_slot_availability();

CREATE TRIGGER trigger_check_sector_capacity
    BEFORE INSERT OR UPDATE
    ON Pet
    FOR EACH ROW
EXECUTE FUNCTION check_sector_capacity();

