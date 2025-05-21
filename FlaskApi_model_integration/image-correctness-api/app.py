from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np
from PIL import Image
import io
import requests
from urllib.parse import urlparse

app = Flask(__name__)

# Load Model
MODEL_PATH = "D:/dontopen/image correctness/model/finalefficientnet_saved"
model = tf.keras.layers.TFSMLayer(MODEL_PATH, call_endpoint="serving_default")
print("✅ Model loaded successfully!")

def is_valid_image_url(url):
    """ Check if the URL is a valid image link """
    parsed_url = urlparse(url)
    return parsed_url.scheme in ["http", "https"] and parsed_url.path.endswith(("jpg", "jpeg", "png"))

@app.route('/predict', methods=['POST'])
def predict():
    try:
        image = None

        # Check if file is uploaded
        if 'file' in request.files:
            image_file = request.files['file']
            image = Image.open(io.BytesIO(image_file.read())).convert('RGB')

        # Check if URL is provided
        elif 'url' in request.form:
            image_url = request.form['url']

            # Validate if it's a direct image URL
            if not is_valid_image_url(image_url):
                return jsonify({"error": "Please provide a direct image URL ending with .jpg, .png, etc."}), 400

            # Fetch the image
            response = requests.get(image_url, stream=True)
            if response.status_code != 200:
                return jsonify({"error": "Failed to fetch image from URL"}), 400

            image = Image.open(io.BytesIO(response.content)).convert('RGB')

        else:
            return jsonify({"error": "No image provided"}), 400

        # Preprocess image
        image = image.resize((224, 224))
        image_array = np.array(image) / 255.0
        image_array = np.expand_dims(image_array, axis=0)

        # Predict
        predictions = model(image_array)
        predicted_score = round(float(predictions["output_0"].numpy()[0][0]) * 100, 2)

        response = {"correctness_score": predicted_score}
        if predicted_score < 58:
            response["warning"] = "⚠️ Image might be incorrect!"

        return jsonify(response)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
