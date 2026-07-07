package com.example.animalvoiceprint.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioFeatureExtractorTest {

    private AudioFeatureExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new AudioFeatureExtractor();
    }

    @Test
    void testExtractMFCC_NonExistentFile() {
        double[] features = extractor.extractMFCC("/non/existent/path.wav");
        assertNull(features);
    }

    @Test
    void testEuclideanDistance() {
        double[] features1 = {1.0, 2.0, 3.0};
        double[] features2 = {4.0, 5.0, 6.0};
        
        double distance = extractor.computeEuclideanDistance(features1, features2);
        
        assertEquals(Math.sqrt(27), distance, 0.0001);
    }

    @Test
    void testEuclideanDistance_NullInput() {
        double distance = extractor.computeEuclideanDistance(null, new double[]{1.0, 2.0});
        assertEquals(Double.MAX_VALUE, distance);
        
        distance = extractor.computeEuclideanDistance(new double[]{1.0, 2.0}, null);
        assertEquals(Double.MAX_VALUE, distance);
        
        distance = extractor.computeEuclideanDistance(new double[]{1.0, 2.0}, new double[]{1.0});
        assertEquals(Double.MAX_VALUE, distance);
    }

    @Test
    void testCosineSimilarity() {
        double[] features1 = {1.0, 0.0};
        double[] features2 = {0.0, 1.0};
        
        double similarity = extractor.computeCosineSimilarity(features1, features2);
        
        assertEquals(0.0, similarity, 0.0001);
    }

    @Test
    void testCosineSimilarity_SameVector() {
        double[] features1 = {1.0, 2.0, 3.0};
        double[] features2 = {1.0, 2.0, 3.0};
        
        double similarity = extractor.computeCosineSimilarity(features1, features2);
        
        assertEquals(1.0, similarity, 0.0001);
    }

    @Test
    void testFitNormalization() {
        double[] features1 = {1.0, 2.0, 3.0};
        double[] features2 = {3.0, 4.0, 5.0};
        
        extractor.fitNormalization(java.util.List.of(features1, features2));
        
        assertTrue(extractor.isFeaturesNormalized());
    }

    @Test
    void testFitNormalization_EmptyList() {
        extractor.fitNormalization(java.util.List.of());
        
        assertFalse(extractor.isFeaturesNormalized());
    }

    @Test
    void testNormalize() {
        double[] features1 = {0.0, 0.0, 0.0};
        double[] features2 = {2.0, 2.0, 2.0};
        
        extractor.fitNormalization(java.util.List.of(features1, features2));
        
        double[] normalized = extractor.normalize(new double[]{1.0, 1.0, 1.0});
        
        assertEquals(3, normalized.length);
        assertEquals(0.0, normalized[0], 0.0001);
    }

    @Test
    void testFFT_Simple() {
        int n = 4;
        double[] real = new double[n];
        double[] imag = new double[n];
        
        real[0] = 1.0;
        real[1] = 1.0;
        real[2] = 1.0;
        real[3] = 1.0;
        
        extractor.fft(real, imag);
        
        assertEquals(4.0, real[0], 0.0001);
        assertEquals(0.0, imag[0], 0.0001);
    }
}