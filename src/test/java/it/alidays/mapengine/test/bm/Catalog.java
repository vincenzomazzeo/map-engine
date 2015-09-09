package it.alidays.mapengine.test.bm;

import java.math.BigDecimal;

public class Catalog {

	private String description;
	private String image;
	private String gender;
	private String itemNumber;
	private BigDecimal price;
	private String itemSizeDescription;
	private String itemSizeColorImage;
	private String itemSizeColorColor;

	@Override
	public String toString() {
		return "[description=" + this.description + ", image=" + this.image + ", gender=" + this.gender + ", itemNumber=" + this.itemNumber + ", price=" + this.price + ", itemSizeDescription=" + this.itemSizeDescription + ", itemSizeColorImage=" + this.itemSizeColorImage + ", itemSizeColorColor=" + this.itemSizeColorColor + "]";
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getGender() {
		return this.gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getItemNumber() {
		return this.itemNumber;
	}

	public void setItemNumber(String itemNumber) {
		this.itemNumber = itemNumber;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getItemSizeDescription() {
		return this.itemSizeDescription;
	}

	public void setItemSizeDescription(String itemSizeDescription) {
		this.itemSizeDescription = itemSizeDescription;
	}

	public String getItemSizeColorImage() {
		return this.itemSizeColorImage;
	}

	public void setItemSizeColorImage(String itemSizeColorImage) {
		this.itemSizeColorImage = itemSizeColorImage;
	}

	public String getItemSizeColorColor() {
		return this.itemSizeColorColor;
	}

	public void setItemSizeColorColor(String itemSizeColorColor) {
		this.itemSizeColorColor = itemSizeColorColor;
	}

}
