package jair.araya.verduritassa;

public class Harvest {
    private String id;
    private String cropName;
    private String harvestDate;
    private String alias;

    public Harvest(String id, String cropName, String harvestDate, String alias) {
        this.id = id;
        this.cropName = cropName;
        this.harvestDate = harvestDate;
        this.alias = alias;
    }

    public String getId() {
        return id;
    }

    public String getCropName() {
        return cropName;
    }

    public String getHarvestDate() {
        return harvestDate;
    }

    public String getAlias() {
        return alias;
    }

    public String getDisplayName() {
        if (alias != null && !alias.isEmpty()) {
            return cropName + " (" + alias + ")";
        }
        return cropName;
    }
}