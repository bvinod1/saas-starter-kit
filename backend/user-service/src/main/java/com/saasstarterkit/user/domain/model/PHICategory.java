package com.saasstarterkit.user.domain.model;

/**
 * Categories of Protected Health Information (PHI) for HIPAA compliance
 */
public enum PHICategory {
    /**
     * Patient demographics and identifiers
     */
    PATIENT_DEMOGRAPHICS("Patient Demographics", "Name, address, phone, email, SSN, etc."),
    
    /**
     * Medical history and diagnoses
     */
    MEDICAL_HISTORY("Medical History", "Past medical conditions, diagnoses, treatments"),
    
    /**
     * Current medical conditions
     */
    CURRENT_CONDITIONS("Current Conditions", "Active medical conditions and status"),
    
    /**
     * Treatment plans and procedures
     */
    TREATMENT_PLANS("Treatment Plans", "Current and planned medical treatments"),
    
    /**
     * Medication information
     */
    MEDICATIONS("Medications", "Prescribed medications, dosages, schedules"),
    
    /**
     * Laboratory test results
     */
    LAB_RESULTS("Laboratory Results", "Blood tests, urine tests, imaging results"),
    
    /**
     * Vital signs and measurements
     */
    VITAL_SIGNS("Vital Signs", "Blood pressure, heart rate, temperature, weight"),
    
    /**
     * Mental health information
     */
    MENTAL_HEALTH("Mental Health", "Psychological assessments, therapy notes"),
    
    /**
     * Substance abuse information
     */
    SUBSTANCE_ABUSE("Substance Abuse", "Drug and alcohol use, treatment history"),
    
    /**
     * Reproductive health information
     */
    REPRODUCTIVE_HEALTH("Reproductive Health", "Pregnancy, fertility, sexual health"),
    
    /**
     * Genetic information
     */
    GENETIC_INFORMATION("Genetic Information", "DNA tests, family medical history"),
    
    /**
     * Immunization records
     */
    IMMUNIZATIONS("Immunizations", "Vaccination history and schedules"),
    
    /**
     * Allergies and adverse reactions
     */
    ALLERGIES("Allergies", "Drug allergies, food allergies, reactions"),
    
    /**
     * Insurance and billing information
     */
    INSURANCE_BILLING("Insurance and Billing", "Health insurance, payment information"),
    
    /**
     * Emergency contact information
     */
    EMERGENCY_CONTACTS("Emergency Contacts", "Emergency contact details"),
    
    /**
     * Advance directives
     */
    ADVANCE_DIRECTIVES("Advance Directives", "Living wills, power of attorney"),
    
    /**
     * Clinical notes and observations
     */
    CLINICAL_NOTES("Clinical Notes", "Physician notes, observations, assessments"),
    
    /**
     * Research participation
     */
    RESEARCH_PARTICIPATION("Research Participation", "Clinical trial participation, research data"),
    
    /**
     * Quality improvement data
     */
    QUALITY_IMPROVEMENT("Quality Improvement", "Data used for healthcare quality improvement"),
    
    /**
     * Public health reporting
     */
    PUBLIC_HEALTH("Public Health", "Data reported to public health authorities"),
    
    /**
     * Legal and compliance data
     */
    LEGAL_COMPLIANCE("Legal and Compliance", "Data required for legal or regulatory purposes");
    
    private final String displayName;
    private final String description;
    
    PHICategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    /**
     * Check if this category is considered high-risk
     */
    public boolean isHighRisk() {
        return this == MENTAL_HEALTH || this == SUBSTANCE_ABUSE || 
               this == REPRODUCTIVE_HEALTH || this == GENETIC_INFORMATION;
    }
    
    /**
     * Check if this category requires special handling
     */
    public boolean requiresSpecialHandling() {
        return this == GENETIC_INFORMATION || this == MENTAL_HEALTH || 
               this == SUBSTANCE_ABUSE || this == REPRODUCTIVE_HEALTH;
    }
    
    /**
     * Check if this category can be shared for treatment purposes
     */
    public boolean canBeSharedForTreatment() {
        return this != GENETIC_INFORMATION; // Genetic info has stricter sharing rules
    }
    
    /**
     * Check if this category requires explicit consent
     */
    public boolean requiresExplicitConsent() {
        return this == GENETIC_INFORMATION || this == MENTAL_HEALTH || 
               this == SUBSTANCE_ABUSE || this == REPRODUCTIVE_HEALTH;
    }
}
