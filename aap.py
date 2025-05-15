from flask import Flask, request, jsonify
from PIL import Image, UnidentifiedImageError
import io
import logging

from transformers import BlipProcessor, BlipForConditionalGeneration
import torch

from flask_cors import CORS

# Set up Flask app
app = Flask(__name__)
CORS(app)  # Enable CORS for Android/web access

# Limit request size to 5 MB
app.config['MAX_CONTENT_LENGTH'] = 5 * 1024 * 1024

# Set up logging
logging.basicConfig(level=logging.INFO)

# Load BLIP model
processor = BlipProcessor.from_pretrained("Salesforce/blip-image-captioning-base", use_fast=True)
model = BlipForConditionalGeneration.from_pretrained("Salesforce/blip-image-captioning-base")

# Warmup the model
def warmup_model():
    dummy = Image.new("RGB", (224, 224), color="white")
    inputs = processor(images=dummy, return_tensors="pt")
    model.generate(**inputs)

warmup_model()

@app.route('/', methods=['GET'])
def home():
    return '✅ Flask Image Captioning API is running.'

@app.route('/caption', methods=['GET'])
def caption_get():
    return '🟢 This endpoint only accepts POST requests with an image file.'

@app.route('/caption', methods=['POST'])
def caption_image():
    if 'image' not in request.files:
        return jsonify({'error': 'No image file provided'}), 400

    try:
        file = request.files['image']
        img_bytes = file.read()

        # ✅ Validate image using Pillow
        try:
            image = Image.open(io.BytesIO(img_bytes)).convert("RGB")
        except UnidentifiedImageError:
            return jsonify({'error': 'Invalid image file'}), 400

        # Generate caption
        inputs = processor(images=image, return_tensors="pt")
        out = model.generate(**inputs)
        caption = processor.decode(out[0], skip_special_tokens=True)

        return jsonify({'caption': caption})

    except Exception as e:
        logging.exception("Error processing image")
        return jsonify({'error': 'Internal server error'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
