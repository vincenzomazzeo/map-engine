
package it.alidays.mapengine.test.bm.generated;

import java.math.BigDecimal;
import java.util.Map;


/**
 * Auto generated class. Do not modify!
 * 
 */
public class CatalogMap {

    private final String description;
    private final String productImage;
    private final String gender;
    private final String itemNumber;
    private final BigDecimal price;
    private final String itemSizeDescription;
    private final String image;
    private final String color;

    protected CatalogMap(Map<String, Object> data) {
        this.description = ((String) data.get("Description"));
        this.productImage = ((String) data.get("ProductImage"));
        this.gender = ((String) data.get("Gender"));
        this.itemNumber = ((String) data.get("ItemNumber"));
        this.price = ((BigDecimal) data.get("Price"));
        this.itemSizeDescription = ((String) data.get("ItemSizeDescription"));
        this.image = ((String) data.get("Image"));
        this.color = ((String) data.get("Color"));
    }

    public String getDescription() {
        return this.description;
    }

    public String getProductImage() {
        return this.productImage;
    }

    public String getGender() {
        return this.gender;
    }

    public String getItemNumber() {
        return this.itemNumber;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public String getItemSizeDescription() {
        return this.itemSizeDescription;
    }

    public String getImage() {
        return this.image;
    }

    public String getColor() {
        return this.color;
    }

}
