package org.example.pojos;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class ArticleContainer {
    @JsonAlias("inventory")
    List<Article> articles;

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public String toString() {
        return "ArticleContainer{" +
                "articles=" + articles +
                '}';
    }
}
