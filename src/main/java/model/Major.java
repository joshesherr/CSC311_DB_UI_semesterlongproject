package model;

public enum Major {
    ANT("Anthropology"), ARA("Arabic"), ARC("Architectural Technology"), ART("Art History"), AIM("Artificial Intelligence Mgmt"), AVN("Aviation"), BIO("Biology"), BUS("Business"), CHM("Chemistry"), CIV("Civil Engineering Technology"), CSC("Computer Science"), CPS("Computer Security Technology"), BCS("Computer Systems"), CON("Construction Management"), CRJ("Criminal Justice"), ECO("Economics"), EET("Electrical Engineering Tech"), EGL("English"), FYE("First Year Experience"), GIS("Geographic Information Systems"), GEO("Geography"), HPW("Health Promotion and Wellness"), HIS("History"), IND("Industrial Technology"), ITA("Italian"), MTH("Mathematics"), MET("Mechanical Engineering Tech"), MLS("Medical Laboratory Science"), MLG("Modern Languages"), NUR("Nursing"), NTR("Nutrition Science"), PHI("Philosophy"), PED("Physical Education"), PHY("Physics and Physical Science"), POL("Politics"), PCM("Professional Communications"), PSY("Psychology"), RAM("Research Aligned Mentorship"), STS("Science, Tech and Society"), SST("Security Systems Technology"), SOC("Sociology"), SPA("Spanish"), SPE("Speech"), SMT("Sport Management"), VIS("Visual Communications");
    private final String name;
    Major(String majorName) {
        this.name = majorName;
    }

    public String getMajorName() {
        return name;
    }

    public boolean equals(String majorName) {
        return this.name.equals(majorName);
    }
}
