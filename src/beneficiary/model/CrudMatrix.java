package beneficiary.model;

import java.util.*;

/**
 * CRUD matrix for the Beneficiary Management module (Phase 1 "CRUD look up table").
 * Rows are the module's functional requirements (BEN-01 .. BEN-10), columns are the
 * entities (model classes), and each cell lists which of Create / Read / Update / Delete
 * that requirement performs on that entity in BeneficiaryService.
 */
public final class CrudMatrix {
    public static final String[] ENTITIES = {
            "Beneficiary", "NeedAssessment", "AssistanceRequest", "Resource",
            "ResourceDistribution", "ServiceRecord", "ImpactRecord"
    };

    public static final class Row {
        private final String id, requirement;
        private final Map<String,String> cells;
        private Row(String id, String requirement, Map<String,String> cells) {
            this.id=id; this.requirement=requirement; this.cells=Collections.unmodifiableMap(cells);
        }
        public String getId() { return id; }
        public String getRequirement() { return requirement; }
        /** e.g. "CRU", or "" when the requirement does not touch the entity. */
        public String get(String entity) { return cells.getOrDefault(entity, ""); }
    }

    private static final List<Row> ROWS = new ArrayList<>();
    private static void row(String id, String requirement, String... entityOps) {
        Map<String,String> cells=new LinkedHashMap<>();
        for (String pair:entityOps) {
            String[] p=pair.split("=");
            if (!Arrays.asList(ENTITIES).contains(p[0])) throw new IllegalStateException("Unknown entity " + p[0]);
            cells.put(p[0],p[1]);
        }
        ROWS.add(new Row(id,requirement,cells));
    }
    static {
        row("BEN-01","Register beneficiary",                "Beneficiary=C");
        row("BEN-02","View / search / update / delete profile","Beneficiary=RUD");
        row("BEN-03","Record needs assessment",             "Beneficiary=R","NeedAssessment=CR");
        row("BEN-04","Create, track and cancel requests",   "Beneficiary=R","AssistanceRequest=CRU","Resource=R");
        row("BEN-05","Distribute resources",                "Beneficiary=R","AssistanceRequest=RU","Resource=RU","ResourceDistribution=CR");
        row("BEN-06","Record delivered services",           "Beneficiary=R","AssistanceRequest=RU","ServiceRecord=CR");
        row("BEN-07","View assistance history",             "Beneficiary=R","NeedAssessment=R","AssistanceRequest=R","Resource=R",
                                                            "ResourceDistribution=R","ServiceRecord=R","ImpactRecord=R");
        row("BEN-08","Manage inventory (add, restock, check, delete)","Resource=CRUD");
        row("BEN-09","Monitor program impact",              "Beneficiary=R","NeedAssessment=R","AssistanceRequest=R",
                                                            "ResourceDistribution=R","ServiceRecord=R","ImpactRecord=CR");
        row("BEN-10","Change beneficiary needs status",     "Beneficiary=U");
    }

    private CrudMatrix() { }
    public static List<Row> rows() { return Collections.unmodifiableList(ROWS); }

    /** Why some entities have no D: assistance records are history and are kept for auditing. */
    public static final String NOTES =
            "C = Create, R = Read, U = Update, D = Delete.  "
            + "Beneficiaries and resources can only be deleted when nothing references them. "
            + "Requests are cancelled (status update) instead of deleted; needs, distributions, services "
            + "and impact records are permanent history and are never deleted.";
}
