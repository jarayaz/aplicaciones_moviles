package jair.araya.verduritassa;

public class Harvest {
    private String cropName;
    private String harvestDate;

    public Harvest(String cropName, String harvestDate) {
        this.cropName = cropName;
        this.harvestDate = harvestDate;
    }

    public String getCropName() {
        return cropName;
    }

    public String getHarvestDate() {
        return harvestDate;
    }
}