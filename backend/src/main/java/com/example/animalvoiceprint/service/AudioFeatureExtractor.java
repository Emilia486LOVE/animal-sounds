package com.example.animalvoiceprint.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AudioFeatureExtractor {

    private static final Logger logger = LoggerFactory.getLogger(AudioFeatureExtractor.class);

    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 1024;
    private static final int MFCC_COEFFICIENTS = 13;
    private static final int MEL_FILTERS = 40;
    private static final int FRAME_SHIFT = 512;
    private static final double PRE_EMPHASIS_ALPHA = 0.9375;
    private static final double VAD_THRESHOLD = 0.005;
    private static final int VAD_FRAMES = 5;

    double[] featureMean = null;
    double[] featureStd = null;
    boolean featuresNormalized = false;

    public double[] extractFeatures(String audioFilePath) {
        File audioFile = new File(audioFilePath);
        if (!audioFile.exists()) {
            logger.error("音频文件不存在: {}", audioFilePath);
            return null;
        }

        List<double[]> mfccFrames = new ArrayList<>();

        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile)) {
            AudioFormat format = audioInputStream.getFormat();
            float sampleRate = format.getSampleRate();
            int channels = format.getChannels();
            int bytesPerSample = format.getSampleSizeInBits() / 8;

            List<Double> allSamples = new ArrayList<>();
            byte[] buffer = new byte[BUFFER_SIZE * channels * bytesPerSample];
            int bytesRead;

            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                double[] samples = convertToMono(buffer, bytesRead, format);
                if (sampleRate != SAMPLE_RATE) {
                    samples = resample(samples, sampleRate, SAMPLE_RATE);
                }
                for (double s : samples) {
                    allSamples.add(s);
                }
            }

            double[] sampleArray = allSamples.stream().mapToDouble(Double::doubleValue).toArray();

            sampleArray = preEmphasis(sampleArray);
            sampleArray = normalizeAudio(sampleArray);

            for (int i = 0; i + BUFFER_SIZE <= sampleArray.length; i += FRAME_SHIFT) {
                double[] frame = new double[BUFFER_SIZE];
                System.arraycopy(sampleArray, i, frame, 0, BUFFER_SIZE);
                double[] mfccValues = computeMFCC(frame);
                if (mfccValues != null && mfccValues.length > 0) {
                    mfccFrames.add(mfccValues);
                }
            }

        } catch (UnsupportedAudioFileException | IOException e) {
            logger.error("提取特征失败: {}", e.getMessage(), e);
            return null;
        }

        if (mfccFrames.isEmpty()) {
            logger.warn("未提取到任何MFCC帧");
            return null;
        }

        return extractCombinedFeatures(mfccFrames);
    }

    private double[] preEmphasis(double[] samples) {
        double[] result = new double[samples.length];
        result[0] = samples[0];
        for (int i = 1; i < samples.length; i++) {
            result[i] = samples[i] - PRE_EMPHASIS_ALPHA * samples[i - 1];
        }
        return result;
    }

    private double[] normalizeAudio(double[] samples) {
        double max = 0;
        for (double s : samples) {
            if (Math.abs(s) > max) {
                max = Math.abs(s);
            }
        }
        if (max < 1e-10) {
            return samples;
        }
        double[] result = new double[samples.length];
        for (int i = 0; i < samples.length; i++) {
            result[i] = samples[i] / max;
        }
        return result;
    }

    private double[] applyVAD(double[] samples) {
        List<Double> result = new ArrayList<>();
        int frameSize = BUFFER_SIZE;
        int stepSize = FRAME_SHIFT;

        for (int i = 0; i + frameSize <= samples.length; i += stepSize) {
            double energy = 0;
            for (int j = i; j < i + frameSize; j++) {
                energy += samples[j] * samples[j];
            }
            energy /= frameSize;

            if (energy > VAD_THRESHOLD) {
                for (int j = i; j < Math.min(i + frameSize, samples.length); j++) {
                    result.add(samples[j]);
                }
            }
        }

        return result.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private double[] extractCombinedFeatures(List<double[]> mfccFrames) {
        int numFrames = mfccFrames.size();
        int numCoeffs = mfccFrames.get(0).length;

        double[] mean = new double[numCoeffs];
        double[] variance = new double[numCoeffs];
        double[] std = new double[numCoeffs];
        double[] min = new double[numCoeffs];
        double[] max = new double[numCoeffs];
        double[] median = new double[numCoeffs];
        double[] skewness = new double[numCoeffs];
        double[] kurtosis = new double[numCoeffs];

        for (int i = 0; i < numCoeffs; i++) {
            min[i] = Double.MAX_VALUE;
            max[i] = Double.MIN_VALUE;
        }

        for (double[] frame : mfccFrames) {
            for (int i = 0; i < numCoeffs; i++) {
                mean[i] += frame[i];
                if (frame[i] < min[i]) min[i] = frame[i];
                if (frame[i] > max[i]) max[i] = frame[i];
            }
        }

        for (int i = 0; i < numCoeffs; i++) {
            mean[i] /= numFrames;
        }

        for (double[] frame : mfccFrames) {
            for (int i = 0; i < numCoeffs; i++) {
                double diff = frame[i] - mean[i];
                variance[i] += diff * diff;
            }
        }

        for (int i = 0; i < numCoeffs; i++) {
            variance[i] /= numFrames;
            std[i] = Math.sqrt(variance[i]);
        }

        for (int i = 0; i < numCoeffs; i++) {
            double[] sorted = new double[numFrames];
            for (int j = 0; j < numFrames; j++) {
                sorted[j] = mfccFrames.get(j)[i];
            }
            java.util.Arrays.sort(sorted);
            if (numFrames % 2 == 0) {
                median[i] = (sorted[numFrames / 2 - 1] + sorted[numFrames / 2]) / 2.0;
            } else {
                median[i] = sorted[numFrames / 2];
            }
        }

        for (double[] frame : mfccFrames) {
            for (int i = 0; i < numCoeffs; i++) {
                double diff = frame[i] - mean[i];
                double stdVal = std[i] > 1e-10 ? std[i] : 1;
                skewness[i] += Math.pow(diff / stdVal, 3);
                kurtosis[i] += Math.pow(diff / stdVal, 4);
            }
        }

        for (int i = 0; i < numCoeffs; i++) {
            skewness[i] /= numFrames;
            kurtosis[i] /= numFrames;
        }

        double[] deltaMfcc = computeDelta(mfccFrames);
        double[] deltaMean = computeMean(deltaMfcc);
        double[] deltaStd = computeStd(deltaMfcc, deltaMean);

        double[] zcr = computeZCR(mfccFrames);
        double[] spectralCentroid = computeSpectralCentroid(mfccFrames);
        double[] spectralFeatures = computeSpectralFeatures(mfccFrames);

        int totalFeatures = numCoeffs * 8 + zcr.length + spectralCentroid.length + 2 * numCoeffs + spectralFeatures.length;
        double[] result = new double[totalFeatures];
        int idx = 0;

        for (double v : mean) result[idx++] = v;
        for (double v : variance) result[idx++] = v;
        for (double v : std) result[idx++] = v;
        for (double v : min) result[idx++] = v;
        for (double v : max) result[idx++] = v;
        for (double v : median) result[idx++] = v;
        for (double v : skewness) result[idx++] = v;
        for (double v : kurtosis) result[idx++] = v;
        for (double v : deltaMean) result[idx++] = v;
        for (double v : deltaStd) result[idx++] = v;
        for (double v : zcr) result[idx++] = v;
        for (double v : spectralCentroid) result[idx++] = v;
        for (double v : spectralFeatures) result[idx++] = v;

        return result;
    }

    private double[] computeDelta(List<double[]> mfccFrames) {
        int numFrames = mfccFrames.size();
        int numCoeffs = mfccFrames.get(0).length;
        double[] delta = new double[numCoeffs];

        for (int i = 0; i < numCoeffs; i++) {
            double sum = 0;
            int count = 0;
            for (int t = 1; t < numFrames - 1; t++) {
                sum += mfccFrames.get(t + 1)[i] - mfccFrames.get(t - 1)[i];
                count++;
            }
            delta[i] = count > 0 ? sum / (2 * count) : 0;
        }
        return delta;
    }

    private double[] computeMean(double[] values) {
        return new double[]{java.util.Arrays.stream(values).average().orElse(0)};
    }

    private double[] computeStd(double[] values, double[] mean) {
        double meanVal = mean[0];
        double variance = java.util.Arrays.stream(values).map(v -> Math.pow(v - meanVal, 2)).average().orElse(0);
        return new double[]{Math.sqrt(variance)};
    }

    private double[] computeZCR(List<double[]> mfccFrames) {
        double[] zcr = new double[mfccFrames.get(0).length];
        for (int i = 0; i < mfccFrames.get(0).length; i++) {
            int crossings = 0;
            for (int j = 1; j < mfccFrames.size(); j++) {
                if (mfccFrames.get(j - 1)[i] * mfccFrames.get(j)[i] < 0) {
                    crossings++;
                }
            }
            zcr[i] = (double) crossings / (mfccFrames.size() - 1);
        }
        return zcr;
    }

    private double[] computeSpectralCentroid(List<double[]> mfccFrames) {
        double[] centroid = new double[mfccFrames.size()];
        for (int i = 0; i < mfccFrames.size(); i++) {
            double[] frame = mfccFrames.get(i);
            double sum = 0;
            double weightedSum = 0;
            for (int j = 0; j < frame.length; j++) {
                sum += Math.abs(frame[j]);
                weightedSum += j * Math.abs(frame[j]);
            }
            centroid[i] = sum > 1e-10 ? weightedSum / sum : 0;
        }

        double[] result = new double[3];
        double meanCentroid = 0;
        double varCentroid = 0;
        for (double c : centroid) {
            meanCentroid += c;
        }
        meanCentroid /= centroid.length;
        for (double c : centroid) {
            varCentroid += Math.pow(c - meanCentroid, 2);
        }
        varCentroid /= centroid.length;

        result[0] = meanCentroid;
        result[1] = varCentroid;
        result[2] = Math.sqrt(varCentroid);
        return result;
    }

    private double[] computeSpectralFeatures(List<double[]> mfccFrames) {
        double[] result = new double[3];

        double rmsSum = 0;
        double rolloffSum = 0;
        double bandwidthSum = 0;

        for (double[] frame : mfccFrames) {
            double rms = 0;
            for (double v : frame) {
                rms += v * v;
            }
            rms = Math.sqrt(rms / frame.length);
            rmsSum += rms;

            double cumsum = 0;
            double total = 0;
            for (double v : frame) {
                total += Math.abs(v);
            }
            int rolloffIndex = 0;
            for (int i = 0; i < frame.length; i++) {
                cumsum += Math.abs(frame[i]);
                if (cumsum >= 0.85 * total) {
                    rolloffIndex = i;
                    break;
                }
            }
            rolloffSum += rolloffIndex;

            double meanFreq = 0;
            for (int i = 0; i < frame.length; i++) {
                meanFreq += i * Math.abs(frame[i]);
            }
            meanFreq /= total > 1e-10 ? total : 1;

            double bandwidth = 0;
            for (int i = 0; i < frame.length; i++) {
                bandwidth += Math.abs(frame[i]) * Math.pow(i - meanFreq, 2);
            }
            bandwidth = Math.sqrt(bandwidth / (total > 1e-10 ? total : 1));
            bandwidthSum += bandwidth;
        }

        result[0] = rmsSum / mfccFrames.size();
        result[1] = rolloffSum / mfccFrames.size();
        result[2] = bandwidthSum / mfccFrames.size();

        return result;
    }

    public double[] extractMFCC(String audioFilePath) {
        return extractFeatures(audioFilePath);
    }

    private double[] convertToMono(byte[] buffer, int bytesRead, AudioFormat format) {
        int channels = format.getChannels();
        int bitsPerSample = format.getSampleSizeInBits();
        int bytesPerSample = bitsPerSample / 8;
        int sampleCount = bytesRead / (channels * bytesPerSample);
        double[] monoSamples = new double[sampleCount];

        for (int i = 0; i < sampleCount; i++) {
            double sum = 0;
            for (int c = 0; c < channels; c++) {
                int offset = i * channels * bytesPerSample + c * bytesPerSample;
                sum += bytesToDouble(buffer, offset, bytesPerSample, format.isBigEndian());
            }
            monoSamples[i] = sum / channels;
        }

        return monoSamples;
    }

    private double bytesToDouble(byte[] buffer, int offset, int bytesPerSample, boolean bigEndian) {
        double value = 0;
        if (bytesPerSample == 2) {
            int intValue;
            if (bigEndian) {
                intValue = ((buffer[offset] & 0xFF) << 8) | (buffer[offset + 1] & 0xFF);
            } else {
                intValue = ((buffer[offset + 1] & 0xFF) << 8) | (buffer[offset] & 0xFF);
            }
            if ((intValue & 0x8000) != 0) {
                intValue = intValue - 0x10000;
            }
            value = intValue / 32768.0;
        } else if (bytesPerSample == 4) {
            int intValue;
            if (bigEndian) {
                intValue = ((buffer[offset] & 0xFF) << 24) | ((buffer[offset + 1] & 0xFF) << 16)
                        | ((buffer[offset + 2] & 0xFF) << 8) | (buffer[offset + 3] & 0xFF);
            } else {
                intValue = ((buffer[offset + 3] & 0xFF) << 24) | ((buffer[offset + 2] & 0xFF) << 16)
                        | ((buffer[offset + 1] & 0xFF) << 8) | (buffer[offset] & 0xFF);
            }
            value = Float.intBitsToFloat(intValue);
        } else {
            value = (buffer[offset] & 0xFF) / 128.0 - 1.0;
        }
        return value;
    }

    private double[] resample(double[] samples, double srcRate, double dstRate) {
        double ratio = dstRate / srcRate;
        int newLength = (int) (samples.length * ratio);
        double[] resampled = new double[newLength];

        for (int i = 0; i < newLength; i++) {
            double pos = i / ratio;
            int idx = (int) pos;
            double frac = pos - idx;

            if (idx >= samples.length - 1) {
                resampled[i] = samples[samples.length - 1];
            } else {
                resampled[i] = samples[idx] * (1 - frac) + samples[idx + 1] * frac;
            }
        }

        return resampled;
    }

    private double[] computeMFCC(double[] samples) {
        int n = samples.length;
        if (n < BUFFER_SIZE) {
            double[] padded = new double[BUFFER_SIZE];
            System.arraycopy(samples, 0, padded, 0, n);
            for (int i = n; i < BUFFER_SIZE; i++) {
                padded[i] = 0;
            }
            samples = padded;
            n = BUFFER_SIZE;
        }

        double[] window = hammingWindow(n);
        double[] windowedSamples = new double[n];
        for (int i = 0; i < n; i++) {
            windowedSamples[i] = samples[i] * window[i];
        }

        double[] real = new double[n];
        double[] imag = new double[n];
        System.arraycopy(windowedSamples, 0, real, 0, n);
        fft(real, imag);

        for (int i = 0; i < n; i++) {
            real[i] /= n;
            imag[i] /= n;
        }

        double[] powerSpectrum = new double[n / 2];
        for (int i = 0; i < n / 2; i++) {
            powerSpectrum[i] = real[i] * real[i] + imag[i] * imag[i];
        }

        double[][] melFilters = createMelFilters(n / 2, SAMPLE_RATE);
        double[] melSpectrum = new double[MEL_FILTERS];
        for (int m = 0; m < MEL_FILTERS; m++) {
            for (int k = 0; k < n / 2; k++) {
                melSpectrum[m] += powerSpectrum[k] * melFilters[m][k];
            }
        }

        for (int m = 0; m < MEL_FILTERS; m++) {
            melSpectrum[m] = Math.log(Math.max(melSpectrum[m], 1e-10));
        }

        double[][] dctMatrix = createDCTMatrix(MEL_FILTERS, MFCC_COEFFICIENTS);
        double[] mfcc = new double[MFCC_COEFFICIENTS];
        for (int i = 0; i < MFCC_COEFFICIENTS; i++) {
            for (int j = 0; j < MEL_FILTERS; j++) {
                mfcc[i] += dctMatrix[i][j] * melSpectrum[j];
            }
        }

        return mfcc;
    }

    public void fft(double[] real, double[] imag) {
        int n = real.length;
        int shift = 0;
        for (int k = n; k > 1; k >>= 1) shift++;

        for (int i = 0; i < n; i++) {
            int j = 0;
            for (int k = 0; k < shift; k++) {
                j |= ((i >> k) & 1) << (shift - 1 - k);
            }
            if (i < j) {
                double temp = real[i];
                real[i] = real[j];
                real[j] = temp;
                temp = imag[i];
                imag[i] = imag[j];
                imag[j] = temp;
            }
        }

        for (int s = 1; s <= shift; s++) {
            int m = 1 << s;
            double wmReal = Math.cos(-2 * Math.PI / m);
            double wmImag = Math.sin(-2 * Math.PI / m);

            for (int k = 0; k < n; k += m) {
                double wReal = 1;
                double wImag = 0;

                for (int j = 0; j < m / 2; j++) {
                    int t = k + j + m / 2;
                    double uReal = real[k + j];
                    double uImag = imag[k + j];
                    double vReal = real[t] * wReal - imag[t] * wImag;
                    double vImag = real[t] * wImag + imag[t] * wReal;

                    real[k + j] = uReal + vReal;
                    imag[k + j] = uImag + vImag;
                    real[t] = uReal - vReal;
                    imag[t] = uImag - vImag;

                    double tempReal = wReal * wmReal - wImag * wmImag;
                    double tempImag = wReal * wmImag + wImag * wmReal;
                    wReal = tempReal;
                    wImag = tempImag;
                }
            }
        }
    }

    private double[] hammingWindow(int n) {
        double[] window = new double[n];
        for (int i = 0; i < n; i++) {
            window[i] = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (n - 1));
        }
        return window;
    }

    private double[][] createMelFilters(int numBins, double sampleRate) {
        double[][] filters = new double[MEL_FILTERS][numBins];
        double lowMel = freqToMel(0);
        double highMel = freqToMel(sampleRate / 2);
        double[] melPoints = new double[MEL_FILTERS + 2];

        for (int i = 0; i < MEL_FILTERS + 2; i++) {
            melPoints[i] = lowMel + (highMel - lowMel) * i / (MEL_FILTERS + 1);
        }

        int[] binIndices = new int[MEL_FILTERS + 2];
        for (int i = 0; i < MEL_FILTERS + 2; i++) {
            binIndices[i] = (int) (numBins * melToFreq(melPoints[i]) / (sampleRate / 2));
        }

        for (int m = 0; m < MEL_FILTERS; m++) {
            if (binIndices[m + 1] > binIndices[m]) {
                for (int k = binIndices[m]; k < binIndices[m + 1]; k++) {
                    filters[m][k] = (k - binIndices[m]) / (double) (binIndices[m + 1] - binIndices[m]);
                }
            }
            if (binIndices[m + 2] > binIndices[m + 1]) {
                for (int k = binIndices[m + 1]; k < binIndices[m + 2]; k++) {
                    filters[m][k] = (binIndices[m + 2] - k) / (double) (binIndices[m + 2] - binIndices[m + 1]);
                }
            }
        }

        return filters;
    }

    private double freqToMel(double freq) {
        return 2595.0 * Math.log10(1.0 + freq / 700.0);
    }

    private double melToFreq(double mel) {
        return 700.0 * (Math.pow(10.0, mel / 2595.0) - 1.0);
    }

    private double[][] createDCTMatrix(int inputSize, int outputSize) {
        double[][] matrix = new double[outputSize][inputSize];
        double sqrt1N = Math.sqrt(1.0 / inputSize);
        double sqrt2N = Math.sqrt(2.0 / inputSize);

        for (int i = 0; i < outputSize; i++) {
            matrix[i][0] = sqrt1N;
            for (int j = 1; j < inputSize; j++) {
                matrix[i][j] = sqrt2N * Math.cos(Math.PI * i * (2 * j + 1) / (2 * inputSize));
            }
        }

        return matrix;
    }

    public void fitNormalization(List<double[]> featuresList) {
        if (featuresList == null || featuresList.isEmpty()) {
            featuresNormalized = false;
            return;
        }

        int dim = featuresList.get(0).length;
        featureMean = new double[dim];
        featureStd = new double[dim];

        for (double[] features : featuresList) {
            for (int i = 0; i < dim; i++) {
                featureMean[i] += features[i];
            }
        }

        for (int i = 0; i < dim; i++) {
            featureMean[i] /= featuresList.size();
        }

        for (double[] features : featuresList) {
            for (int i = 0; i < dim; i++) {
                double diff = features[i] - featureMean[i];
                featureStd[i] += diff * diff;
            }
        }

        for (int i = 0; i < dim; i++) {
            featureStd[i] = Math.sqrt(featureStd[i] / featuresList.size());
            if (featureStd[i] < 1e-10) {
                featureStd[i] = 1;
            }
        }

        featuresNormalized = true;
    }

    public double[] normalize(double[] features) {
        if (!featuresNormalized || featureMean == null || featureStd == null) {
            return features;
        }

        double[] normalized = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            normalized[i] = (features[i] - featureMean[i]) / featureStd[i];
        }
        return normalized;
    }

    public double[] extractCombinedFeatures(String audioFilePath) {
        return extractFeatures(audioFilePath);
    }

    public double computeEuclideanDistance(double[] features1, double[] features2) {
        if (features1 == null || features2 == null || features1.length != features2.length) {
            return Double.MAX_VALUE;
        }

        double sum = 0;
        for (int i = 0; i < features1.length; i++) {
            double diff = features1[i] - features2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public double computeCosineSimilarity(double[] features1, double[] features2) {
        if (features1 == null || features2 == null || features1.length != features2.length) {
            return 0;
        }

        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i = 0; i < features1.length; i++) {
            dotProduct += features1[i] * features2[i];
            norm1 += features1[i] * features1[i];
            norm2 += features2[i] * features2[i];
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public boolean isFeaturesNormalized() {
        return featuresNormalized;
    }
}