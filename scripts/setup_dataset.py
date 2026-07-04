import os
import sys
import json
import requests
import wave
import numpy as np

BASE_URL = "http://localhost:8080/api"
AUDIO_DIR = "./audio_data"

ANIMAL_LABELS = {
    "birds": {
        "label_name": "鸟类",
        "parent_id": 0,
        "taxon_rank": "class",
        "children": [
            {"label_name": "麻雀", "taxon_rank": "species"},
            {"label_name": "喜鹊", "taxon_rank": "species"},
            {"label_name": "乌鸦", "taxon_rank": "species"},
            {"label_name": "鸽子", "taxon_rank": "species"},
            {"label_name": "鹰", "taxon_rank": "species"},
            {"label_name": "猫头鹰", "taxon_rank": "species"},
        ]
    },
    "mammals": {
        "label_name": "哺乳动物",
        "parent_id": 0,
        "taxon_rank": "class",
        "children": [
            {"label_name": "狗", "taxon_rank": "species"},
            {"label_name": "猫", "taxon_rank": "species"},
            {"label_name": "老虎", "taxon_rank": "species"},
            {"label_name": "狮子", "taxon_rank": "species"},
            {"label_name": "狼", "taxon_rank": "species"},
            {"label_name": "大象", "taxon_rank": "species"},
            {"label_name": "熊", "taxon_rank": "species"},
            {"label_name": "猴子", "taxon_rank": "species"},
        ]
    },
    "insects": {
        "label_name": "昆虫",
        "parent_id": 0,
        "taxon_rank": "class",
        "children": [
            {"label_name": "蝉", "taxon_rank": "species"},
            {"label_name": "蜜蜂", "taxon_rank": "species"},
            {"label_name": "蟋蟀", "taxon_rank": "species"},
            {"label_name": "蚊子", "taxon_rank": "species"},
        ]
    },
    "amphibians": {
        "label_name": "两栖动物",
        "parent_id": 0,
        "taxon_rank": "class",
        "children": [
            {"label_name": "青蛙", "taxon_rank": "species"},
            {"label_name": "蟾蜍", "taxon_rank": "species"},
        ]
    },
}

ANIMAL_SOUND_CONFIG = {
    "birds": {
        "麻雀": {"base_freq": 3000, "variation": 1500, "duration": 0.15, "repeats": 8},
        "喜鹊": {"base_freq": 2500, "variation": 1000, "duration": 0.2, "repeats": 5},
        "乌鸦": {"base_freq": 800, "variation": 200, "duration": 0.4, "repeats": 3},
        "鸽子": {"base_freq": 400, "variation": 100, "duration": 0.5, "repeats": 4},
        "鹰": {"base_freq": 600, "variation": 1500, "duration": 1.0, "repeats": 1},
        "猫头鹰": {"base_freq": 300, "variation": 50, "duration": 0.8, "repeats": 3},
    },
    "mammals": {
        "狗": {"base_freq": 400, "variation": 600, "duration": 0.1, "repeats": 6},
        "猫": {"base_freq": 600, "variation": 1200, "duration": 0.3, "repeats": 4},
        "老虎": {"base_freq": 100, "variation": 200, "duration": 1.5, "repeats": 1},
        "狮子": {"base_freq": 80, "variation": 150, "duration": 2.0, "repeats": 1},
        "狼": {"base_freq": 150, "variation": 400, "duration": 1.2, "repeats": 3},
        "大象": {"base_freq": 50, "variation": 100, "duration": 1.8, "repeats": 1},
        "熊": {"base_freq": 120, "variation": 150, "duration": 0.8, "repeats": 2},
        "猴子": {"base_freq": 800, "variation": 1000, "duration": 0.25, "repeats": 7},
    },
    "insects": {
        "蝉": {"base_freq": 4000, "variation": 500, "duration": 0.05, "repeats": 40},
        "蜜蜂": {"base_freq": 200, "variation": 50, "duration": 0.02, "repeats": 100},
        "蟋蟀": {"base_freq": 3000, "variation": 200, "duration": 0.1, "repeats": 20},
        "蚊子": {"base_freq": 450, "variation": 50, "duration": 0.03, "repeats": 80},
    },
    "amphibians": {
        "青蛙": {"base_freq": 300, "variation": 200, "duration": 0.3, "repeats": 6},
        "蟾蜍": {"base_freq": 150, "variation": 100, "duration": 0.5, "repeats": 4},
    },
}


def generate_animal_sound(base_freq, variation, duration, repeats, sample_rate=16000):
    t_total = duration * repeats + 0.5
    t = np.linspace(0, t_total, int(sample_rate * t_total), dtype=np.float32)
    audio = np.zeros(len(t), dtype=np.float32)
    
    for i in range(repeats):
        start_idx = int(i * duration * sample_rate)
        end_idx = int((i + 1) * duration * sample_rate)
        t_seg = t[start_idx:end_idx]
        
        freq_mod = base_freq + variation * np.sin(2 * np.pi * 10 * t_seg)
        envelope = np.sin(np.pi * t_seg / duration)
        audio[start_idx:end_idx] += np.sin(2 * np.pi * freq_mod * t_seg) * envelope * 0.3
    
    audio = audio / np.max(np.abs(audio)) * 0.7
    return audio


