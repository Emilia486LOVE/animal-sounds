import os
import sys
import json
import time
import argparse
import requests
import numpy as np
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.ensemble import RandomForestClassifier
from sklearn.svm import SVC
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import Pipeline
from sklearn.multiclass import OneVsRestClassifier
import joblib
import librosa
import librosa.display

BASE_URL = "http://localhost:8080/api"

def get_auth_token(username, password):
    url = f"{BASE_URL}/auth/login"
    data = {"username": username, "password": password}
    response = requests.post(url, json=data)
    if response.status_code == 200:
        return response.json()["data"]["token"]
    raise Exception(f"Authentication failed: {response.text}")

def get_annotations(token, dataset_id=None):
    url = f"{BASE_URL}/annotations"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        return response.json()["data"]
    return []

def get_labels(token):
    url = f"{BASE_URL}/labels"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        return response.json()["data"]
    return []

def get_audio_file(token, audio_id):
    url = f"{BASE_URL}/audio/{audio_id}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        return response.json()["data"]
    return None

def download_audio_file(token, file_path, save_dir):
    url = f"{BASE_URL}/audio/download/{file_path}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(url, headers=headers, stream=True)
    if response.status_code == 200:
        save_path = os.path.join(save_dir, os.path.basename(file_path))
        with open(save_path, 'wb') as f:
            for chunk in response.iter_content(chunk_size=8192):
                f.write(chunk)
        return save_path
    return None

def get_label_hierarchy(labels):
    hierarchy = {}
    for label in labels:
        hierarchy[label["labelId"]] = {
            "name": label["labelName"],
            "parentId": label["parentId"],
            "taxonRank": label["taxonRank"]
        }
    return hierarchy

def extract_audio_features(audio_path, start_time=0, end_time=None):
    try:
        y, sr = librosa.load(audio_path, sr=None)
        
        if end_time and end_time > start_time:
            start_sample = int(start_time * sr)
            end_sample = int(end_time * sr)
            y = y[start_sample:end_sample]
        
        features = []
        
        mfccs = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=20)
        features.extend(np.mean(mfccs, axis=1))
        features.extend(np.std(mfccs, axis=1))
        
        mel_spec = librosa.feature.melspectrogram(y=y, sr=sr, n_mels=40)
        log_mel_spec = librosa.power_to_db(mel_spec, ref=np.max)
        features.extend(np.mean(log_mel_spec, axis=1))
        features.extend(np.std(log_mel_spec, axis=1))
        
        spectral_centroid = librosa.feature.spectral_centroid(y=y, sr=sr)
        features.append(np.mean(spectral_centroid))
        features.append(np.std(spectral_centroid))
        
        spectral_bandwidth = librosa.feature.spectral_bandwidth(y=y, sr=sr)
        features.append(np.mean(spectral_bandwidth))
        features.append(np.std(spectral_bandwidth))
        
        spectral_rolloff = librosa.feature.spectral_rolloff(y=y, sr=sr)
        features.append(np.mean(spectral_rolloff))
        features.append(np.std(spectral_rolloff))
        
        zero_crossing_rate = librosa.feature.zero_crossing_rate(y)
        features.append(np.mean(zero_crossing_rate))
        features.append(np.std(zero_crossing_rate))
        
        chroma_stft = librosa.feature.chroma_stft(y=y, sr=sr)
        features.extend(np.mean(chroma_stft, axis=1))
        features.extend(np.std(chroma_stft, axis=1))
        
        rms = librosa.feature.rms(y=y)
        features.append(np.mean(rms))
        features.append(np.std(rms))
        
        tempo, _ = librosa.beat.beat_track(y=y, sr=sr)
        features.append(tempo)
        
        return np.array(features)
    
    except Exception as e:
        print(f"Error extracting features from {audio_path}: {str(e)}")
        return None

def build_hierarchical_labels(annotations, hierarchy):
    hierarchical_data = []
    for ann in annotations:
        if ann["status"] != "approved":
            continue
        label_id = ann["labelId"]
        if label_id not in hierarchy:
            continue
        
        current = hierarchy[label_id]
        path = [current]
        while current["parentId"] != 0 and current["parentId"] in hierarchy:
            current = hierarchy[current["parentId"]]
            path.insert(0, current)
        
        hierarchical_data.append({
            "annotationId": ann["annotationId"],
            "audioId": ann["audioId"],
            "startTime": ann.get("startTime", 0),
            "endTime": ann.get("endTime", None),
            "labels": path,
            "labelId": label_id
        })
    return hierarchical_data

