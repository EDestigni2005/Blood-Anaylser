package com.example.bloodanalyser;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.*;

public class BloodCellAnalyser {
    private final int minCellSize;
    private final int maxCellSize;
    private final boolean showNumbering;

    private UnionFind unionFind;
    private Map<Integer, CellInfo> cellComponents;

    public BloodCellAnalyser(int minCellSize, int maxCellSize, boolean showNumbering) {
        this.minCellSize = minCellSize;
        this.maxCellSize = maxCellSize;
        this.showNumbering = showNumbering;
    }

    public AnalysisResult analyseImage(WritableImage tricolourImage) {
        int width = (int) tricolourImage.getWidth();
        int height = (int) tricolourImage.getHeight();

        unionFind = new UnionFind(width, height);
        cellComponents = new HashMap<>();

        PixelReader pixelReader = tricolourImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color currentColor = pixelReader.getColor(x, y);

                if (isWhite(currentColor)) continue;

                int currentIndex = unionFind.coordToIndex(x, y);

                if (x + 1 < width) {
                    Color rightColor = pixelReader.getColor(x + 1, y);
                    if (isSameColor(currentColor, rightColor)) {
                        unionFind.union(currentIndex, unionFind.coordToIndex(x + 1, y));
                    }
                }

                if (y + 1 < height) {
                    Color bottomColor = pixelReader.getColor(x, y + 1);
                    if (isSameColor(currentColor, bottomColor)) {
                        unionFind.union(currentIndex, unionFind.coordToIndex(x, y + 1));
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);

                if (isWhite(color)) continue;

                int index = unionFind.coordToIndex(x, y);
                int root = unionFind.find(index);

                if (!cellComponents.containsKey(root)) {
                    CellInfo cellInfo = new CellInfo();
                    cellInfo.color = isRed(color) ? CellType.RED : CellType.WHITE;
                    cellInfo.minX = cellInfo.maxX = x;
                    cellInfo.minY = cellInfo.maxY = y;
                    cellInfo.size = 1;
                    cellComponents.put(root, cellInfo);
                } else {
                    CellInfo cellInfo = cellComponents.get(root);
                    cellInfo.minX = Math.min(cellInfo.minX, x);
                    cellInfo.maxX = Math.max(cellInfo.maxX, x);
                    cellInfo.minY = Math.min(cellInfo.minY, y);
                    cellInfo.maxY = Math.max(cellInfo.maxY, y);
                    cellInfo.size++;
                }
            }
        }

        List<CellInfo> validCells = cellComponents.values().stream()
                .filter(cell -> cell.size >= minCellSize && cell.size <= maxCellSize)
                .toList();

        int redCellsCount = 0;
        int redClustersCount = 0;
        int whiteCellsCount = 0;

        List<CellInfo> sortedCells = new ArrayList<>(validCells);
        sortedCells.sort(Comparator.comparingInt(c -> c.minX + c.minY * width));

        int cellNumber = 1;
        for (CellInfo cell : sortedCells) {
            cell.id = cellNumber++;

            int boxWidth = cell.maxX - cell.minX + 1;
            int boxHeight = cell.maxY - cell.minY + 1;

            if (cell.color == CellType.RED) {
                if (cell.size > 500 || (boxWidth > 25 && boxHeight > 25)) {
                    cell.isCluster = true;
                    redClustersCount++;
                    cell.estimatedCellCount = Math.max(2, cell.size / 250);
                    redCellsCount += cell.estimatedCellCount;
                } else {
                    redCellsCount++;
                    cell.estimatedCellCount = 1;
                }
            } else if (cell.color == CellType.WHITE) {
                whiteCellsCount++;
                cell.estimatedCellCount = 1;
            }
        }

        return new AnalysisResult(sortedCells, redCellsCount, whiteCellsCount);
    }

    public WritableImage createAnalysisImage(Image originalImage, List<CellInfo> cells) {
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        WritableImage result = new WritableImage(width, height);
        PixelReader pixelReader = originalImage.getPixelReader();
        PixelWriter pixelWriter = result.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
            }
        }

        for (CellInfo cell : cells) {
            drawRectangle(pixelWriter, cell, width, height);

            if (showNumbering) {
                int labelX = cell.minX + (cell.maxX - cell.minX) / 2;
                int labelY = cell.minY + (cell.maxY - cell.minY) / 2;

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        drawText(pixelWriter, String.valueOf(cell.id), labelX + dx, labelY + dy, Color.BLACK);
                    }
                }
                drawText(pixelWriter, String.valueOf(cell.id), labelX, labelY, Color.WHITE);
            }
        }

        return result;
    }

    private void drawText(PixelWriter pixelWriter, String text, int x, int y, Color color) {
        for (int i = 0; i < text.length(); i++) {
            pixelWriter.setColor(x + i * 7, y, color);
            pixelWriter.setColor(x + i * 7, y + 1, color);
        }
    }

    private void drawRectangle(PixelWriter pixelWriter, CellInfo cell, int imageWidth, int imageHeight) {
        Color color;
        if (cell.color == CellType.RED) {
            color = cell.isCluster ? Color.BLUE : Color.GREEN;
        } else {
            color = Color.PURPLE;
        }

        int minX = Math.max(0, cell.minX - 1);
        int minY = Math.max(0, cell.minY - 1);
        int maxX = Math.min(imageWidth - 1, cell.maxX + 1);
        int maxY = Math.min(imageHeight - 1, cell.maxY + 1);

        for (int x = minX; x <= maxX; x++) {
            pixelWriter.setColor(x, minY, color);
            pixelWriter.setColor(x, maxY, color);
        }

        for (int y = minY; y <= maxY; y++) {
            pixelWriter.setColor(minX, y, color);
            pixelWriter.setColor(maxX, y, color);
        }
    }

    private boolean isWhite(Color color) {
        return color.getRed() > 0.9 && color.getGreen() > 0.9 && color.getBlue() > 0.9;
    }

    private boolean isRed(Color color) {
        return color.getRed() > 0.7 && color.getGreen() < 0.3 && color.getBlue() < 0.3;
    }

    private boolean isSameColor(Color c1, Color c2) {
        return (isRed(c1) && isRed(c2)) || (!isRed(c1) && !isRed(c2) && !isWhite(c2));
    }

    public enum CellType {
        RED, WHITE
    }

    public static class CellInfo {
        CellType color;
        int minX, minY, maxX, maxY;
        int size;
        int id;
        boolean isCluster = false;
        int estimatedCellCount = 1;
    }

    public static class AnalysisResult {
        private final List<CellInfo> cells;
        private final int redCellCount;
        private final int whiteCellCount;

        public AnalysisResult(List<CellInfo> cells, int redCellCount, int whiteCellCount) {
            this.cells = cells;
            this.redCellCount = redCellCount;
            this.whiteCellCount = whiteCellCount;
        }

        public List<CellInfo> getCells() {
            return cells;
        }

        public int getRedCellCount() {
            return redCellCount;
        }

        public int getWhiteCellCount() {
            return whiteCellCount;
        }

        public int getTotalCellCount() {
            return redCellCount + whiteCellCount;
        }
    }
}