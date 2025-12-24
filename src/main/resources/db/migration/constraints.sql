-- ============================================
-- Добавление недостающих ограничений целостности
-- ============================================
-- FK Constraints, CHECK Constraints, UNIQUE Constraints
-- Примечание: Ограничения добавляются только если таблицы уже существуют
-- (таблицы создаются Hibernate из Java-моделей)

DO $$
BEGIN
    -- ============================================
    -- FOREIGN KEY CONSTRAINTS
    -- ============================================
    
    -- App_User
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'App_User') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'Clinic') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_user_clinic' AND table_name = 'App_User') THEN
                ALTER TABLE "App_User" 
                ADD CONSTRAINT fk_user_clinic FOREIGN KEY (clinic_id) REFERENCES "Clinic"(id) ON DELETE SET NULL;
            END IF;
        END IF;
    END IF;
    
    -- Pet
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'App_User') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_pet_owner' AND table_name = 'pet') THEN
                ALTER TABLE pet 
                ADD CONSTRAINT fk_pet_owner FOREIGN KEY (owner_id) REFERENCES "App_User"(id) ON DELETE SET NULL;
            END IF;
            
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_pet_vet' AND table_name = 'pet') THEN
                ALTER TABLE pet 
                ADD CONSTRAINT fk_pet_vet FOREIGN KEY (actual_vet_id) REFERENCES "App_User"(id) ON DELETE SET NULL;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'sector') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_pet_sector' AND table_name = 'pet') THEN
                ALTER TABLE pet 
                ADD CONSTRAINT fk_pet_sector FOREIGN KEY (sector_id) REFERENCES sector(id) ON DELETE SET NULL;
            END IF;
        END IF;
    END IF;
    
    -- Appointment
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'appointment') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_appointment_pet' AND table_name = 'appointment') THEN
                ALTER TABLE appointment 
                ADD CONSTRAINT fk_appointment_pet FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'available_slots') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_appointment_slot' AND table_name = 'appointment') THEN
                ALTER TABLE appointment 
                ADD CONSTRAINT fk_appointment_slot FOREIGN KEY (slot_id) REFERENCES available_slots(id) ON DELETE SET NULL;
            END IF;
        END IF;
    END IF;
    
    -- Available_Slots
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'available_slots') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'App_User') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_slot_vet' AND table_name = 'available_slots') THEN
                ALTER TABLE available_slots 
                ADD CONSTRAINT fk_slot_vet FOREIGN KEY (vet_id) REFERENCES "App_User"(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- Anamnesis
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'anamnesis') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                           WHERE constraint_name = 'fk_anamnesis_pet' AND table_name = 'anamnesis') THEN
                ALTER TABLE anamnesis 
                ADD CONSTRAINT fk_anamnesis_pet FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'appointment') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_anamnesis_appointment' AND table_name = 'anamnesis') THEN
                ALTER TABLE anamnesis 
                ADD CONSTRAINT fk_anamnesis_appointment FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- Diagnosis
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'diagnosis') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'anamnesis') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_diagnosis_anamnesis' AND table_name = 'diagnosis') THEN
                ALTER TABLE diagnosis 
                ADD CONSTRAINT fk_diagnosis_anamnesis FOREIGN KEY (anamnesis_id) REFERENCES anamnesis(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- Treatment
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'treatment') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'diagnosis') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_treatment_diagnosis' AND table_name = 'treatment') THEN
                ALTER TABLE treatment 
                ADD CONSTRAINT fk_treatment_diagnosis FOREIGN KEY (diagnosis_id) REFERENCES diagnosis(id) ON DELETE SET NULL;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_treatment_pet' AND table_name = 'treatment') THEN
                ALTER TABLE treatment 
                ADD CONSTRAINT fk_treatment_pet FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- MedicalProcedure
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'medical_procedure') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_medical_procedure_pet' AND table_name = 'medical_procedure') THEN
                ALTER TABLE medical_procedure 
                ADD CONSTRAINT fk_medical_procedure_pet FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'App_User') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_medical_procedure_vet' AND table_name = 'medical_procedure') THEN
                ALTER TABLE medical_procedure 
                ADD CONSTRAINT fk_medical_procedure_vet FOREIGN KEY (vet_id) REFERENCES "App_User"(id) ON DELETE SET NULL;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'anamnesis') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_medical_procedure_anamnesis' AND table_name = 'medical_procedure') THEN
                ALTER TABLE medical_procedure 
                ADD CONSTRAINT fk_medical_procedure_anamnesis FOREIGN KEY (anamnesis_id) REFERENCES anamnesis(id) ON DELETE SET NULL;
            END IF;
        END IF;
    END IF;
    
    -- HealthUpdate
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'health_update') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_health_update_pet' AND table_name = 'health_update') THEN
                ALTER TABLE health_update 
                ADD CONSTRAINT fk_health_update_pet FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- Quarantine
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'quarantine') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'sector') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_quarantine_sector' AND table_name = 'quarantine') THEN
                ALTER TABLE quarantine 
                ADD CONSTRAINT fk_quarantine_sector FOREIGN KEY (sector_id) REFERENCES sector(id) ON DELETE SET NULL;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_quarantine_pet' AND table_name = 'quarantine') THEN
                ALTER TABLE quarantine 
                ADD CONSTRAINT fk_quarantine_pet FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'App_User') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_quarantine_vet' AND table_name = 'quarantine') THEN
                ALTER TABLE quarantine 
                ADD CONSTRAINT fk_quarantine_vet FOREIGN KEY (vet_id) REFERENCES "App_User"(id) ON DELETE SET NULL;
            END IF;
        END IF;
    END IF;
    
    -- Notification
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notification') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'App_User') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_notification_user' AND table_name = 'notification') THEN
                ALTER TABLE notification 
                ADD CONSTRAINT fk_notification_user FOREIGN KEY (app_user_id) REFERENCES "App_User"(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- RatingAndReviews
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'rating_and_reviews') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'App_User') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_rating_vet' AND table_name = 'rating_and_reviews') THEN
                ALTER TABLE rating_and_reviews 
                ADD CONSTRAINT fk_rating_vet FOREIGN KEY (vet_id) REFERENCES "App_User"(id) ON DELETE CASCADE;
            END IF;
            
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_rating_owner' AND table_name = 'rating_and_reviews') THEN
                ALTER TABLE rating_and_reviews 
                ADD CONSTRAINT fk_rating_owner FOREIGN KEY (owner_id) REFERENCES "App_User"(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- DiagnosticAttachment
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'diagnostic_attachment') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'anamnesis') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_attachment_anamnesis' AND table_name = 'diagnostic_attachment') THEN
                ALTER TABLE diagnostic_attachment 
                ADD CONSTRAINT fk_attachment_anamnesis FOREIGN KEY (anamnesis_id) REFERENCES anamnesis(id) ON DELETE CASCADE;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'diagnosis') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_attachment_diagnosis' AND table_name = 'diagnostic_attachment') THEN
                ALTER TABLE diagnostic_attachment 
                ADD CONSTRAINT fk_attachment_diagnosis FOREIGN KEY (diagnosis_id) REFERENCES diagnosis(id) ON DELETE SET NULL;
            END IF;
        END IF;
    END IF;
    
    -- BodyMarker
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'body_marker') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_body_marker_pet' AND table_name = 'body_marker') THEN
                ALTER TABLE body_marker 
                ADD CONSTRAINT fk_body_marker_pet FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'appointment') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_body_marker_appointment' AND table_name = 'body_marker') THEN
                ALTER TABLE body_marker 
                ADD CONSTRAINT fk_body_marker_appointment FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- Report
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'report') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'App_User') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_report_vet' AND table_name = 'report') THEN
                ALTER TABLE report 
                ADD CONSTRAINT fk_report_vet FOREIGN KEY (vet_id) REFERENCES "App_User"(id) ON DELETE SET NULL;
            END IF;
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'anamnesis') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_report_anamnesis' AND table_name = 'report') THEN
                ALTER TABLE report 
                ADD CONSTRAINT fk_report_anamnesis FOREIGN KEY (anamnesis_id) REFERENCES anamnesis(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- Queue
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'queue') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'appointment') THEN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                           WHERE constraint_name = 'fk_queue_appointment' AND table_name = 'queue') THEN
                ALTER TABLE queue 
                ADD CONSTRAINT fk_queue_appointment FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE;
            END IF;
        END IF;
    END IF;
    
    -- ============================================
    -- UNIQUE CONSTRAINTS
    -- ============================================
    
    -- Appointment: один слот может быть забронирован только один раз
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'appointment') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'uq_appointment_slot' AND table_name = 'appointment') THEN
            CREATE UNIQUE INDEX uq_appointment_slot ON appointment(slot_id) WHERE slot_id IS NOT NULL;
        END IF;
    END IF;
    
    -- Anamnesis: один appointment может иметь только один anamnesis (OneToOne)
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'anamnesis') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'uq_anamnesis_appointment' AND table_name = 'anamnesis') THEN
            CREATE UNIQUE INDEX uq_anamnesis_appointment ON anamnesis(appointment_id) WHERE appointment_id IS NOT NULL;
        END IF;
    END IF;
    
    -- RatingAndReviews: один владелец может оставить только один отзыв на ветеринара
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'rating_and_reviews') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'uq_rating_vet_owner' AND table_name = 'rating_and_reviews') THEN
            CREATE UNIQUE INDEX uq_rating_vet_owner ON rating_and_reviews(vet_id, owner_id);
        END IF;
    END IF;
    
    -- Available_Slots: предотвращение дубликатов слотов
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'available_slots') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'uq_slot_vet_datetime' AND table_name = 'available_slots') THEN
            CREATE UNIQUE INDEX uq_slot_vet_datetime ON available_slots(vet_id, date, start_time, end_time);
        END IF;
    END IF;
    
    -- ============================================
    -- CHECK CONSTRAINTS
    -- ============================================
    
    -- Sector
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'sector') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_sector_occupancy_positive' AND table_name = 'sector') THEN
            ALTER TABLE sector 
            ADD CONSTRAINT ck_sector_occupancy_positive CHECK (occupancy >= 0);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_sector_capacity_positive' AND table_name = 'sector') THEN
            ALTER TABLE sector 
            ADD CONSTRAINT ck_sector_capacity_positive CHECK (capacity > 0);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_sector_occupancy_capacity' AND table_name = 'sector') THEN
            ALTER TABLE sector 
            ADD CONSTRAINT ck_sector_occupancy_capacity CHECK (occupancy <= capacity);
        END IF;
    END IF;
    
    -- Pet
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pet') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_pet_age_range' AND table_name = 'pet') THEN
            ALTER TABLE pet 
            ADD CONSTRAINT ck_pet_age_range CHECK (age IS NULL OR (age >= 0 AND age <= 40));
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_pet_weight_range' AND table_name = 'pet') THEN
            ALTER TABLE pet 
            ADD CONSTRAINT ck_pet_weight_range CHECK (weight IS NULL OR (weight >= 0 AND weight <= 120));
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_pet_owner_not_vet' AND table_name = 'pet') THEN
            ALTER TABLE pet 
            ADD CONSTRAINT ck_pet_owner_not_vet CHECK (owner_id IS NULL OR actual_vet_id IS NULL OR owner_id != actual_vet_id);
        END IF;
    END IF;
    
    -- Available_Slots
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'available_slots') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_slot_time_order' AND table_name = 'available_slots') THEN
            ALTER TABLE available_slots 
            ADD CONSTRAINT ck_slot_time_order CHECK (start_time < end_time);
        END IF;
    END IF;
    
    -- Quarantine
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'quarantine') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_quarantine_date_order' AND table_name = 'quarantine') THEN
            ALTER TABLE quarantine 
            ADD CONSTRAINT ck_quarantine_date_order CHECK (start_date < end_date);
        END IF;
    END IF;
    
    -- RatingAndReviews
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'rating_and_reviews') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_rating_range' AND table_name = 'rating_and_reviews') THEN
            ALTER TABLE rating_and_reviews 
            ADD CONSTRAINT ck_rating_range CHECK (rating >= 1 AND rating <= 5);
        END IF;
    END IF;
    
    -- Diagnosis
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'diagnosis') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                       WHERE constraint_name = 'ck_diagnosis_date_past' AND table_name = 'diagnosis') THEN
            ALTER TABLE diagnosis 
            ADD CONSTRAINT ck_diagnosis_date_past CHECK (date IS NULL OR date <= CURRENT_TIMESTAMP);
        END IF;
    END IF;
    
END $$;
