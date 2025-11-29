package com.rrinnne.intergation.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Spare {
    private String spareCode;
    private String spareName;
    private String spareDescription;
    private String spareType;
    private String spareStatus;
    private BigDecimal price;
    private int quantity;
    private String updatedAt;

    public String getSpareCode() { return spareCode; }
    public void setSpareCode(String spareCode) { this.spareCode = spareCode; }

    public String getSpareName() { return spareName; }
    public void setSpareName(String spareName) { this.spareName = spareName; }

    public String getSpareDescription() { return spareDescription; }
    public void setSpareDescription(String spareDescription) { this.spareDescription = spareDescription; }

    public String getSpareType() { return spareType; }
    public void setSpareType(String spareType) { this.spareType = spareType; }

    public String getSpareStatus() { return spareStatus; }
    public void setSpareStatus(String spareStatus) { this.spareStatus = spareStatus; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}