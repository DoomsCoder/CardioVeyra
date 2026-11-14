# 🧠 CardioLens ML & Algorithm Pipeline

Welcome to the ML pipeline workspace for CardioLens! This directory is dedicated to offline dataset preprocessing, neural network model experimentation, TFLite model optimization, and Explainable AI (XAI).

---

## 🏗️ Architecture & Division of Scope

**Core Strategy:** We are using an end-to-end Deep Learning approach for rPPG. We are **not** using classical mathematical signal processing (like CHROM or POS). The neural network must learn to map raw RGB pixel variations directly to physiological vitals.

To ensure optimal on-device performance, the pipeline follows a strict separation of concerns:

- **Android Client Scope (`/app`):**
  - Real-time video capture via CameraX and ML Kit ROI extraction.
  - Extracting the raw spatial RGB averages frame-by-frame and storing them in a time-series buffer.
  - TFLite Model Execution (passing the RGB buffer into the model) and mapping outputs to the Explainability UI.

- **ML Workspace Scope (`/ml-pipeline` - Your Focus):**
  - Dataset ingestion and preprocessing (e.g., UBFC-rPPG).
  - Deep Learning architecture development (1D-CNN, MobileNet, or similar).
  - **SHAP Explainability Analysis:** Running SHAP on the validation set to extract global feature attributions and identify which waveform characteristics drive the HRV/Stress predictions.
  - Exporting, quantizing, and packaging trained weights into a compact `.tflite` model.

> **Note for Contributors:** You do not need to write any Android integration code. Focus exclusively on dataset preparation, neural network training, SHAP feature importance plots, and dropping the final `.tflite` model into the `models/` folder.
---

## 📁 Directory Structure

```text
ml-pipeline/
├── data/           # Raw & preprocessed datasets (git-ignored)
├── notebooks/      # Exploratory analysis, training, and SHAP visualizations
├── src/            # Modular Python code (preprocessing, network definitions)
├── models/         # Final exported .tflite models
└── README.md