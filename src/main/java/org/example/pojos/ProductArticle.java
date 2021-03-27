package org.example.pojos;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ProductArticle {
    @JsonAlias("prod_id")
    private int prodId;

    @JsonAlias("art_id")
    private int artId;

    @JsonAlias("amount_of")
    private int artQuantity;

    public int getProdId() {
        return prodId;
    }

    public void setProdId(int prodId) {
        this.prodId = prodId;
    }

    public int getArtId() {
        return artId;
    }

    public void setArtId(int artId) {
        this.artId = artId;
    }

    public int getArtQuantity() {
        return artQuantity;
    }

    public void setArtQuantity(int artQuantity) {
        this.artQuantity = artQuantity;
    }

    @Override
    public String toString() {
        return "ProductArticle{" +
                "prodId=" + prodId +
                ", artId=" + artId +
                ", artQuantity=" + artQuantity +
                '}';
    }
}
