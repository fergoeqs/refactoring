DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
        CREATE INDEX IF NOT EXISTS idx_pet_owner_id ON pet(owner_id);
        CREATE INDEX IF NOT EXISTS idx_pet_actual_vet_id ON pet(actual_vet_id);
        CREATE INDEX IF NOT EXISTS idx_pet_sector_id ON pet(sector_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'appointment') THEN
        CREATE INDEX IF NOT EXISTS idx_appointment_pet_id ON appointment(pet_id);
        CREATE INDEX IF NOT EXISTS idx_appointment_slot_id ON appointment(slot_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'anamnesis') THEN
        CREATE INDEX IF NOT EXISTS idx_anamnesis_pet_id ON anamnesis(pet_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'diagnosis') THEN
        CREATE INDEX IF NOT EXISTS idx_diagnosis_anamnesis_id ON diagnosis(anamnesis_id);
        CREATE INDEX IF NOT EXISTS idx_diagnosis_date ON diagnosis(date);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'medical_procedure') THEN
        CREATE INDEX IF NOT EXISTS idx_medical_procedure_pet_id ON medical_procedure(pet_id);
        CREATE INDEX IF NOT EXISTS idx_medical_procedure_anamnesis_id ON medical_procedure(anamnesis_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'treatment') THEN
        CREATE INDEX IF NOT EXISTS idx_treatment_pet_id ON treatment(pet_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'health_update') THEN
        CREATE INDEX IF NOT EXISTS idx_health_update_pet_id ON health_update(pet_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'quarantine') THEN
        CREATE INDEX IF NOT EXISTS idx_quarantine_sector_id ON quarantine(sector_id);
        CREATE INDEX IF NOT EXISTS idx_quarantine_pet_id ON quarantine(pet_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'App_User') THEN
        CREATE INDEX IF NOT EXISTS idx_user_clinic_id ON "App_User"(clinic_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notification') THEN
        CREATE INDEX IF NOT EXISTS idx_notification_app_user_id ON notification(app_user_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'rating_and_reviews') THEN
        CREATE INDEX IF NOT EXISTS idx_rating_vet_id ON rating_and_reviews(vet_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'diagnostic_attachment') THEN
        CREATE INDEX IF NOT EXISTS idx_attachment_anamnesis_id ON diagnostic_attachment(anamnesis_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'sector') THEN
        CREATE INDEX IF NOT EXISTS idx_sector_category ON sector(category);
        CREATE INDEX IF NOT EXISTS idx_sector_is_available ON sector(is_available);
    END IF;
END $$;
