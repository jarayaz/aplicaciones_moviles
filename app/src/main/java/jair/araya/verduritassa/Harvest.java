package jair.araya.verduritassa;

public class Harvest {
    private String id;
    private String cropName;
    private String harvestDate;

    public Harvest(String id, String cropName, String harvestDate) {
        this.id = id;
        this.cropName = cropName;
        this.harvestDate = harvestDate;
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
}