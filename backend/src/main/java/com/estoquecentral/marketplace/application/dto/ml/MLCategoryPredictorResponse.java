package com.estoquecentral.marketplace.application.dto.ml;

import java.util.List;

/**
 * DTO for Mercado Livre category predictor response
 * Story 5.3: Publish Products to Mercado Livre - AC2
 *
 * API: GET /sites/MLB/category_predictor/predict?title={title}
 */
public class MLCategoryPredictorResponse {

    private String id;
    private String name;
    private List<PathFromRoot> pathFromRoot;

    public MLCategoryPredictorResponse() {
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PathFromRoot> getPathFromRoot() {
        return pathFromRoot;
    }

    public void setPathFromRoot(List<PathFromRoot> pathFromRoot) {
        this.pathFromRoot = pathFromRoot;
    }

    /**
     * PathFromRoot - Category hierarchy path
     */
    public static class PathFromRoot {
        private String id;
        private String name;

        public PathFromRoot() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
