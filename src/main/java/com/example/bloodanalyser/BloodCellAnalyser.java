package com.example.bloodanalyser;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class BloodCellAnalyser {
    private final int minCellSize;
    private final int maxCellSize;
    private final boolean showNumbering;

    public BloodCellAnalyser(int minCellSize, int maxCellSize, boolean showNumbering) {
        this.minCellSize = minCellSize;
        this.maxCellSize = maxCellSize;
        this.showNumbering = showNumbering;
    }

    public AnalysisResult analyseImage(WritableImage tricolourImage) {
        int width = (int) tricolourImage.getWidth();
        int height = (int) tricolourImage.getHeight();

        UnionFind unionFind = new UnionFind(width, height);
        PixelReader pixelReader = tricolourImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);

                if (isWhite(color)) continue;

                int currentIndex = y * width + x;

                if (x + 1 < width) {
                    Color rightColor = pixelReader.getColor(x + 1, y);
                    if (isSameType(color, rightColor)) {
                        unionFind.union(currentIndex, (y * width) + (x + 1));
                    }
                }

                if (y + 1 < height) {
                    Color bottomColor = pixelReader.getColor(x, y + 1);
                    if (isSameType(color, bottomColor)) {
                        unionFind.union(currentIndex, ((y + 1) * width) + x);
                    }
                }
            }
        }

        Map<Integer, CellInfo> cellMap = new HashMap<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);

                if (isWhite(color)) continue;

                int index = y * width + x;
                int root  = unionFind.find(index);
                CellInfo ci = cellMap.get(root);
                if (ci == null) {
                    ci = new CellInfo();
                    ci.color = isPurple(color) ? CellType.WHITE : CellType.RED;
                    ci.minX = ci.maxX = x;
                    ci.minY = ci.maxY = y;
                    ci.size = 0;
                    cellMap.put(root, ci);
                }
                ci.size++;
                ci.minX = Math.min(ci.minX, x);
                ci.maxX = Math.max(ci.maxX, x);
                ci.minY = Math.min(ci.minY, y);
                ci.maxY = Math.max(ci.maxY, y);

            }
        }

        List<CellInfo> validCells = cellMap.values().stream()
                .filter(cell -> cell.size >= minCellSize && cell.size <= maxCellSize)
                .sorted(Comparator.comparingInt(c -> c.minX + c.minY * width))
                .toList();

        int redCellCount = 0;
        int whiteCellCount = 0;

        int id = 1;
        for (CellInfo cell : validCells) {
            cell.id = id++;

            int width1 = cell.maxX - cell.minX + 1;
            int height1 = cell.maxY - cell.minY + 1;
            double aspectRatio = (double) width1 / height1;

            if (cell.color == CellType.RED) {
                if (cell.size > 500 || (aspectRatio < 0.7 || aspectRatio > 1.3) || (width1 > 30 || height1 > 30)) {
                    cell.isCluster = true;
                    int estCount = Math.max(2, cell.size / 300);
                    cell.estimatedCellCount = estCount;
                    redCellCount += estCount;
                } else {
                    cell.estimatedCellCount = 1;
                    redCellCount++;
                }
            } else {
                cell.estimatedCellCount = 1;
                whiteCellCount++;
            }
        }

        return new AnalysisResult(validCells, redCellCount, whiteCellCount);
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
            drawCellRectangle(pixelWriter, cell, width, height);

            if (showNumbering) {
                int centerX = cell.minX + (cell.maxX - cell.minX) / 2;
                int centerY = cell.minY + (cell.maxY - cell.minY) / 2;
                drawCellNumber(pixelWriter, cell.id, centerX, centerY, width, height);
            }
        }

        return result;
    }

    private void drawCellRectangle(PixelWriter pixelWriter, CellInfo cell, int imageWidth, int imageHeight) {
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

    private void drawCellNumber(PixelWriter pixelWriter, int number, int x, int y, int imageWidth, int imageHeight) {
        String text = String.valueOf(number);
        boolean[][][] digitPatterns = {
                // Digit 0
                {
                        {true, true, true},
                        {true, false, true},
                        {true, false, true},
                        {true, false, true},
                        {true, true, true}
                },
                // Digit 1
                {
                        {false, true, false},
                        {true, true, false},
                        {false, true, false},
                        {false, true, false},
                        {true, true, true}
                },
                // Digit 2
                {
                        {true, true, true},
                        {false, false, true},
                        {true, true, true},
                        {true, false, false},
                        {true, true, true}
                },
                // Digit 3
                {
                        {true, true, true},
                        {false, false, true},
                        {true, true, true},
                        {false, false, true},
                        {true, true, true}
                },
                // Digit 4
                {
                        {true, false, true},
                        {true, false, true},
                        {true, true, true},
                        {false, false, true},
                        {false, false, true}
                },
                // Digit 5
                {
                        {true, true, true},
                        {true, false, false},
                        {true, true, true},
                        {false, false, true},
                        {true, true, true}
                },
                // Digit 6
                {
                        {true, true, true},
                        {true, false, false},
                        {true, true, true},
                        {true, false, true},
                        {true, true, true}
                },
                // Digit 7
                {
                        {true, true, true},
                        {false, false, true},
                        {false, true, false},
                        {true, false, false},
                        {true, false, false}
                },
                // Digit 8
                {
                        {true, true, true},
                        {true, false, true},
                        {true, true, true},
                        {true, false, true},
                        {true, true, true}
                },
                // Digit 9
                {
                        {true, true, true},
                        {true, false, true},
                        {true, true, true},
                        {false, false, true},
                        {true, true, true}
                }
        };

        int digitWidth = 3;
        int digitHeight = 5;
        int spacing = 1;
        int totalWidth = text.length() * (digitWidth + spacing) - spacing;

        int startX = x - totalWidth / 2;
        int startY = y - digitHeight / 2;

        for (int bgX = startX - 1; bgX <= startX + totalWidth; bgX++) {
            for (int bgY = startY - 1; bgY <= startY + digitHeight; bgY++) {
                if (bgX >= 0 && bgX < imageWidth && bgY >= 0 && bgY < imageHeight) {
                    pixelWriter.setColor(bgX, bgY, Color.BLACK);
                }
            }
        }

        for (int i = 0; i < text.length(); i++) {
            int digit = Character.getNumericValue(text.charAt(i));
            if (digit >= 0 && digit <= 9) {
                int digitX = startX + i * (digitWidth + spacing);

                for (int dy = 0; dy < digitHeight; dy++) {
                    for (int dx = 0; dx < digitWidth; dx++) {
                        int px = digitX + dx;
                        int py = startY + dy;

                        if (px >= 0 && px < imageWidth && py >= 0 && py < imageHeight) {
                            if (digitPatterns[digit][dy][dx]) {
                                pixelWriter.setColor(px, py, Color.WHITE);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isWhite(Color color) {
        return color.getRed() > 0.9 && color.getGreen() > 0.9 && color.getBlue() > 0.9;
    }

    private boolean isPurple(Color color) {
        return color.getBlue() > 0.5 && color.getRed() < 0.5;
    }

    private boolean isSameType(Color c1, Color c2) {
        if (isWhite(c1) || isWhite(c2)) return false;
        return isPurple(c1) == isPurple(c2);
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

        @Override
        public String toString() {
            return String.format("Cell %d: %s, size=%d, pos=(%d,%d)-(%d,%d), isCluster=%s, count=%d",
                    id, color, size, minX, minY, maxX, maxY, isCluster, estimatedCellCount);
        }
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