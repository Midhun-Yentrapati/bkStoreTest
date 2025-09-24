package com.bookverse.bookCatalog.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookImageRequest {
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private Boolean isPrimary = false;
    private String altText;
    private Integer displayOrder = 0;
    
    public BookImageRequest() {}
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}