def save_wav_file(file_path, audio_data, sample_rate=16000):
    audio_int16 = (audio_data * 32767).astype(np.int16)
    
    with wave.open(file_path, 'w') as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        wf.writeframes(audio_int16.tobytes())


def generate_audio_files():
    print("\nGenerating synthetic animal sound files...")
    os.makedirs(AUDIO_DIR, exist_ok=True)
    generated_files = {}
    
    for class_key, sounds in ANIMAL_SOUND_CONFIG.items():
        class_dir = os.path.join(AUDIO_DIR, class_key)
        os.makedirs(class_dir, exist_ok=True)
        
        for label_name, config in sounds.items():
            file_name = f"{label_name}.wav"
            save_path = os.path.join(class_dir, file_name)
            
            if not os.path.exists(save_path):
                print(f"  Generating {file_name}...")
                audio = generate_animal_sound(
                    config["base_freq"],
                    config["variation"],
                    config["duration"],
                    config["repeats"]
                )
                save_wav_file(save_path, audio)
                generated_files[file_name] = {"path": save_path, "label_name": label_name}
            else:
                generated_files[file_name] = {"path": save_path, "label_name": label_name}
                print(f"  Skipping {file_name} (already exists)")
    
    return generated_files


def get_auth_token(username="admin", password="password"):
    url = f"{BASE_URL}/auth/login"
    data = {"username": username, "password": password}
    response = requests.post(url, json=data)
    if response.status_code == 200:
        return response.json()["data"]["token"]
    raise Exception(f"Authentication failed: {response.text}")


def create_dataset(token, name, description):
    url = f"{BASE_URL}/datasets"
    headers = {"Authorization": f"Bearer {token}"}
    data = {"datasetName": name, "description": description}
    response = requests.post(url, headers=headers, json=data)
    if response.status_code == 200:
        return response.json()["data"]
    raise Exception(f"Failed to create dataset: {response.text}")


def create_label(token, label_name, parent_id, taxon_rank, description=""):
    url = f"{BASE_URL}/labels"
    headers = {"Authorization": f"Bearer {token}"}
    data = {
        "labelName": label_name,
        "parentId": parent_id,
        "taxonRank": taxon_rank,
        "description": description
    }
    response = requests.post(url, headers=headers, json=data)
    if response.status_code == 200:
        return response.json()["data"]
    raise Exception(f"Failed to create label: {response.text}")


def upload_audio(token, dataset_id, file_path):
    url = f"{BASE_URL}/audio/upload/{dataset_id}"
    headers = {"Authorization": f"Bearer {token}"}
    with open(file_path, "rb") as f:
        files = {"files": (os.path.basename(file_path), f)}
        response = requests.post(url, headers=headers, files=files)
    if response.status_code == 200:
        return response.json()["data"]
    raise Exception(f"Failed to upload audio: {response.text}")


def setup_labels(token):
    print("Creating taxonomy labels...")
    label_map = {}
    
    for class_key, class_data in ANIMAL_LABELS.items():
        class_label = create_label(token, class_data["label_name"], class_data["parent_id"], class_data["taxon_rank"])
        label_map[class_data["label_name"]] = class_label["labelId"]
        print(f"  Created class label: {class_data['label_name']} (ID: {class_label['labelId']})")
        
        for child in class_data["children"]:
            child_label = create_label(token, child["label_name"], class_label["labelId"], child["taxon_rank"])
            label_map[child["label_name"]] = child_label["labelId"]
            print(f"    Created species label: {child['label_name']} (ID: {child_label['labelId']})")
    
    return label_map


def upload_audio_files(token, dataset_id, generated_files):
    print("\nUploading audio files...")
    uploaded_count = 0
    
    for file_name, info in generated_files.items():
        try:
            result = upload_audio(token, dataset_id, info["path"])
            print(f"  Uploaded: {file_name} -> {info['label_name']}")
            uploaded_count += len(result) if isinstance(result, list) else 1
        except Exception as e:
            print(f"  Failed to upload {file_name}: {e}")
    
    return uploaded_count


def main():
    print("=" * 60)
    print("Animal Sound Dataset Setup Tool")
    print("=" * 60)
    
    try:
        print("\nStep 1: Authenticating...")
        token = get_auth_token()
        print("  Successfully authenticated")
        
        print("\nStep 2: Creating dataset...")
        dataset = create_dataset(token, "动物叫声数据集", "包含多种动物的叫声录音，用于声纹识别和分类训练")
        dataset_id = dataset["datasetId"]
        print(f"  Created dataset: {dataset['datasetName']} (ID: {dataset_id})")
        
        print("\nStep 3: Creating taxonomy labels...")
        label_map = setup_labels(token)
        
        print("\nStep 4: Generating synthetic audio files...")
        generated_files = generate_audio_files()
        
        print("\nStep 5: Uploading audio files...")
        uploaded_count = upload_audio_files(token, dataset_id, generated_files)
        
        print("\n" + "=" * 60)
        print("Setup Complete!")
        print("=" * 60)
        print(f"Dataset ID: {dataset_id}")
        print(f"Labels created: {len(label_map)}")
        print(f"Files generated: {len(generated_files)}")
        print(f"Files uploaded: {uploaded_count}")
        print("\nYou can now access the system at http://localhost:3000")
        print("Use admin/password to login and start annotation!")
        
    except Exception as e:
        print(f"\nError: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
