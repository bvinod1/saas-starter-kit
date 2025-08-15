package com.saasstarterkit.organisation.dto;

public class OnboardingCompleteRequest {
    private String settings;
    private String onboardingStep;

    // Constructors
    public OnboardingCompleteRequest() {}

    public OnboardingCompleteRequest(String settings, String onboardingStep) {
        this.settings = settings;
        this.onboardingStep = onboardingStep;
    }

    // Getters and Setters
    public String getSettings() { return settings; }
    public void setSettings(String settings) { this.settings = settings; }

    public String getOnboardingStep() { return onboardingStep; }
    public void setOnboardingStep(String onboardingStep) { this.onboardingStep = onboardingStep; }

    @Override
    public String toString() {
        return "OnboardingCompleteRequest{" +
                "settings='" + settings + '\'' +
                ", onboardingStep='" + onboardingStep + '\'' +
                '}';
    }
}