def prepare_training_data(hierarchical_data, token, cache_dir):
    X = []
    y = []
    label_ids = []
    
    os.makedirs(cache_dir, exist_ok=True)
    
    for item in hierarchical_data:
        audio_info = get_audio_file(token, item["audioId"])
        if not audio_info:
            continue
        
        file_path = audio_info.get("filePath", "")
        if not file_path:
            continue
        
        cached_path = os.path.join(cache_dir, os.path.basename(file_path))
        
        if not os.path.exists(cached_path):
            cached_path = download_audio_file(token, file_path, cache_dir)
            if not cached_path:
                continue
        
        features = extract_audio_features(cached_path, item["startTime"], item["endTime"])
        if features is None:
            continue
        
        X.append(features)
        y.append(item["labelId"])
        label_ids.append(item["labelId"])
    
    return np.array(X), np.array(y), np.array(label_ids)

def calculate_hierarchical_loss(y_true, y_pred, hierarchy):
    loss = 0.0
    for i in range(len(y_true)):
        if y_true[i] != y_pred[i]:
            loss += 1.0
            parent_true = hierarchy.get(y_true[i], {}).get("parentId", 0)
            parent_pred = hierarchy.get(y_pred[i], {}).get("parentId", 0)
            
            weight = 1.5
            while parent_true != 0 and parent_true != parent_pred:
                loss += weight
                weight *= 1.5
                parent_true = hierarchy.get(parent_true, {}).get("parentId", 0)
                parent_pred = hierarchy.get(parent_pred, {}).get("parentId", 0)
    
    return loss / len(y_true)

def evaluate_hierarchical(y_true, y_pred, hierarchy, label_encoder):
    results = {}
    
    all_labels = list(hierarchy.values())
    for rank in ["kingdom", "phylum", "class", "order", "family", "genus", "species"]:
        y_true_rank = []
        y_pred_rank = []
        
        for i in range(len(y_true)):
            true_path = []
            current = hierarchy.get(y_true[i])
            while current:
                if current["taxonRank"] == rank:
                    true_path.append(current["name"])
                    break
                current = hierarchy.get(current["parentId"])
            
            pred_path = []
            current = hierarchy.get(y_pred[i])
            while current:
                if current["taxonRank"] == rank:
                    pred_path.append(current["name"])
                    break
                current = hierarchy.get(current["parentId"])
            
            if true_path and pred_path:
                y_true_rank.append(true_path[0])
                y_pred_rank.append(pred_path[0])
        
        if y_true_rank:
            results[rank] = {
                "accuracy": accuracy_score(y_true_rank, y_pred_rank),
                "precision": precision_score(y_true_rank, y_pred_rank, average="macro", zero_division=0),
                "recall": recall_score(y_true_rank, y_pred_rank, average="macro", zero_division=0),
                "f1_score": f1_score(y_true_rank, y_pred_rank, average="macro", zero_division=0)
            }
    
    return results

def save_evaluations(token, task_id, metrics):
    url = f"{BASE_URL}/evaluation/task/{task_id}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.post(url, headers=headers, json=metrics)
    return response.status_code == 200

def update_task_progress(token, task_id, epoch, val_metric):
    url = f"{BASE_URL}/train/tasks/{task_id}/progress?epoch={epoch}&valMetric={val_metric}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.post(url, headers=headers)
    return response.status_code == 200

def complete_task(token, task_id, model_path):
    url = f"{BASE_URL}/train/tasks/{task_id}/complete?modelPath={model_path}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.post(url, headers=headers)
    return response.status_code == 200

def fail_task(token, task_id, error_msg):
    url = f"{BASE_URL}/train/tasks/{task_id}/fail?errorMsg={error_msg}"
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.post(url, headers=headers)
    return response.status_code == 200

