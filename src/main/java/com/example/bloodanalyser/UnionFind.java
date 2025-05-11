package com.example.bloodanalyser;

public class UnionFind {
    private final int[] parent;
    private final int[] size;
    private final int width;
    private final int height;

    public UnionFind(int width, int height){
        this.width = width;
        this.height = height;
        int n = width * height;
        parent = new int[n];
        size = new int[n];

        for(int i = 0; i < n; i++){
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int p){
        while(p != parent[p]){
            parent[p] = parent[parent[p]];
            p = parent[p];
        }
        return p;
    }

    public void union(int p, int q){
        int rootP = find(p);
        int rootQ = find(q);

        if (rootP == rootQ) return;

        if (size[rootP] < size[rootQ]){
            parent[rootP] = rootQ;
            size[rootQ] += size[rootP];
        }else {
            parent[rootQ] = rootP;
            size[rootP] += size[rootQ];
        }
    }

    public int coordToIndex(int x, int y){
        return y * width + x;
    }

    public int getComponentSize(int p){
        return size[find(p)];
    }
}
