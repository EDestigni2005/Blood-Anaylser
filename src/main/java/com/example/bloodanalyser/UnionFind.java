package com.example.bloodanalyser;

public class UnionFind {
    private final int[] parent;
    private final int[] rank;
    private final int width;
    private final int height;

    public UnionFind(int width, int height){
        this.width = width;
        this.height = height;
        int n = width * height;
        parent = new int[n];
        rank = new int[n];

        for(int i = 0; i < n; i++){
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int p){
        if (p != parent[p])
            parent[p] = find(parent[p]);
        return parent[p];
    }

    public void union(int p, int q){
        int rootP = find(p);
        int rootQ = find(q);

        if (rootP == rootQ) return;

        if (rank[rootP] < rank[rootQ]){
            parent[rootP] = rootQ;
        } else {
            parent[rootQ] = rootP;
            if (rank[rootP] == rank[rootQ])
                rank[rootP]++;
        }
    }

    public int coordToIndex(int x, int y){
        return y * width + x;
    }

    public boolean connected(int p, int q) {
        return find(p) == find(q);
    }
}