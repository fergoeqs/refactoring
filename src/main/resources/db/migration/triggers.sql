DROP TRIGGER IF EXISTS trigger_update_sector_occupancy ON pet;
CREATE TRIGGER trigger_update_sector_occupancy
    AFTER INSERT OR DELETE OR UPDATE
    ON pet
    FOR EACH ROW
EXECUTE FUNCTION update_sector_occupancy();


DROP TRIGGER IF EXISTS trigger_update_sector_availability ON sector;
CREATE TRIGGER trigger_update_sector_availability
    BEFORE UPDATE
    ON sector
    FOR EACH ROW
EXECUTE FUNCTION update_sector_availability();


DROP TRIGGER IF EXISTS trigger_update_slot_availability ON appointment;
CREATE TRIGGER trigger_update_slot_availability
    AFTER INSERT OR DELETE ON appointment
    FOR EACH ROW
EXECUTE FUNCTION update_slot_availability();


DROP TRIGGER IF EXISTS trigger_check_sector_capacity ON pet;
CREATE TRIGGER trigger_check_sector_capacity
    BEFORE INSERT OR UPDATE
    ON pet
    FOR EACH ROW
EXECUTE FUNCTION check_sector_capacity();