def create_model(model_type):
    if model_type.lower() == "svm":
        return Pipeline([
            ('scaler', StandardScaler()),
            ('classifier', SVC(kernel='rbf', C=1.0, gamma='scale', probability=True, random_state=42))
        ])
    elif model_type.lower() == "knn":
        return Pipeline([
            ('scaler', StandardScaler()),
            ('classifier', KNeighborsClassifier(n_neighbors=5))
        ])
    else:
        return RandomForestClassifier(n_estimators=100, max_depth=20, random_state=42)

def main():
    parser = argparse.ArgumentParser(description="Train animal voiceprint classification model")
    parser.add_argument("--task-id", type=int, required=True, help="Training task ID")
    parser.add_argument("--username", type=str, default="algorithm1", help="Username")
    parser.add_argument("--password", type=str, default="password", help="Password")
    parser.add_argument("--epochs", type=int, default=50, help="Number of epochs")
    parser.add_argument("--model-type", type=str, default="RandomForest", help="Model type")
    parser.add_argument("--cache-dir", type=str, default="./cache", help="Cache directory for audio files")
    args = parser.parse_args()
    
    try:
        token = get_auth_token(args.username, args.password)
        print("Authenticated successfully")
        
        annotations = get_annotations(token)
        print(f"Loaded {len(annotations)} annotations")
        
        labels = get_labels(token)
        hierarchy = get_label_hierarchy(labels)
        print(f"Loaded {len(labels)} labels")
        
        hierarchical_data = build_hierarchical_labels(annotations, hierarchy)
        print(f"Prepared {len(hierarchical_data)} training samples")
        
        if len(hierarchical_data) == 0:
            fail_task(token, args.task_id, "No approved annotations available for training")
            return
        
        print("Extracting audio features...")
        X, y, label_ids = prepare_training_data(hierarchical_data, token, args.cache_dir)
        
        print(f"Feature extraction complete: {X.shape[0]} samples, {X.shape[1]} features")
        
        if X.shape[0] == 0:
            fail_task(token, args.task_id, "No valid audio files for feature extraction")
            return
        
        label_encoder = LabelEncoder()
        y_encoded = label_encoder.fit_transform(y)
        
        X_train, X_val, y_train, y_val = train_test_split(X, y_encoded, test_size=0.2, random_state=42)
        
        print(f"Training set: {len(X_train)} samples")
        print(f"Validation set: {len(X_val)} samples")
        
        model = create_model(args.model_type)
        
        best_val_acc = 0.0
        model_path = f"./models/{args.task_id}/model.joblib"
        os.makedirs(f"./models/{args.task_id}", exist_ok=True)
        
        for epoch in range(args.epochs):
            model.fit(X_train, y_train)
            y_pred_val = model.predict(X_val)
            val_acc = accuracy_score(y_val, y_pred_val)
            
            update_task_progress(token, args.task_id, epoch, val_acc)
            
            print(f"Epoch {epoch+1}/{args.epochs} - Val Accuracy: {val_acc:.4f}")
            
            if val_acc > best_val_acc:
                best_val_acc = val_acc
                joblib.dump(model, model_path)
                joblib.dump(label_encoder, f"./models/{args.task_id}/label_encoder.joblib")
                np.save(f"./models/{args.task_id}/features.npy", X)
                np.save(f"./models/{args.task_id}/labels.npy", y)
        
        y_pred_all = model.predict(X_val)
        y_val_original = label_encoder.inverse_transform(y_val)
        y_pred_original = label_encoder.inverse_transform(y_pred_all)
        
        metrics = evaluate_hierarchical(y_val_original, y_pred_original, hierarchy, label_encoder)
        
        print("\nEvaluation Results:")
        for rank, values in metrics.items():
            print(f"{rank}:")
            print(f"  Accuracy: {values['accuracy']:.4f}")
            print(f"  Precision: {values['precision']:.4f}")
            print(f"  Recall: {values['recall']:.4f}")
            print(f"  F1: {values['f1_score']:.4f}")
        
        save_evaluations(token, args.task_id, metrics)
        complete_task(token, args.task_id, model_path)
        
        print(f"\nTraining completed successfully! Model saved to {model_path}")
        
    except Exception as e:
        print(f"Training failed: {str(e)}")
        try:
            fail_task(token, args.task_id, str(e))
        except:
            pass

if __name__ == "__main__":
    main()